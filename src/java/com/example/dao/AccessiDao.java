package com.example.dao;

import com.example.service.DataService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccessiDao {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY);
    private static final List<DateTimeFormatter> DATE_PARSERS = Arrays.asList(
            formatter("dd/MM/yyyy", Locale.ITALY),
            formatter("d/M/yyyy", Locale.ITALY),
            formatter("yyyy-MM-dd", Locale.ITALY),
            formatter("dd-MM-yyyy", Locale.ITALY),
            formatter("d-M-yyyy", Locale.ITALY),
            formatter("dd.MM.yyyy", Locale.ITALY),
            formatter("d.M.yyyy", Locale.ITALY),
            formatter("dd MMM yyyy", Locale.ITALY),
            formatter("d MMM yyyy", Locale.ITALY),
            formatter("dd MMM yy", Locale.ITALY),
            formatter("d MMM yy", Locale.ITALY),
            formatter("dd-MMM-yyyy", Locale.ITALY),
            formatter("d-MMM-yyyy", Locale.ITALY),
            formatter("dd-MMM-yy", Locale.ITALY),
            formatter("d-MMM-yy", Locale.ITALY),
            formatter("dd MMM yyyy", Locale.ENGLISH),
            formatter("d MMM yyyy", Locale.ENGLISH),
            formatter("dd MMM yy", Locale.ENGLISH),
            formatter("d MMM yy", Locale.ENGLISH),
            formatter("dd-MMM-yyyy", Locale.ENGLISH),
            formatter("d-MMM-yyyy", Locale.ENGLISH),
            formatter("dd-MMM-yy", Locale.ENGLISH),
            formatter("d-MMM-yy", Locale.ENGLISH)
    );
    private static final List<DateTimeFormatter> DATE_TIME_PARSERS = Arrays.asList(
            formatter("yyyy-MM-dd'T'HH:mm", Locale.ITALY),
            formatter("yyyy-MM-dd'T'HH:mm:ss", Locale.ITALY),
            formatter("yyyy-MM-dd HH:mm", Locale.ITALY),
            formatter("yyyy-MM-dd HH:mm:ss", Locale.ITALY),
            formatter("dd/MM/yyyy HH:mm", Locale.ITALY),
            formatter("dd/MM/yyyy HH:mm:ss", Locale.ITALY)
    );
    private static final Gson GSON = new Gson();
    private static final Type TERAPIE_PAYLOAD_TYPE = new TypeToken<List<Map<String, String>>>() {
    }.getType();

    private final DataService dataService;

    public AccessiDao(DataService dataService) {
        this.dataService = dataService;
    }

    public List<Map<String, String>> searchAccessi(String serviceCode, String filterDate, String dateOperator,
            String consulenza, String cognome, String nome, String sede, String codiceFiscale,
            String medico) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        sql.append("SELECT ");
        sql.append("TRIM(CAST(COD_CONSULENZE AS VARCHAR2(100))) AS COD_CONSULENZE, ");
        sql.append("TO_CHAR(DATA_CONSULENZE, 'DD/MM/YYYY') AS DATA_CONSULENZE_DISPLAY, ");
        sql.append("TO_CHAR(DATA_CONSULENZE, 'DD/MM/YYYY HH24:MI') AS DATA_ORA, ");
        sql.append("TO_CHAR(DATA_CONSULENZE, 'HH24:MI') AS ORARIO_CONSULENZA, ");
        sql.append("TO_CHAR(DATA_CONSULENZE, 'YYYY-MM-DD HH24:MI:SS') AS DATA_CONSULENZE_SORT, ");
        sql.append("TRIM(CAST(COD_SERVIZIO AS VARCHAR2(100))) AS COD_SERVIZIO, ");
        sql.append("TRIM(CAST(DESC_SERVIZIO AS VARCHAR2(255))) AS DESC_SERVIZIO, ");
        sql.append("TRIM(CAST(VALORE AS VARCHAR2(255))) AS VALORE, ");
        sql.append("TRIM(CAST(COD_FISCALE AS VARCHAR2(32))) AS COD_FISCALE, ");
        sql.append("TRIM(CAST(COGNOME AS VARCHAR2(255))) AS COGNOME, ");
        sql.append("TRIM(CAST(NOME AS VARCHAR2(255))) AS NOME, ");
        sql.append("TRIM(CAST(DATA_NASCITA AS VARCHAR2(64))) AS DATA_NASCITA_RAW, ");
        sql.append("TRIM(CAST(ETA AS VARCHAR2(20))) AS ETA, ");
        sql.append("TRIM(CAST(MOD_UTENTE AS VARCHAR2(255))) AS MOD_UTENTE, ");
        sql.append("TRIM(CAST(MEDICO AS VARCHAR2(255))) AS MEDICO, ");
        sql.append("TRIM(CAST(NOTE AS VARCHAR2(2000))) AS NOTE, ");
        sql.append("(SELECT TRIM(CAST(CG.PATOLOGIA AS VARCHAR2(255))) ");
        sql.append("FROM CONSULENZE_GM CG ");
        sql.append("WHERE TRIM(CAST(CG.COD_CONSULENZE AS VARCHAR2(100))) = TRIM(CAST(V.COD_CONSULENZE AS VARCHAR2(100))) ");
        sql.append("AND ROWNUM = 1) AS PATOLOGIA, ");
        sql.append("TRIM(CAST(PIN AS VARCHAR2(20))) AS PIN, ");
        sql.append("TRIM(CAST(COD_PAZ AS VARCHAR2(20))) AS COD_PAZ ");
        sql.append("FROM VIEW_CONSULENZE_GMNEW V WHERE 1 = 1");

        if (hasText(serviceCode)) {
            sql.append(" AND UPPER(TRIM(CAST(COD_SERVIZIO AS VARCHAR2(100)))) = UPPER(?)");
            parameters.add(serviceCode.trim());
        }

        if (hasText(filterDate)) {
            sql.append(" AND TRUNC(DATA_CONSULENZE) ");
            sql.append(normalizeDateOperator(dateOperator));
            sql.append(" TO_DATE(?, 'YYYY-MM-DD')");
            parameters.add(LocalDate.parse(filterDate.trim()).toString());
        }

        if (hasText(consulenza)) {
            sql.append(" AND UPPER(COALESCE(TRIM(CAST(VALORE AS VARCHAR2(255))), TRIM(CAST(COD_CONSULENZE AS VARCHAR2(100))))) LIKE ?");
            parameters.add(likeValue(consulenza));
        }

        if (hasText(cognome)) {
            sql.append(" AND UPPER(TRIM(CAST(COGNOME AS VARCHAR2(255)))) LIKE ?");
            parameters.add(likeValue(cognome));
        }

        if (hasText(nome)) {
            sql.append(" AND UPPER(TRIM(CAST(NOME AS VARCHAR2(255)))) LIKE ?");
            parameters.add(likeValue(nome));
        }

        if (hasText(sede)) {
            sql.append(" AND UPPER(COALESCE(TRIM(CAST(DESC_SERVIZIO AS VARCHAR2(255))), TRIM(CAST(COD_SERVIZIO AS VARCHAR2(100))))) LIKE ?");
            parameters.add(likeValue(sede));
        }

        if (hasText(codiceFiscale)) {
            sql.append(" AND UPPER(TRIM(CAST(COD_FISCALE AS VARCHAR2(32)))) LIKE ?");
            parameters.add(likeValue(codiceFiscale));
        }

        if (hasText(medico)) {
            sql.append(" AND UPPER(TRIM(CAST(MEDICO AS VARCHAR2(255)))) LIKE ?");
            parameters.add(likeValue(medico));
        }

        sql.append(" ORDER BY DATA_CONSULENZE DESC NULLS LAST, COGNOME ASC NULLS LAST, NOME ASC NULLS LAST");

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet rs = statement.executeQuery()) {
                int rowIndex = 0;
                while (rs.next()) {
                    rowIndex += 1;
                    results.add(mapAccesso(rs, rowIndex));
                }
            }
        }

        return results;
    }

    public Map<String, String> createAccesso(String serviceCode, Map<String, String> payload,
            String username, String medico, String workstation) throws SQLException {
        HashMap<String, String> created = new HashMap<>();
        String codiceFiscale = safe(payload.get("codFiscale"));
        String codPaz = safe(payload.get("codPaz"));
        String tipoAccesso = safe(payload.get("tipoAccesso"));
        String tipoAccessoLabel = safe(payload.get("tipoAccessoLabel"));
        String tipoAccessoValue = safe(payload.get("tipoAccessoValue"));
        String patologia = safe(payload.get("patologia")).toUpperCase(Locale.ITALY);
        String diagnosi = safe(payload.get("diagnosi"));
        String codComuneRes = safe(payload.get("codComuneRes"));
        Timestamp dataConsulenza = parseTimestamp(payload.get("dataOra"));
        int codConsulenze;

        try (Connection connection = dataService.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                List<String> columns = getConsulenzeGmColumns(connection);
                LinkedHashMap<String, Object> values = new LinkedHashMap<>();
                codConsulenze = loadNextCodConsulenze(connection);

                putIfColumn(values, columns, "COD_CONSULENZE", Integer.valueOf(codConsulenze));
                putIfColumn(values, columns, "DATA_CONSULENZE", dataConsulenza);
                putIfColumn(values, columns, "COD_SERVIZIO", serviceCode);
                putIfColumn(values, columns, "COD_PAZ", codPaz);
                putIfColumn(values, columns, "COD_UTENTE", username);
                putIfColumn(values, columns, "COD_PRESTAZIONE", tipoAccessoValue);
//            putIfColumn(values, columns, "STATO", Integer.valueOf(1));
                putIfColumn(values, columns, "NOTE", diagnosi);
                putIfColumn(values, columns, "DATA_MODIFICA", new Timestamp(System.currentTimeMillis()));
                putIfColumn(values, columns, "MOD_UTENTE", username);
                putIfColumn(values, columns, "WS_UTENTE", workstation);
                putIfColumn(values, columns, "ORARIO_CONSULENZA", formatOrarioConsulenza(dataConsulenza));
//            putIfColumn(values, columns, "RICOVERO", Integer.valueOf(0));
                putIfColumn(values, columns, "CANCELLATO", "N");
                putIfColumn(values, columns, "COD_FISCALE", codiceFiscale);
                putIfColumn(values, columns, "PIN", safe(payload.get("pin")));
                putIfColumn(values, columns, "MEDICO", firstNonEmpty(medico, username));
                putIfColumn(values, columns, "VALORE", firstNonEmpty(tipoAccessoLabel, tipoAccessoValue));
                putIfColumn(values, columns, "COD_PRESTAZIONE_NOTE", tipoAccessoValue);
                putIfColumn(values, columns, "COGNOME", safe(payload.get("cognome")));
                putIfColumn(values, columns, "NOME", safe(payload.get("nome")));
                putIfColumn(values, columns, "SESSO", safe(payload.get("sesso")));
                putIfColumn(values, columns, "COD_COM_RES", codComuneRes);
                putIfColumn(values, columns, "PATOLOGIA", patologia);

                if (!values.containsKey("COD_CONSULENZE")
                        || !values.containsKey("DATA_CONSULENZE")
                        || !values.containsKey("COD_SERVIZIO")
                        || !values.containsKey("COD_PAZ")
                        || !values.containsKey("COD_UTENTE")
                        || !values.containsKey("COD_PRESTAZIONE")) {
                    throw new SQLException("Mancano le colonne minime richieste in CONSULENZE_GM per inserire il nuovo accesso.");
                }

                insertConsulenza(connection, values);
                if (isTruthy(payload.get("schedaPaziente"))) {
                    BigDecimal idScheda = insertSchedaPaziente(connection, codPaz, payload, username);
                    insertSchedaPazientePatologie(connection, idScheda, codPaz, payload, username);
                    insertSchedaPazienteAllergie(connection, idScheda, codPaz, payload, username);
                    insertSchedaPazienteTerapie(connection, idScheda, codPaz, payload, username);
                    insertSchedaPazienteDiario(connection, idScheda, codPaz, payload, username);
                }
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }

        created.put("codConsulenze", String.valueOf(codConsulenze));
        created.put("codFiscale", codiceFiscale);
        created.put("codPaz", codPaz);
        created.put("codServizio", safe(serviceCode));
        created.put("tipoAccesso", tipoAccesso);
        created.put("tipoAccessoLabel", tipoAccessoLabel);
        created.put("tipoAccessoValue", tipoAccessoValue);
        created.put("patologia", patologia);
        created.put("diagnosi", diagnosi);
        created.put("codComuneRes", codComuneRes);
        created.put("dataOra", dataConsulenza == null ? "" : dataConsulenza.toString());
        return created;
    }

    public int deleteAccesso(String codConsulenza) throws SQLException {
        String sql = "UPDATE CONSULENZE_GM SET CANCELLATO = 'S' "
                + "WHERE TRIM(CAST(COD_CONSULENZE AS VARCHAR2(100))) = ?";

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codConsulenza));
            return statement.executeUpdate();
        }
    }

    public void saveSchedaPaziente(String codPaz, Map<String, String> payload, String username) throws SQLException {
        try (Connection connection = dataService.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                BigDecimal idScheda = insertSchedaPaziente(connection, codPaz, payload, username);
                insertSchedaPazientePatologie(connection, idScheda, codPaz, payload, username);
                insertSchedaPazienteAllergie(connection, idScheda, codPaz, payload, username);
                deleteAllSchedaPazienteTerapie(connection, idScheda, codPaz);
                insertSchedaPazienteTerapie(connection, idScheda, codPaz, payload, username);
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private void deleteAllSchedaPazienteTerapie(Connection connection, BigDecimal idScheda, String codPaz)
            throws SQLException {
        if (idScheda == null) {
            return;
        }

        Map<String, String> tableColumns = findTableColumns(connection, "APO_SCHEDE_PAZIENTE_TERAPIE");
        String patientColumn = firstExistingColumn(tableColumns, "COD_PAZIENTE", "COD_PAZ");
        if (patientColumn == null) {
            return;
        }

        String sql = "DELETE FROM APO_SCHEDE_PAZIENTE_TERAPIE "
                + "WHERE ID_SCHEDA = ? "
                + "AND TRIM(CAST(" + patientColumn + " AS VARCHAR2(100))) = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, idScheda);
            statement.setString(2, safe(codPaz));
            statement.executeUpdate();
        }
    }

    private Map<String, String> mapAccesso(ResultSet rs, int rowIndex) throws SQLException {
        HashMap<String, String> accesso = new HashMap<>();
        String codConsulenze = safe(rs.getString("COD_CONSULENZE"));
        String dataDisplay = safe(rs.getString("DATA_CONSULENZE_DISPLAY"));
        String dataOra = safe(rs.getString("DATA_ORA"));
        String orarioConsulenza = safe(rs.getString("ORARIO_CONSULENZA"));
        String dataSortKey = safe(rs.getString("DATA_CONSULENZE_SORT"));
        String codServizio = safe(rs.getString("COD_SERVIZIO"));
        String descServizio = safe(rs.getString("DESC_SERVIZIO"));
        String valore = safe(rs.getString("VALORE"));
        String codFiscale = safe(rs.getString("COD_FISCALE"));
        String cognome = safe(rs.getString("COGNOME"));
        String nome = safe(rs.getString("NOME"));
        String dataNascitaDisplay = normalizeDisplayDate(safe(rs.getString("DATA_NASCITA_RAW")));
        String eta = safe(rs.getString("ETA"));
        String modUtente = safe(rs.getString("MOD_UTENTE"));
        String medico = safe(rs.getString("MEDICO"));
        String note = safe(rs.getString("NOTE"));
        String patologia = safe(rs.getString("PATOLOGIA"));
        String pin = safe(rs.getString("PIN"));
        String codPaz = safe(rs.getString("COD_PAZ"));

        String consulenza = codConsulenze;
        String sede = hasText(descServizio) ? descServizio : codServizio;
        String paziente = (cognome + " " + nome).trim();
        String operatore = hasText(medico) ? medico : modUtente;

        accesso.put("id", buildAccessoId(codConsulenze, dataSortKey, codFiscale, rowIndex));
        accesso.put("codConsulenze", codConsulenze);
        accesso.put("dataConsulenze", dataDisplay);
        accesso.put("dataDisplay", dataDisplay);
        accesso.put("dataOra", dataOra);
        accesso.put("orarioConsulenza", orarioConsulenza);
        accesso.put("dataIso", dataSortKey);
        accesso.put("codServizio", codServizio);
        accesso.put("descServizio", descServizio);
        accesso.put("valore", valore);
        accesso.put("consulenza", consulenza);
        accesso.put("sede", sede);
        accesso.put("codFiscale", codFiscale);
        accesso.put("cognome", cognome);
        accesso.put("nome", nome);
        accesso.put("paziente", paziente);
        accesso.put("dataNascita", dataNascitaDisplay);
        accesso.put("eta", eta);
        accesso.put("modUtente", modUtente);
        accesso.put("medico", medico);
        accesso.put("operatore", operatore);
        accesso.put("note", note);
        accesso.put("patologia", patologia);
        accesso.put("pin", pin);
        accesso.put("codPaz", codPaz);

        return accesso;
    }

    private void insertConsulenza(Connection connection, LinkedHashMap<String, Object> values) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO CONSULENZE_GM (");

        int index = 0;
        for (String columnName : values.keySet()) {
            if (index > 0) {
                sql.append(", ");
            }
            sql.append(columnName);
            index += 1;
        }

        sql.append(") VALUES (");
        for (int parameterIndex = 0; parameterIndex < values.size(); parameterIndex += 1) {
            if (parameterIndex > 0) {
                sql.append(", ");
            }
            sql.append("?");
        }
        sql.append(")");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            for (Object value : values.values()) {
                bindInsertValue(statement, parameterIndex, value);
                parameterIndex += 1;
            }
            statement.executeUpdate();
        }
    }

    private BigDecimal insertSchedaPaziente(Connection connection, String codPaz, Map<String, String> payload,
            String username) throws SQLException {
        BigDecimal idScheda = findLatestSchedaPazienteId(connection, codPaz);
        if (idScheda != null) {
            updateSchedaPaziente(connection, idScheda, codPaz, payload);
            return idScheda;
        }

        String sql = "BEGIN INSERT INTO APO_SCHEDE_PAZIENTE ("
                + "COD_PAZ, DATA_INS, UTENTE_INS, TELEFONO, STATO_CIVILE, "
                + "FUMO, IBM, IBM_KG, IBM_CM, CAREGIVER"
                + ") VALUES (?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "RETURNING ID INTO ?; END;";

        try (CallableStatement statement = connection.prepareCall(sql)) {
            int parameterIndex = 1;
            statement.setString(parameterIndex++, safe(codPaz));
            statement.setString(parameterIndex++, safe(username));
            statement.setString(parameterIndex++, safe(payload.get("schedaTelefono")));
            setNullableInteger(statement, parameterIndex++, payload.get("schedaStatoCivile"), "STATO_CIVILE");
            setNullableInteger(statement, parameterIndex++, normalizeFumoValue(payload.get("schedaFumo")), "FUMO");
            setNullableBigDecimal(statement, parameterIndex++, payload.get("schedaIbm"), "IBM");
            setNullableBigDecimal(statement, parameterIndex++, payload.get("schedaIbmKg"), "IBM_KG");
            setNullableBigDecimal(statement, parameterIndex++, payload.get("schedaIbmCm"), "IBM_CM");
            statement.setString(parameterIndex++, safe(payload.get("schedaCaregiver")));
            statement.registerOutParameter(parameterIndex, Types.NUMERIC);
            statement.execute();
            return statement.getBigDecimal(parameterIndex);
        }
    }

    private void updateSchedaPaziente(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> payload) throws SQLException {
        String sql = "UPDATE APO_SCHEDE_PAZIENTE SET "
                + "TELEFONO = ?, STATO_CIVILE = ?, FUMO = ?, IBM = ?, IBM_KG = ?, IBM_CM = ?, CAREGIVER = ? "
                + "WHERE ID = ? AND TRIM(CAST(COD_PAZ AS VARCHAR2(100))) = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            statement.setString(parameterIndex++, safe(payload.get("schedaTelefono")));
            setNullableInteger(statement, parameterIndex++, payload.get("schedaStatoCivile"), "STATO_CIVILE");
            setNullableInteger(statement, parameterIndex++, normalizeFumoValue(payload.get("schedaFumo")), "FUMO");
            setNullableBigDecimal(statement, parameterIndex++, payload.get("schedaIbm"), "IBM");
            setNullableBigDecimal(statement, parameterIndex++, payload.get("schedaIbmKg"), "IBM_KG");
            setNullableBigDecimal(statement, parameterIndex++, payload.get("schedaIbmCm"), "IBM_CM");
            statement.setString(parameterIndex++, safe(payload.get("schedaCaregiver")));
            statement.setBigDecimal(parameterIndex++, idScheda);
            statement.setString(parameterIndex, safe(codPaz));
            statement.executeUpdate();
        }
    }

    private void insertSchedaPazientePatologie(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> payload, String username) throws SQLException {
        List<String> codiciPatologia = parseSchedaPatologie(payload.get("schedaPatologie"));
        if (idScheda == null) {
            return;
        }

        LinkedHashMap<String, String> selected = new LinkedHashMap<>();
        for (String codicePatologia : codiciPatologia) {
            String key = normalizePatologiaKey(codicePatologia);
            if (!key.isEmpty()) {
                selected.put(key, safe(codicePatologia));
            }
        }
        Map<String, String> existing = loadSchedaPazientePatologie(connection, idScheda, codPaz);
        deleteRemovedSchedaPazientePatologie(connection, idScheda, codPaz, existing, selected);

        String sql = "INSERT INTO APO_SCHEDE_PAZIENTE_PATOLOGIE ("
                + "ID_SCHEDA, COD_PAZIENTE, CODICE_PATOLOGIA, UTENTE_INS"
                + ") VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean hasBatch = false;
            for (Map.Entry<String, String> entry : selected.entrySet()) {
                if (existing.containsKey(entry.getKey())) {
                    continue;
                }
                statement.setBigDecimal(1, idScheda);
                statement.setString(2, safe(codPaz));
                statement.setString(3, entry.getValue());
                statement.setString(4, safe(username));
                statement.addBatch();
                hasBatch = true;
            }
            if (hasBatch) {
                statement.executeBatch();
            }
        }
    }

    private void insertSchedaPazienteAllergie(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> payload, String username) throws SQLException {
        List<String> codiciAllergia = parseSchedaPatologie(payload.get("schedaAllergieCodes"));
        if (idScheda == null) {
            return;
        }

        LinkedHashMap<String, String> selected = new LinkedHashMap<>();
        for (String codiceAllergia : codiciAllergia) {
            String key = normalizePatologiaKey(codiceAllergia);
            if (!key.isEmpty()) {
                selected.put(key, safe(codiceAllergia));
            }
        }
        Map<String, String> existing = loadSchedaPazienteAllergie(connection, idScheda, codPaz);
        deleteRemovedSchedaPazienteAllergie(connection, idScheda, codPaz, existing, selected);

        String sql = "INSERT INTO APO_SCHEDE_PAZIENTE_ALLERGIE ("
                + "ID_SCHEDA, COD_PAZIENTE, CODICE_ALLERGIA, UTENTE_INS"
                + ") VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean hasBatch = false;
            for (Map.Entry<String, String> entry : selected.entrySet()) {
                if (existing.containsKey(entry.getKey())) {
                    continue;
                }
                statement.setBigDecimal(1, idScheda);
                statement.setString(2, safe(codPaz));
                statement.setString(3, entry.getValue());
                statement.setString(4, safe(username));
                statement.addBatch();
                hasBatch = true;
            }
            if (hasBatch) {
                statement.executeBatch();
            }
        }
    }

    private void insertSchedaPazienteTerapie(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> payload, String username) throws SQLException {
        List<Map<String, String>> terapie = parseSchedaTerapie(payload.get("schedaTerapieJson"));
        if (idScheda == null || terapie.isEmpty()) {
            return;
        }

        Map<String, String> tableColumns = findTableColumns(connection, "APO_SCHEDE_PAZIENTE_TERAPIE");
        Map<String, String> existing = loadSchedaPazienteTerapie(connection, idScheda, codPaz, tableColumns);

        for (Map<String, String> terapia : terapie) {
            String farmaco = getTerapiaPayloadValue(terapia, "farmaco");
            if (farmaco.isEmpty()) {
                continue;
            }

            String key = buildSchedaTerapiaKey(terapia);
            if (existing.containsKey(key)) {
                continue;
            }

            ArrayList<String> insertColumns = new ArrayList<>();
            ArrayList<Object> insertValues = new ArrayList<>();
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns, idScheda, "ID_SCHEDA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns, safe(codPaz), "COD_PAZIENTE", "COD_PAZ");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    getTerapiaPayloadValue(terapia, "principioAttivo", "principio_attivo", "pa"),
                    "PRINCIPIO_ATTIVO", "PRINC_ATTIVO", "PA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns, farmaco, "FARMACO");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    getTerapiaPayloadValue(terapia, "confezione"), "CONFEZIONE");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    parseBigDecimal(getTerapiaPayloadValue(terapia, "quantita", "qta"), "quantita"),
                    "QUANTIUTA", "QUANTITA", "QTA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    getTerapiaPayloadValue(terapia, "frequenzaUnita", "frequenza"), "FREQUENZA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    getTerapiaPayloadValue(terapia, "frequenzaUnita", "frequenza"), "FREQUENZA_UNITA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    parseBigDecimal(getTerapiaPayloadValue(terapia, "durataValore", "durata"), "durata"),
                    "DURATA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    parseBigDecimal(getTerapiaPayloadValue(terapia, "durataValore", "durata"), "durata"),
                    "DURATA_VALORE");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns,
                    getTerapiaPayloadValue(terapia, "durataUnita"), "UNITA", "DURATA_UNITA");
            addTerapiaInsertColumn(insertColumns, insertValues, tableColumns, safe(username), "UTENTE_INS", "INS_UTENTE");

            if (insertColumns.isEmpty()) {
                continue;
            }

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO APO_SCHEDE_PAZIENTE_TERAPIE (");
            for (int index = 0; index < insertColumns.size(); index += 1) {
                if (index > 0) {
                    sql.append(", ");
                }
                sql.append(insertColumns.get(index));
            }
            sql.append(") VALUES (");
            for (int index = 0; index < insertColumns.size(); index += 1) {
                if (index > 0) {
                    sql.append(", ");
                }
                sql.append("?");
            }
            sql.append(")");

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int index = 0; index < insertValues.size(); index += 1) {
                    bindTerapiaInsertValue(statement, index + 1,
                            insertColumns.get(index), insertValues.get(index));
                }
                statement.executeUpdate();
                existing.put(key, key);
            }
        }
    }

    private void insertSchedaPazienteDiario(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> payload, String username) throws SQLException {
        String diario = safe(payload.get("schedaDiario"));
        if (idScheda == null || diario.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO APO_SCHEDE_PAZIENTE_DIARIO ("
                + "ID_SCHEDA, COD_PAZ, DATA_DIARIO, DESCRIZIONE, UTENTE_INS"
                + ") VALUES (?, ?, SYSDATE, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, idScheda);
            statement.setString(2, safe(codPaz));
            statement.setString(3, diario);
            statement.setString(4, safe(username));
            statement.executeUpdate();
        }
    }

    private BigDecimal findLatestSchedaPazienteId(Connection connection, String codPaz) throws SQLException {
        String sql = "SELECT ID FROM ("
                + "SELECT ID FROM APO_SCHEDE_PAZIENTE "
                + "WHERE TRIM(CAST(COD_PAZ AS VARCHAR2(100))) = ? "
                + "ORDER BY DATA_INS DESC NULLS LAST, ID DESC"
                + ") WHERE ROWNUM = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("ID");
                }
            }
        }
        return null;
    }

    private Map<String, String> loadSchedaPazientePatologie(Connection connection, BigDecimal idScheda,
            String codPaz) throws SQLException {
        LinkedHashMap<String, String> existing = new LinkedHashMap<>();
        String sql = "SELECT CODICE_PATOLOGIA FROM APO_SCHEDE_PAZIENTE_PATOLOGIE "
                + "WHERE ID_SCHEDA = ? AND TRIM(CAST(COD_PAZIENTE AS VARCHAR2(100))) = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, idScheda);
            statement.setString(2, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String codice = safe(rs.getString("CODICE_PATOLOGIA"));
                    String key = normalizePatologiaKey(codice);
                    if (!key.isEmpty()) {
                        existing.put(key, codice);
                    }
                }
            }
        }
        return existing;
    }

    private Map<String, String> loadSchedaPazienteAllergie(Connection connection, BigDecimal idScheda,
            String codPaz) throws SQLException {
        LinkedHashMap<String, String> existing = new LinkedHashMap<>();
        String sql = "SELECT CODICE_ALLERGIA FROM APO_SCHEDE_PAZIENTE_ALLERGIE "
                + "WHERE ID_SCHEDA = ? AND TRIM(CAST(COD_PAZIENTE AS VARCHAR2(100))) = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, idScheda);
            statement.setString(2, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String codice = safe(rs.getString("CODICE_ALLERGIA"));
                    String key = normalizePatologiaKey(codice);
                    if (!key.isEmpty()) {
                        existing.put(key, codice);
                    }
                }
            }
        }
        return existing;
    }

    private Map<String, String> loadSchedaPazienteTerapie(Connection connection, BigDecimal idScheda,
            String codPaz, Map<String, String> tableColumns) throws SQLException {
        LinkedHashMap<String, String> existing = new LinkedHashMap<>();
        String patientColumn = firstExistingColumn(tableColumns, "COD_PAZIENTE", "COD_PAZ");
        if (idScheda == null || patientColumn == null) {
            return existing;
        }

        String sql = "SELECT * FROM APO_SCHEDE_PAZIENTE_TERAPIE "
                + "WHERE ID_SCHEDA = ? "
                + "AND TRIM(CAST(" + patientColumn + " AS VARCHAR2(100))) = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, idScheda);
            statement.setString(2, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> terapia = new HashMap<>();
                    terapia.put("principioAttivo", getResultColumnValue(rs, tableColumns, "PRINCIPIO_ATTIVO", "PRINC_ATTIVO", "PA"));
                    terapia.put("farmaco", getResultColumnValue(rs, tableColumns, "FARMACO"));
                    terapia.put("confezione", getResultColumnValue(rs, tableColumns, "CONFEZIONE"));
                    terapia.put("quantita", getResultColumnValue(rs, tableColumns, "QUANTIUTA", "QUANTITA", "QTA"));
                    terapia.put("frequenzaUnita", firstNonEmpty(
                            getResultColumnValue(rs, tableColumns, "FREQUENZA_UNITA"),
                            getResultColumnValue(rs, tableColumns, "FREQUENZA")));
                    terapia.put("durataValore", firstNonEmpty(
                            getResultColumnValue(rs, tableColumns, "DURATA_VALORE"),
                            getResultColumnValue(rs, tableColumns, "DURATA")));
                    String durataUnita = firstNonEmpty(
                            getResultColumnValue(rs, tableColumns, "UNITA"),
                            getResultColumnValue(rs, tableColumns, "DURATA_UNITA"));
                    terapia.put("durataUnita", durataUnita);
                    terapia.put("unita", durataUnita);

                    String key = buildSchedaTerapiaKey(terapia);
                    if (!key.isEmpty()) {
                        existing.put(key, key);
                    }
                }
            }
        }
        return existing;
    }

    private void deleteRemovedSchedaPazientePatologie(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> existing, Map<String, String> selected) throws SQLException {
        if (existing.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM APO_SCHEDE_PAZIENTE_PATOLOGIE "
                + "WHERE ID_SCHEDA = ? "
                + "AND TRIM(CAST(COD_PAZIENTE AS VARCHAR2(100))) = ? "
                + "AND UPPER(TRIM(CODICE_PATOLOGIA)) = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean hasBatch = false;
            for (String existingKey : existing.keySet()) {
                if (selected.containsKey(existingKey)) {
                    continue;
                }
                statement.setBigDecimal(1, idScheda);
                statement.setString(2, safe(codPaz));
                statement.setString(3, existingKey);
                statement.addBatch();
                hasBatch = true;
            }
            if (hasBatch) {
                statement.executeBatch();
            }
        }
    }

    private void deleteRemovedSchedaPazienteAllergie(Connection connection, BigDecimal idScheda, String codPaz,
            Map<String, String> existing, Map<String, String> selected) throws SQLException {
        if (existing.isEmpty()) {
            return;
        }

        String sql = "DELETE FROM APO_SCHEDE_PAZIENTE_ALLERGIE "
                + "WHERE ID_SCHEDA = ? "
                + "AND TRIM(CAST(COD_PAZIENTE AS VARCHAR2(100))) = ? "
                + "AND UPPER(TRIM(CODICE_ALLERGIA)) = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            boolean hasBatch = false;
            for (String existingKey : existing.keySet()) {
                if (selected.containsKey(existingKey)) {
                    continue;
                }
                statement.setBigDecimal(1, idScheda);
                statement.setString(2, safe(codPaz));
                statement.setString(3, existingKey);
                statement.addBatch();
                hasBatch = true;
            }
            if (hasBatch) {
                statement.executeBatch();
            }
        }
    }

    private String normalizePatologiaKey(String value) {
        return safe(value).toUpperCase(Locale.ITALY);
    }

    private void setNullableInteger(PreparedStatement statement, int parameterIndex, String value,
            String fieldName) throws SQLException {
        Integer numberValue = parseInteger(value, fieldName);
        if (numberValue == null) {
            statement.setNull(parameterIndex, Types.NUMERIC);
            return;
        }
        statement.setInt(parameterIndex, numberValue.intValue());
    }

    private void setNullableBigDecimal(PreparedStatement statement, int parameterIndex, String value,
            String fieldName) throws SQLException {
        BigDecimal numberValue = parseBigDecimal(value, fieldName);
        if (numberValue == null) {
            statement.setNull(parameterIndex, Types.NUMERIC);
            return;
        }
        statement.setBigDecimal(parameterIndex, numberValue);
    }

    private Integer parseInteger(String value, String fieldName) throws SQLException {
        String normalizedValue = safe(value);
        if (normalizedValue.isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(normalizedValue);
        } catch (NumberFormatException ex) {
            throw new SQLException("Valore numerico non valido per " + safe(fieldName) + ": " + normalizedValue, ex);
        }
    }

    private BigDecimal parseBigDecimal(String value, String fieldName) throws SQLException {
        String normalizedValue = safe(value).replace(',', '.');
        if (normalizedValue.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(normalizedValue);
        } catch (NumberFormatException ex) {
            throw new SQLException("Valore numerico non valido per " + safe(fieldName) + ": " + normalizedValue, ex);
        }
    }

    private List<String> parseSchedaPatologie(String value) {
        ArrayList<String> codiciPatologia = new ArrayList<>();
        String normalizedValue = safe(value);
        if (normalizedValue.isEmpty()) {
            return codiciPatologia;
        }

        String[] parts = normalizedValue.split("\\|");
        for (String part : parts) {
            String codicePatologia = safe(part);
            if (!codicePatologia.isEmpty() && !codiciPatologia.contains(codicePatologia)) {
                codiciPatologia.add(codicePatologia);
            }
        }
        return codiciPatologia;
    }

    private List<Map<String, String>> parseSchedaTerapie(String value) throws SQLException {
        String normalizedValue = safe(value);
        if (normalizedValue.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<Map<String, String>> terapie = GSON.fromJson(normalizedValue, TERAPIE_PAYLOAD_TYPE);
            return terapie == null ? new ArrayList<Map<String, String>>() : terapie;
        } catch (RuntimeException ex) {
            throw new SQLException("Terapie in corso non valide.", ex);
        }
    }

    private String buildSchedaTerapiaKey(Map<String, String> terapia) {
        return normalizePatologiaKey(getTerapiaPayloadValue(terapia, "principioAttivo", "principio_attivo", "pa"))
                + "|" + normalizePatologiaKey(getTerapiaPayloadValue(terapia, "farmaco"))
                + "|" + normalizePatologiaKey(getTerapiaPayloadValue(terapia, "confezione"))
                + "|" + normalizePatologiaKey(getTerapiaPayloadValue(terapia, "quantita", "qta"))
                + "|" + normalizePatologiaKey(getTerapiaPayloadValue(terapia, "frequenzaUnita", "frequenza"))
                + "|" + normalizePatologiaKey(buildDurataTerapia(
                        getTerapiaPayloadValue(terapia, "durataValore", "durata"),
                        getTerapiaPayloadValue(terapia, "durataUnita")));
    }

    private String getTerapiaPayloadValue(Map<String, String> terapia, String... keys) {
        if (terapia == null || keys == null) {
            return "";
        }

        for (String key : keys) {
            String value = safe(terapia.get(key));
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String normalizeFumoValue(String value) {
        String normalizedValue = safe(value);
        if (normalizedValue.isEmpty()) {
            return "";
        }
        if (isTruthy(normalizedValue) || "s".equalsIgnoreCase(normalizedValue)) {
            return "1";
        }
        if ("0".equals(normalizedValue)
                || "false".equalsIgnoreCase(normalizedValue)
                || "no".equalsIgnoreCase(normalizedValue)
                || "n".equalsIgnoreCase(normalizedValue)
                || "f".equalsIgnoreCase(normalizedValue)) {
            return "0";
        }
        return normalizedValue;
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index += 1) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    private void bindInsertValue(PreparedStatement statement, int parameterIndex, Object value) throws SQLException {
        if (value instanceof Timestamp) {
            statement.setTimestamp(parameterIndex, (Timestamp) value);
            return;
        }
        if (value instanceof Integer) {
            statement.setInt(parameterIndex, ((Integer) value).intValue());
            return;
        }
        statement.setString(parameterIndex, value == null ? "" : String.valueOf(value).trim());
    }

    private String normalizeDateOperator(String operator) {
        String normalized = safe(operator);
        if ("<".equals(normalized) || ">".equals(normalized) || "<=".equals(normalized) || ">=".equals(normalized)) {
            return normalized;
        }
        if ("!=".equals(normalized) || "<>".equals(normalized)) {
            return "<>";
        }
        return "=";
    }

    private String likeValue(String value) {
        return "%" + safe(value).toUpperCase(Locale.ITALY) + "%";
    }

    private String buildAccessoId(String codConsulenze, String dataSortKey, String codFiscale, int rowIndex) {
        return safe(codConsulenze) + "|" + safe(dataSortKey) + "|" + safe(codFiscale) + "|" + rowIndex;
    }

    private String normalizeDisplayDate(String rawValue) {
        String value = safe(rawValue);
        if (value.isEmpty()) {
            return "";
        }

        if (value.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            return value;
        }
        if (value.matches("^\\d{4}-\\d{2}-\\d{2}.*$")) {
            return value.substring(8, 10) + "/" + value.substring(5, 7) + "/" + value.substring(0, 4);
        }

        String cleaned = value.replace(',', ' ').replaceAll("\\s+", " ").trim();
        for (DateTimeFormatter formatter : DATE_PARSERS) {
            try {
                TemporalAccessor parsed = formatter.parse(cleaned);
                LocalDate date = LocalDate.from(parsed);
                return date.format(DISPLAY_DATE_FORMATTER);
            } catch (DateTimeParseException ignore) {
            }
        }

        return value;
    }

    private List<String> getConsulenzeGmColumns(Connection connection) throws SQLException {
        ArrayList<String> columns = new ArrayList<>();
        String sql = "SELECT * FROM CONSULENZE_GM WHERE 1 = 0";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int index = 1; index <= metaData.getColumnCount(); index += 1) {
                String columnName = safe(metaData.getColumnLabel(index));
                if (columnName.isEmpty()) {
                    columnName = safe(metaData.getColumnName(index));
                }
                if (!columnName.isEmpty()) {
                    columns.add(columnName.toUpperCase(Locale.ITALY));
                }
            }
        }

        return columns;
    }

    private Map<String, String> findTableColumns(Connection connection, String tableName) throws SQLException {
        HashMap<String, String> columns = new HashMap<>();
        String sql = "SELECT * FROM " + tableName + " WHERE 1 = 0";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            ResultSetMetaData metaData = rs.getMetaData();
            for (int index = 1; index <= metaData.getColumnCount(); index += 1) {
                String columnName = safe(metaData.getColumnLabel(index));
                if (columnName.isEmpty()) {
                    columnName = safe(metaData.getColumnName(index));
                }
                if (!columnName.isEmpty()) {
                    columns.put(columnName.toUpperCase(Locale.ITALY), columnName);
                }
            }
        }

        return columns;
    }

    private String firstExistingColumn(Map<String, String> columns, String... candidates) {
        if (columns == null || candidates == null) {
            return null;
        }

        for (String candidate : candidates) {
            String columnName = columns.get(safe(candidate).toUpperCase(Locale.ITALY));
            if (columnName != null) {
                return columnName;
            }
        }
        return null;
    }

    private String getResultColumnValue(ResultSet rs, Map<String, String> columns, String... candidates)
            throws SQLException {
        String columnName = firstExistingColumn(columns, candidates);
        return columnName == null ? "" : safe(rs.getString(columnName));
    }

    private void addTerapiaInsertColumn(List<String> insertColumns, List<Object> insertValues,
            Map<String, String> tableColumns, Object value, String... candidates) {
        String columnName = firstExistingColumn(tableColumns, candidates);
        if (columnName == null || insertColumns.contains(columnName)) {
            return;
        }

        insertColumns.add(columnName);
        insertValues.add(value);
    }

    private void bindTerapiaInsertValue(PreparedStatement statement, int parameterIndex,
            String columnName, Object value) throws SQLException {
        if (value instanceof BigDecimal) {
            statement.setBigDecimal(parameterIndex, (BigDecimal) value);
            return;
        }
        if (value == null && isTerapiaNumericColumn(columnName)) {
            statement.setNull(parameterIndex, Types.NUMERIC);
            return;
        }
        statement.setString(parameterIndex, value == null ? "" : String.valueOf(value).trim());
    }

    private boolean isTerapiaNumericColumn(String columnName) {
        String normalized = safe(columnName).toUpperCase(Locale.ITALY);
        return "ID_SCHEDA".equals(normalized)
                || "QUANTIUTA".equals(normalized)
                || "QUANTITA".equals(normalized)
                || "QTA".equals(normalized)
                || "DURATA".equals(normalized)
                || "DURATA_VALORE".equals(normalized);
    }

    private String buildDurataTerapia(String durataValore, String durataUnita) {
        return firstNonEmpty((safe(durataValore) + " " + safe(durataUnita)).trim(),
                safe(durataValore),
                safe(durataUnita));
    }

    private int loadNextCodConsulenze(Connection connection) throws SQLException {
        String sql = "SELECT NVL(MAX(COD_CONSULENZE), 0) + 1 AS NEXT_COD FROM CONSULENZE_GM";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("NEXT_COD");
            }
        }

        return 1;
    }

    private void putIfColumn(LinkedHashMap<String, Object> values, List<String> columns, String columnName, Object value) {
        if (columns == null || !columns.contains(columnName) || value == null) {
            return;
        }
        if (value instanceof String && !hasText((String) value)) {
            return;
        }
        values.put(columnName, value);
    }

    private Timestamp parseTimestamp(String value) {
        String normalizedValue = safe(value);
        if (normalizedValue.isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_TIME_PARSERS) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(normalizedValue, formatter);
                return Timestamp.valueOf(parsed);
            } catch (DateTimeParseException ignore) {
            }
        }

        throw new IllegalArgumentException("Data e ora non valide: " + normalizedValue);
    }

    private String formatOrarioConsulenza(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }

        LocalDateTime dateTime = timestamp.toLocalDateTime();
        return String.format(Locale.ITALY, "%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }

    private String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }

        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isTruthy(String value) {
        String normalizedValue = safe(value);
        return "1".equals(normalizedValue)
                || "true".equalsIgnoreCase(normalizedValue)
                || "yes".equalsIgnoreCase(normalizedValue)
                || "si".equalsIgnoreCase(normalizedValue);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static DateTimeFormatter formatter(String pattern, Locale locale) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(locale);
    }
}
