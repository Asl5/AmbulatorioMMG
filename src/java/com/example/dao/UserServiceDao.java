package com.example.dao;

import com.example.service.DataService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String sql = "SELECT DISTINCT U.COD_SERVIZIO, S.DESCRIZIONE "
                    + "FROM TESTFISIATRIAWEB.UTENTI U "
                    + "JOIN TESTFISIATRIAWEB.SERVIZI S ON U.COD_SERVIZIO = S.COD_SERVIZIO "
                    + "WHERE UPPER(U.COD_UTENTE) = UPPER(?) "
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
                + "FROM SERVIZI "
                + "WHERE COD_SERVIZIO LIKE 'APO_%' "
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
