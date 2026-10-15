package com.example.dao;

import com.example.service.DataService;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServiceDao {

    private final DataService dataService;

    public UserServiceDao(DataService dataService) {
        this.dataService = dataService;
    }

    public List<Map<String, String>> getServiziByUser(String username) throws SQLException {
        String sql = "SELECT DISTINCT U.COD_SERVIZIO, S.DESCRIZIONE, S.IP_MINIMO, S.IP_MASSIMO "
                    + "FROM TESTFISIATRIAWEB.UTENTI U "
                    + "JOIN TESTFISIATRIAWEB.SERVIZI S ON U.COD_SERVIZIO = S.COD_SERVIZIO "
                    + "WHERE UPPER(U.COD_UTENTE) = UPPER(?) "
                    + "AND SUBSTR(UPPER(TRIM(U.COD_SERVIZIO)), 1, 4) = 'APO_' "
                    + "AND S.A_DATA IS NULL "
                    + "ORDER BY S.DESCRIZIONE, U.COD_SERVIZIO";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> service = new HashMap<>();
                    service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                    service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                    service.put("ipMinimo", safe(rs.getString("IP_MINIMO")));
                    service.put("ipMassimo", safe(rs.getString("IP_MASSIMO")));
                    results.add(service);
                }
            }
        }
        return results;
    }

    public List<Map<String, String>> getAllActiveServicesByUser(String username) throws SQLException {
        String sql = "SELECT DISTINCT U.COD_SERVIZIO, S.DESCRIZIONE "
                + "FROM TESTFISIATRIAWEB.UTENTI U "
                + "JOIN TESTFISIATRIAWEB.SERVIZI S ON U.COD_SERVIZIO = S.COD_SERVIZIO "
                + "WHERE UPPER(U.COD_UTENTE) = UPPER(?) "
                + "AND S.A_DATA IS NULL "
                + "ORDER BY S.DESCRIZIONE, U.COD_SERVIZIO";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> service = new HashMap<>();
                    service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                    service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                    results.add(service);
                }
            }
        }
        return results;
    }

    public List<Map<String, String>> getApoServices() throws SQLException {
        String sql = "SELECT COD_SERVIZIO, DESCRIZIONE "
                + "FROM TESTFISIATRIAWEB.SERVIZI "
                + "WHERE SUBSTR(UPPER(TRIM(COD_SERVIZIO)), 1, 4) = 'APO_' "
                + "AND A_DATA IS NULL "
                + "ORDER BY DESCRIZIONE";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                HashMap<String, String> service = new HashMap<>();
                service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                results.add(service);
            }
        }
        return results;
    }

    public int enableApoUser(String matricola, String cognome, String nome, String codiceFiscale,
            String codGruppo, String codQualifica, String pincode, String codSpec,
            List<String> codServizi) throws SQLException {
        StringBuilder concatenatedServices = new StringBuilder();
        for (String codServizio : codServizi) {
            if (concatenatedServices.length() > 0) {
                concatenatedServices.append('-');
            }
            concatenatedServices.append(safe(codServizio));
        }

        String procedureSql = "BEGIN "
                + "TESTFISIATRIAWEB.ABILITAZIONE_UTENTE_APO(?, ?, ?, ?, ?, ?, ?, ?, ?); "
                + "COMMIT; "
                + "END;";
        try (Connection conn = dataService.getConnection();
             CallableStatement statement = conn.prepareCall(procedureSql)) {
            statement.setString(1, safe(matricola));
            statement.setString(2, safe(cognome));
            statement.setString(3, safe(nome));
            statement.setString(4, safe(codiceFiscale));
            statement.setString(5, safe(codGruppo).toUpperCase());
            statement.setString(6, safe(codQualifica).toUpperCase());
            setNullableString(statement, 7, pincode);
            setNullableString(statement, 8, codSpec);
            statement.setString(9, concatenatedServices.toString());
            statement.execute();
        }
        return codServizi.size();
    }

    private void setNullableString(CallableStatement statement, int parameterIndex, String value)
            throws SQLException {
        String normalizedValue = safe(value);
        if (normalizedValue.isEmpty()) {
            statement.setNull(parameterIndex, Types.VARCHAR);
            return;
        }
        statement.setString(parameterIndex, normalizedValue);
    }

    public List<Map<String, String>> getApoServicesByUser(String username) throws SQLException {
        String sql = "SELECT DISTINCT U.COD_SERVIZIO, S.DESCRIZIONE "
                + "FROM TESTFISIATRIAWEB.UTENTI U "
                + "JOIN TESTFISIATRIAWEB.SERVIZI S ON U.COD_SERVIZIO = S.COD_SERVIZIO "
                + "WHERE UPPER(U.COD_UTENTE) = UPPER(?) "
                + "AND SUBSTR(UPPER(TRIM(U.COD_SERVIZIO)), 1, 4) = 'APO_' "
                + "AND S.A_DATA IS NULL "
                + "ORDER BY S.DESCRIZIONE, U.COD_SERVIZIO";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> service = new HashMap<>();
                    service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                    service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                    results.add(service);
                }
            }
        }
        return results;
    }

    public Map<String, String> getApoServiceByCode(String codServizio) throws SQLException {
        String sql = "SELECT COD_SERVIZIO, DESCRIZIONE "
                + "FROM SERVIZI "
                + "WHERE UPPER(COD_SERVIZIO) = UPPER(?) "
                + "AND COD_SERVIZIO LIKE 'APO_%' "
                + "AND A_DATA IS NULL";

        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codServizio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                HashMap<String, String> service = new HashMap<>();
                service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                return service;
            }
        }
    }

    public Map<String, String> getApoServiceByUser(String username, String codServizio) throws SQLException {
        String sql = "SELECT DISTINCT U.COD_SERVIZIO, S.DESCRIZIONE "
                + "FROM TESTFISIATRIAWEB.UTENTI U "
                + "JOIN TESTFISIATRIAWEB.SERVIZI S ON U.COD_SERVIZIO = S.COD_SERVIZIO "
                + "WHERE UPPER(U.COD_UTENTE) = UPPER(?) "
                + "AND UPPER(U.COD_SERVIZIO) = UPPER(?) "
                + "AND SUBSTR(UPPER(TRIM(U.COD_SERVIZIO)), 1, 4) = 'APO_' "
                + "AND S.A_DATA IS NULL";

        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, codServizio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                HashMap<String, String> service = new HashMap<>();
                service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                return service;
            }
        }
    }

    public Map<String, String> getServiceByUser(String username, String codServizio) throws SQLException {
        String sql = "SELECT DISTINCT U.COD_SERVIZIO, S.DESCRIZIONE "
                + "FROM TESTFISIATRIAWEB.UTENTI U "
                + "JOIN TESTFISIATRIAWEB.SERVIZI S ON U.COD_SERVIZIO = S.COD_SERVIZIO "
                + "WHERE UPPER(U.COD_UTENTE) = UPPER(?) "
                + "AND UPPER(U.COD_SERVIZIO) = UPPER(?)";

        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, codServizio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                HashMap<String, String> service = new HashMap<>();
                service.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
                service.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                return service;
            }
        }
    }

    public Map<String, String> getUserDataByUsername(String username, String codServizio) throws SQLException {
        String sql = "SELECT COD_FISCALE, COD_QUALIFICA, COD_GRUPPO "
                + "FROM TESTFISIATRIAWEB.UTENTI "
                + "WHERE UPPER(COD_UTENTE) = UPPER(?) "
                + "AND UPPER(COD_SERVIZIO) = UPPER(?) "
                + "AND ROWNUM = 1";

        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, codServizio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new HashMap<>();
                }

                HashMap<String, String> userData = new HashMap<>();
                userData.put("codFiscale", safe(rs.getString("COD_FISCALE")));
                userData.put("codQualifica", safe(rs.getString("COD_QUALIFICA")));
                userData.put("codGruppo", safe(rs.getString("COD_GRUPPO")));
                return userData;
            }
        }
    }

    public void insertApoUserAccess(String codUtente, String codServizio) throws SQLException {
        String sql = "INSERT INTO APO_UTENTI_ACCESSI (COD_UTENTE, COD_SERVIZIO) VALUES (?, ?)";
        String sql_up = "UPDATE UTENTI SET ULTIMO_ACCESSO=SYSDATE WHERE UPPER(COD_UTENTE)=UPPER(?) AND COD_SERVIZIO=?";

        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             PreparedStatement ps_up = conn.prepareStatement(sql_up)){
            ps.setString(1, safe(codUtente));
            ps.setString(2, safe(codServizio));
            ps_up.setString(1, safe(codUtente));
            ps_up.setString(2, safe(codServizio));
            ps.executeUpdate();
            ps_up.executeUpdate();
        }
    }

    public void insertApoActionLog(String codUtente, String codServizio,
            String codConsulenza, String azione, String parametri) throws SQLException {
        String sql = "INSERT INTO APO_LOG_AZIONI "
                + "(COD_UTENTE, COD_SERVIZIO, COD_CONSULENZA, AZIONE, PARAMETRI) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, safe(codUtente));
            ps.setString(2, safe(codServizio));
            ps.setString(3, safe(codConsulenza));
            ps.setString(4, safe(azione));
            ps.setString(5, safe(parametri));
            ps.executeUpdate();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
