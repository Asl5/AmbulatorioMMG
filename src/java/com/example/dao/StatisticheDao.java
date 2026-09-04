package com.example.dao;

import com.example.service.DataService;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class StatisticheDao {

    private static final Pattern FORBIDDEN_SQL_PATTERN = Pattern.compile(
            "(^|\\W)(INSERT|UPDATE|DELETE|MERGE|DROP|ALTER|TRUNCATE|EXECUTE|CALL|BEGIN|DECLARE|COMMIT|ROLLBACK)(\\W|$)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[A-Z0-9_]+(?:\\.[A-Z0-9_]+)?$");

    private final DataService dataService;

    public StatisticheDao(DataService dataService) {
        this.dataService = dataService;
    }

    public List<Map<String, String>> loadActiveReports() throws SQLException {
        String sql = "SELECT ID, TITOLO, DESCRIZIONE, QUERY "
                + "FROM APO_STATISTICHE "
                + "WHERE ATTIVO = 1 "
                + "ORDER BY TITOLO";

        List<Map<String, String>> reports = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                LinkedHashMap<String, String> row = new LinkedHashMap<>();
                row.put("id", safe(rs.getString("ID")));
                row.put("titolo", safe(rs.getString("TITOLO")));
                row.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                reports.add(row);
            }
        }
        return reports;
    }

    public ReportExportData exportReport(String reportId, Integer anno, Integer mese, String struttura)
            throws SQLException {
        ArrayList<Integer> mesi = new ArrayList<>();
        if (mese != null) {
            mesi.add(mese);
        }
        ArrayList<String> strutture = new ArrayList<>();
        if (struttura != null && !struttura.trim().isEmpty()) {
            strutture.add(struttura);
        }
        return exportReport(reportId, anno, mesi, strutture);
    }

    public ReportExportData exportReport(String reportId, Integer anno, List<Integer> mesi, List<String> strutture)
            throws SQLException {
        ReportDefinition definition = loadReportDefinition(reportId);
        if (definition == null) {
            throw new SQLException("Report non trovato o non attivo.");
        }

        String tableName = normalizeTableName(definition.query);

        try (Connection connection = dataService.getConnection()) {
            HashSet<String> columnNames = loadColumnNames(connection, tableName);
            PreparedQuery preparedQuery = buildFilteredQuery(tableName, columnNames, anno, mesi, strutture);

            try (PreparedStatement statement = connection.prepareStatement(preparedQuery.sql)) {
                bindParameters(statement, preparedQuery.parameters);

                try (ResultSet rs = statement.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    List<String> columns = new ArrayList<>();
                    for (int columnIndex = 1; columnIndex <= columnCount; columnIndex += 1) {
                        String label = safe(metaData.getColumnLabel(columnIndex));
                        columns.add(label.isEmpty() ? "COLONNA_" + columnIndex : label);
                    }

                    List<List<Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex += 1) {
                            row.add(readCellValue(rs, columnIndex));
                        }
                        rows.add(row);
                    }

                    return new ReportExportData(definition.id, definition.titolo, columns, rows);
                }
            }
        }
    }

    private HashSet<String> loadColumnNames(Connection connection, String tableName) throws SQLException {
        HashSet<String> columnNames = new HashSet<>();
        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 0";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex += 1) {
                columnNames.add(safe(metaData.getColumnLabel(columnIndex)).toUpperCase(Locale.ROOT));
            }
        }

        return columnNames;
    }

    private String normalizeTableName(String query) throws SQLException {
        String normalized = query == null ? "" : query.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new SQLException("La tabella del report non e' valorizzata.");
        }
        if (normalized.contains(" ") || normalized.contains(";")) {
            throw new SQLException("Il campo QUERY deve contenere solo il nome tabella.");
        }
        if (FORBIDDEN_SQL_PATTERN.matcher(normalized).find()) {
            throw new SQLException("Il nome tabella contiene caratteri non consentiti.");
        }
        if (!TABLE_NAME_PATTERN.matcher(normalized).matches()) {
            throw new SQLException("Nome tabella non valido.");
        }

        return normalized;
    }

    private PreparedQuery buildFilteredQuery(String tableName, HashSet<String> columnNames,
            Integer anno, List<Integer> mesi, List<String> strutture) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        List<QueryParameter> parameters = new ArrayList<>();

        List<Integer> normalizedMonths = normalizeMonths(mesi);
        List<String> normalizedStructures = normalizeStructures(strutture);

        if (columnNames.contains("DATA_CONSULENZE") && anno != null) {
            if (normalizedMonths.isEmpty()) {
                LocalDate fromDate = LocalDate.of(anno.intValue(), 1, 1);
                LocalDate toDate = fromDate.plusYears(1L);
                appendCondition(sql, "DATA_CONSULENZE >= ? AND DATA_CONSULENZE < ?");
                parameters.add(new QueryParameter(Types.TIMESTAMP, Timestamp.valueOf(fromDate.atStartOfDay())));
                parameters.add(new QueryParameter(Types.TIMESTAMP, Timestamp.valueOf(toDate.atStartOfDay())));
            } else {
                StringBuilder monthCondition = new StringBuilder();
                for (int index = 0; index < normalizedMonths.size(); index += 1) {
                    int mese = normalizedMonths.get(index).intValue();
                    LocalDate fromDate = LocalDate.of(anno.intValue(), mese, 1);
                    LocalDate toDate = fromDate.plusMonths(1L);
                    if (index > 0) {
                        monthCondition.append(" OR ");
                    }
                    monthCondition.append("(DATA_CONSULENZE >= ? AND DATA_CONSULENZE < ?)");
                    parameters.add(new QueryParameter(Types.TIMESTAMP, Timestamp.valueOf(fromDate.atStartOfDay())));
                    parameters.add(new QueryParameter(Types.TIMESTAMP, Timestamp.valueOf(toDate.atStartOfDay())));
                }
                appendCondition(sql, "(" + monthCondition.toString() + ")");
            }
        }

        if (columnNames.contains("COD_SERVIZIO") && !normalizedStructures.isEmpty()) {
            StringBuilder structureCondition = new StringBuilder("COD_SERVIZIO IN (");
            for (int index = 0; index < normalizedStructures.size(); index += 1) {
                if (index > 0) {
                    structureCondition.append(", ");
                }
                structureCondition.append("?");
                parameters.add(new QueryParameter(Types.VARCHAR, normalizedStructures.get(index)));
            }
            structureCondition.append(")");
            appendCondition(sql, structureCondition.toString());
        }

        return new PreparedQuery(sql.toString(), parameters);
    }

    private List<Integer> normalizeMonths(List<Integer> mesi) {
        ArrayList<Integer> normalizedMonths = new ArrayList<>();
        if (mesi == null) {
            return normalizedMonths;
        }

        for (int index = 0; index < mesi.size(); index += 1) {
            Integer mese = mesi.get(index);
            if (mese == null || normalizedMonths.contains(mese)) {
                continue;
            }
            normalizedMonths.add(mese);
        }

        return normalizedMonths;
    }

    private List<String> normalizeStructures(List<String> strutture) {
        ArrayList<String> normalizedStructures = new ArrayList<>();
        if (strutture == null) {
            return normalizedStructures;
        }

        for (int index = 0; index < strutture.size(); index += 1) {
            String struttura = safe(strutture.get(index)).toUpperCase(Locale.ROOT);
            if (struttura.isEmpty() || "TUTTE".equals(struttura) || normalizedStructures.contains(struttura)) {
                continue;
            }
            normalizedStructures.add(struttura);
        }

        return normalizedStructures;
    }

    private ReportDefinition loadReportDefinition(String reportId) throws SQLException {
        String sql = "SELECT ID, TITOLO, DESCRIZIONE, QUERY "
                + "FROM APO_STATISTICHE "
                + "WHERE ATTIVO = 1 "
                + "AND ID = ?";

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reportId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new ReportDefinition(
                        safe(rs.getString("ID")),
                        safe(rs.getString("TITOLO")),
                        safe(rs.getString("DESCRIZIONE")),
                        safe(rs.getString("QUERY")));
            }
        }
    }

    private void appendCondition(StringBuilder sql, String condition) {
        if (!containsWhereClause(sql.toString())) {
            sql.append(" WHERE ");
        } else {
            sql.append(" AND ");
        }
        sql.append(condition);
    }

    private boolean containsWhereClause(String sql) {
        return Pattern.compile("\\bWHERE\\b", Pattern.CASE_INSENSITIVE).matcher(sql).find();
    }

    private void bindParameters(PreparedStatement statement, List<QueryParameter> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index += 1) {
            QueryParameter parameter = parameters.get(index);
            int position = index + 1;
            if (parameter.value == null) {
                statement.setNull(position, parameter.sqlType);
                continue;
            }

            switch (parameter.sqlType) {
                case Types.INTEGER:
                    if (parameter.value instanceof Integer) {
                        statement.setInt(position, (Integer) parameter.value);
                    } else {
                        statement.setInt(position, Integer.parseInt(String.valueOf(parameter.value)));
                    }
                    break;
                case Types.TIMESTAMP:
                    statement.setTimestamp(position, (Timestamp) parameter.value);
                    break;
                case Types.VARCHAR:
                default:
                    statement.setString(position, String.valueOf(parameter.value));
                    break;
            }
        }
    }

    private Object readCellValue(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        if (value == null) {
            return "";
        }
        if (value instanceof Clob) {
            Clob clob = (Clob) value;
            long length = clob.length();
            return clob.getSubString(1L, (int) Math.min(length, Integer.MAX_VALUE));
        }
        if (value instanceof Timestamp) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) value);
        }
        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((Date) value);
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        return value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static final class ReportDefinition {
        private final String id;
        private final String titolo;
        private final String descrizione;
        private final String query;

        private ReportDefinition(String id, String titolo, String descrizione, String query) {
            this.id = id;
            this.titolo = titolo;
            this.descrizione = descrizione;
            this.query = query;
        }
    }

    private static final class PreparedQuery {
        private final String sql;
        private final List<QueryParameter> parameters;

        private PreparedQuery(String sql, List<QueryParameter> parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }
    }

    private static final class QueryParameter {
        private final int sqlType;
        private final Object value;

        private QueryParameter(int sqlType, Object value) {
            this.sqlType = sqlType;
            this.value = value;
        }
    }

    public static final class ReportExportData {
        private final String reportId;
        private final String titolo;
        private final List<String> columns;
        private final List<List<Object>> rows;

        public ReportExportData(String reportId, String titolo, List<String> columns, List<List<Object>> rows) {
            this.reportId = reportId;
            this.titolo = titolo;
            this.columns = columns;
            this.rows = rows;
        }

        public String getReportId() {
            return reportId;
        }

        public String getTitolo() {
            return titolo;
        }

        public List<String> getColumns() {
            return columns;
        }

        public List<List<Object>> getRows() {
            return rows;
        }
    }
}
