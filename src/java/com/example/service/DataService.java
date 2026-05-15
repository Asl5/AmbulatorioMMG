package com.example.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataService {

    private final String jndiName;
    private final String dbDriver;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DataService(String jndiName, String dbDriver, String dbUrl, String dbUser, String dbPassword) {
        this.jndiName = jndiName;
        this.dbDriver = dbDriver;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public Connection getConnection() throws SQLException {
        DataSource ds = lookupDataSource();
        if (ds != null) {
            return ds.getConnection();
        }

        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new SQLException("Datasource JNDI non trovato e dbUrl non configurato");
        }

        if (dbDriver != null && !dbDriver.trim().isEmpty()) {
            try {
                Class.forName(dbDriver);
            } catch (ClassNotFoundException ex) {
                throw new SQLException("Driver JDBC non trovato: " + dbDriver, ex);
            }
        }

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    private DataSource lookupDataSource() {
        if (jndiName == null || jndiName.trim().isEmpty()) {
            return null;
        }

        String[] candidates = buildLookupCandidates(jndiName);
        try {
            InitialContext ctx = new InitialContext();
            for (String candidate : candidates) {
                if (candidate == null || candidate.trim().isEmpty()) {
                    continue;
                }
                Object obj = ctx.lookup(candidate);
                if (obj instanceof DataSource) {
                    return (DataSource) obj;
                }
            }
        } catch (NamingException ignore) {
        }
        return null;
    }

    private String[] buildLookupCandidates(String configuredName) {
        String trimmed = configuredName == null ? "" : configuredName.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }

        if (trimmed.startsWith("java:comp/env/")) {
            return new String[]{trimmed, trimmed.substring("java:comp/env/".length())};
        }

        if (trimmed.startsWith("jdbc/")) {
            return new String[]{trimmed, "java:comp/env/" + trimmed};
        }

        return new String[]{trimmed, "java:comp/env/" + trimmed};
    }
}
