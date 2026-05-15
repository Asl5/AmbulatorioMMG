package com.example.dao;

import com.example.service.DataService;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PazienteDao {

    private static final String[] ALLEGATO_M_EDITABLE_COLUMNS = {
        "EMAIL", "TELEFONO", "MOT_CIRC", "ALTRO", "TERAPIA", "ESITO_INTERV",
        "SN_INDENNE", "SN_COSCIENZA", "SN_PERDITA", "SN_STATO", "SN_CONVULSIONI",
        "SN_ROMBERG", "SN_RIGOR", "SN_DEF_MOT", "SN_DEF_SEN", "SN_DEVIAZIONE",
        "PLL_NORM_SX", "PLL_NORM_DX", "PLL_MIO_SX", "PLL_MIO_DX", "PLL_MID_SX",
        "PLL_MID_DX", "PLL_FOT_SX", "PLL_FOT_DX", "PLL_NISTAGMO",
        "APPCC_NORMALE", "APPCC_ARITMIA", "APPCC_CIANOSI", "APPCC_EDEMI",
        "APPRESP_INDENNE", "APPRESP_RUM", "APPRESP_RUM_S", "APPRESP_ENFISEMA",
        "ADD_MURPHY", "ADD_BLUEM", "ADD_ROV", "ADD_GIO_SX", "ADD_GIO_DX",
        "ADD_ASCITE", "DIFFERIBILE", "MMG", "PS", "COT", "INV118", "INV188"
    };
    private static final String ALLEGATO_M_NUMERIC_COLUMNS = "|SN_INDENNE|SN_COSCIENZA|SN_PERDITA|SN_STATO|"
            + "SN_CONVULSIONI|SN_ROMBERG|SN_RIGOR|SN_DEF_MOT|SN_DEF_SEN|SN_DEVIAZIONE|"
            + "PLL_NORM_SX|PLL_NORM_DX|PLL_MIO_SX|PLL_MIO_DX|PLL_MID_SX|PLL_MID_DX|"
            + "PLL_FOT_SX|PLL_FOT_DX|PLL_NISTAGMO|APPCC_NORMALE|APPCC_ARITMIA|"
            + "APPCC_CIANOSI|APPCC_EDEMI|APPRESP_INDENNE|APPRESP_RUM|APPRESP_RUM_S|"
            + "APPRESP_ENFISEMA|ADD_MURPHY|ADD_BLUEM|ADD_ROV|ADD_GIO_SX|ADD_GIO_DX|"
            + "ADD_ASCITE|DIFFERIBILE|MMG|PS|COT|INV118|INV188|";
    private static final String ANAGRAFE_BUFFER_SERVICE = "ANAGRAFE";
    private static final String ANAGRAFE_BUFFER_WORKSTATION = "TOPAMBWEB";

    private final DataService dataService;

    public PazienteDao(DataService dataService) {
        this.dataService = dataService;
    }

    public Map<String, String> findMedicoCurante(String codiceFiscale) throws SQLException {
        String sql = "SELECT "
                + "vvcm.pin_azienda, aim.matricola as MED_MATRICOLA, aim.nome as MED_NOME, aim.cognome as MED_COGNOME, aim.cod_Fiscale CF_MEDICO "
                + "from v_max_var_assistiti@anacon vvcm left join v_medici@anacon aim on vvcm.pin_azienda_med = aim.pin_azienda "
                + "where upper(vvcm.cod_fiscale) = upper(?)";

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapMedicoCurante(rs);
                }
            }
        }

        return new HashMap<>();
    }

    public String findConsensoDseHtml(String codiceFiscale) throws SQLException {
        String sql = "SELECT topgamweb.stato_dse(?) AS HTML FROM dual";

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return safe(rs.getString("HTML"));
                }
            }
        }

        return "";
    }

    public List<Map<String, String>> searchAnagrafe(String codiceFiscale, String cognome, String nome) throws SQLException {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        sql.append("SELECT * FROM VIEW_ANAGRAFE_DETTAGLIO_GM WHERE 1 = 1 ");

        if (hasText(codiceFiscale)) {
            sql.append("AND UPPER(TRIM(CAST(COD_FISCALE AS VARCHAR2(32)))) LIKE ? ");
            parameters.add(likeValue(codiceFiscale));
        }
        if (hasText(cognome)) {
            sql.append("AND UPPER(TRIM(CAST(COGNOME AS VARCHAR2(255)))) LIKE ? ");
            parameters.add(likeValue(cognome));
        }
        if (hasText(nome)) {
            sql.append("AND UPPER(TRIM(CAST(NOME AS VARCHAR2(255)))) LIKE ? ");
            parameters.add(likeValue(nome));
        }

        sql.append("ORDER BY COGNOME ASC NULLS LAST, NOME ASC NULLS LAST, DATA_NASCITA DESC NULLS LAST");

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapAnagrafePaziente(rs));
                }
            }
        }

        return results;
    }

    public List<Map<String, String>> loadComuniSuggerimenti() throws SQLException {
        String sql = "SELECT COD_COMUNE, DES_COMUNE FROM SELEZIONA_COMUNE ORDER BY DES_COMUNE";
        ArrayList<Map<String, String>> results = new ArrayList<>();

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                HashMap<String, String> comune = new HashMap<>();
                comune.put("codComune", safe(rs.getString("COD_COMUNE")));
                comune.put("desComune", safe(rs.getString("DES_COMUNE")));
                results.add(comune);
            }
        }

        return results;
    }

    public List<Map<String, String>> loadAslSuggerimenti() throws SQLException {
        String sql = "SELECT COD_ASL, REGIONE || ' | ' || PROVINCIA || ' | (' || COD_ASL || ') | ' || DESCRIZIONE DESCRIZIONE "
                + "FROM SELEZIONA_ASL ORDER BY REGIONE, PROVINCIA, DESCRIZIONE";
        ArrayList<Map<String, String>> results = new ArrayList<>();

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                HashMap<String, String> asl = new HashMap<>();
                asl.put("codAsl", safe(rs.getString("COD_ASL")));
                asl.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                results.add(asl);
            }
        }

        return results;
    }

    public Map<String, String> createAnagrafePaziente(Map<String, String> payload,
            String username, String workstation, String serviceCode) throws SQLException {
        String codiceFiscale = safe(payload.get("codiceFiscale")).toUpperCase(Locale.ITALY);
        Date dataNascita = Date.valueOf(LocalDate.parse(safe(payload.get("dataNascita"))));

        try (Connection connection = dataService.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                if (isAnagrafeCodiceFiscalePresent(connection, codiceFiscale)) {
                    throw new SQLException("Paziente gia presente in anagrafe.");
                }

                int progressivo = insertPazienteBuffer(connection, payload, dataNascita,
                        codiceFiscale, username);
                if (progressivo <= 0) {
                    throw new SQLException("Errore inserimento paziente in anagrafe buffer.");
                }

                insertPazienteStoricaBuffer(connection, progressivo, payload, codiceFiscale, username);
                if (registraPazienteBuffer(connection, progressivo) != 1) {
                    throw new SQLException("Errore procedura inserimento paziente in anagrafe.");
                }
                connection.commit();

                Map<String, String> paziente = findAnagrafeByCodiceFiscale(connection, codiceFiscale);
                if (paziente == null || paziente.isEmpty()) {
                    String comuneResidenza = safe(payload.get("codComuneResidenza"));
                    paziente = new HashMap<>();
                    paziente.put("id", codiceFiscale);
                    paziente.put("codFiscale", codiceFiscale);
                    paziente.put("codPaz", findBufferCodPaz(connection, progressivo));
                    paziente.put("cognome", safe(payload.get("cognome")));
                    paziente.put("nome", safe(payload.get("nome")));
                    paziente.put("paziente", (safe(payload.get("cognome")) + " " + safe(payload.get("nome"))).trim());
                    paziente.put("dataNascita", safe(payload.get("dataNascita")));
                    paziente.put("sesso", safe(payload.get("sesso")));
                    paziente.put("codComuneRes", comuneResidenza);
                    paziente.put("pin", "");
                }
                return paziente;
            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException ignore) {
                }
                throw ex;
            } finally {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignore) {
                }
            }
        }
    }

    public String calculateCodiceFiscale(String cognome, String nome, String dataNascita,
            String sesso, String comuneNascita) throws SQLException {
        LocalDate birthDate = LocalDate.parse(safe(dataNascita));
        String normalizedSesso = safe(sesso).toUpperCase(Locale.ITALY);
        if (!"M".equals(normalizedSesso) && !"F".equals(normalizedSesso)) {
            throw new IllegalArgumentException("Sesso non valido.");
        }

        try (Connection connection = dataService.getConnection()) {
            String codiceComune = resolveComuneFiscalCode(connection, comuneNascita, "Comune nascita", true);
            if (codiceComune.length() < 4) {
                throw new SQLException("Codice catastale del comune nascita non disponibile.");
            }

            String partial = fiscalCodeNamePart(cognome, false)
                    + fiscalCodeNamePart(nome, true)
                    + String.format(Locale.ITALY, "%02d", Integer.valueOf(birthDate.getYear() % 100))
                    + fiscalCodeMonthCode(birthDate.getMonthValue())
                    + fiscalCodeDayCode(birthDate.getDayOfMonth(), normalizedSesso)
                    + codiceComune.substring(0, 4).toUpperCase(Locale.ITALY);
            return partial + fiscalCodeCheckChar(partial);
        }
    }

    public List<Map<String, String>> findPrestazioniInfermieristiche(String codiceFiscale) throws SQLException {
        String sql = "SELECT "
                + "TRIM(CAST(PEA.ID AS VARCHAR2(100))) AS ID, "
                + "TRIM(CAST(PEA.COD_SERVIZIO AS VARCHAR2(100))) AS COD_SERVIZIO, "
                + "TRIM(CAST(S.DESCRIZIONE AS VARCHAR2(255))) AS DESCRIZIONE, "
                + "TO_CHAR(PEA.DATA_INS, 'DD/MM/YYYY HH24:MI') AS DATA_INS, "
                + "TO_CHAR(PEA.DATA_INS, 'YYYY-MM-DD HH24:MI:SS') AS DATA_INS_SORT, "
                + "TRIM(CAST(PEA.OPERATORE AS VARCHAR2(100))) AS OPERATORE, "
                + "TRIM(CAST(U.COGNOME || ' ' || U.NOME AS VARCHAR2(255))) AS DESC_OPERATORE, "
                + "TRIM(CAST(PEA.ID_CONSULENZA AS VARCHAR2(100))) AS ID_CONSULENZA, "
                + "TRIM(CAST(PEA.ID_CATEGORIA AS VARCHAR2(100))) AS COD_PRESTAZIONE, "
                + "TRIM(CAST(PIA.CATEGORIA AS VARCHAR2(255))) AS CATEGORIA, "
                + "TRIM(CAST(PIA.SOTTO_CATEGORIA AS VARCHAR2(255))) AS SOTTO_CATEGORIA, "
                + "TRIM(CAST(PIA.CATEGORIA || ' - ' || PIA.SOTTO_CATEGORIA AS VARCHAR2(255))) AS PRESTAZIONE "
                + "FROM prestazioni_effettuate_apo PEA "
                + "LEFT JOIN prestazioni_inf_apo PIA ON PEA.id_categoria = PIA.id "
                + "JOIN SERVIZI S ON S.COD_SERVIZIO = PEA.COD_SERVIZIO "
                + "JOIN UTENTI U ON U.COD_UTENTE = PEA.OPERATORE AND U.COD_SERVIZIO = PEA.COD_SERVIZIO "
                + "WHERE UPPER(TRIM(CAST(PEA.COD_FISCALE_PAZ AS VARCHAR2(32)))) = UPPER(?) "
                + "AND PEA.TIPO = 'PRESTAZIONE INFERMIERISTICA' "
                + "ORDER BY PEA.ID DESC";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPrestazioneInfermieristica(rs));
                }
            }
        }

        return results;
    }

    public List<Map<String, String>> loadPrestazioniInfermieristicheCategorie(String codServizio) throws SQLException {
        String sql = "SELECT DISTINCT TRIM(CAST(CATEGORIA AS VARCHAR2(255))) AS CATEGORIA "
                + "FROM PRESTAZIONI_INF_APO "
                + "WHERE A_DATA IS NULL "
                + "AND COD_SERVIZIO = ? "
                + "ORDER BY CATEGORIA";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codServizio));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> categoria = new HashMap<>();
                    categoria.put("categoria", safe(rs.getString("CATEGORIA")));
                    results.add(categoria);
                }
            }
        }

        return results;
    }

    public List<Map<String, String>> loadPrestazioniInfermieristicheSottocategorie(String codServizio,
            String categoria) throws SQLException {
        String sql = "SELECT DISTINCT "
                + "TRIM(CAST(ID AS VARCHAR2(100))) AS ID, "
                + "TRIM(CAST(SOTTO_CATEGORIA AS VARCHAR2(255))) AS SOTTO_CATEGORIA "
                + "FROM PRESTAZIONI_INF_APO "
                + "WHERE A_DATA IS NULL "
                + "AND COD_SERVIZIO = ? "
                + "AND CATEGORIA = ? "
                + "ORDER BY SOTTO_CATEGORIA";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codServizio));
            statement.setString(2, safe(categoria));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> sottocategoria = new HashMap<>();
                    sottocategoria.put("id", safe(rs.getString("ID")));
                    sottocategoria.put("sottoCategoria", safe(rs.getString("SOTTO_CATEGORIA")));
                    results.add(sottocategoria);
                }
            }
        }

        return results;
    }

    public List<Map<String, String>> searchPatologieCronicheIcd10(String filtro) throws SQLException {
        String sql = "SELECT I1.ICD10 CODICE_CATEGORIA, I1.DESCRIZIONE CATEGORIA, "
                + "I2.CODICE, I2.DESCRIZIONE PATOLOGIA "
                + "FROM TOPSAN.ICD10 I1 JOIN TOPSAN.ICD10 I2 ON I1.ICD10 = I2.ICD10 "
                + "WHERE I2.SOTTOCATEGORIA IS NOT NULL AND I1.SOTTOCATEGORIA IS NULL "
                + "ORDER BY CODICE, I2.SOTTOCATEGORIA";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPatologiaCronicaIcd10(rs));
                }
            }
        }

        return results;
    }

    public Map<String, Object> findLatestSchedaPaziente(String codPaz) throws SQLException {
        String sql = "SELECT ID, COD_PAZ, TO_CHAR(DATA_INS, 'DD/MM/YYYY HH24:MI') DATA_INS, UTENTE_INS, "
                + "TELEFONO, STATO_CIVILE, FUMO, IBM, IBM_KG, IBM_CM, ALLERGIE, TERAPIE, CAREGIVER "
                + "FROM ("
                + "SELECT ID, COD_PAZ, DATA_INS, UTENTE_INS, TELEFONO, STATO_CIVILE, FUMO, IBM, IBM_KG, IBM_CM, "
                + "ALLERGIE, TERAPIE, CAREGIVER "
                + "FROM APO_SCHEDE_PAZIENTE "
                + "WHERE TRIM(CAST(COD_PAZ AS VARCHAR2(100))) = ? "
                + "ORDER BY DATA_INS DESC NULLS LAST, ID DESC"
                + ") WHERE ROWNUM = 1";

        HashMap<String, Object> scheda = null;
        BigDecimal idScheda = null;
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    idScheda = rs.getBigDecimal("ID");
                    scheda = new HashMap<>();
                    scheda.put("idScheda", safe(rs.getString("ID")));
                    scheda.put("codPaz", safe(rs.getString("COD_PAZ")));
                    scheda.put("dataIns", safe(rs.getString("DATA_INS")));
                    scheda.put("utenteIns", safe(rs.getString("UTENTE_INS")));
                    scheda.put("telefono", safe(rs.getString("TELEFONO")));
                    scheda.put("statoCivile", safe(rs.getString("STATO_CIVILE")));
                    scheda.put("fumo", safe(rs.getString("FUMO")));
                    scheda.put("ibm", safe(rs.getString("IBM")));
                    scheda.put("massIndex", safe(rs.getString("IBM")));
                    scheda.put("ibmKg", safe(rs.getString("IBM_KG")));
                    scheda.put("pesoKg", safe(rs.getString("IBM_KG")));
                    scheda.put("ibmCm", safe(rs.getString("IBM_CM")));
                    scheda.put("altezzaCm", safe(rs.getString("IBM_CM")));
                    scheda.put("allergie", safe(rs.getString("ALLERGIE")));
                    scheda.put("terapie", safe(rs.getString("TERAPIE")));
                    scheda.put("caregiver", safe(rs.getString("CAREGIVER")));
                    scheda.put("caregiverCodFiscale", safe(rs.getString("CAREGIVER")));
                }
            }
        }

        if (scheda == null) {
            return null;
        }

        scheda.put("patologieCroniche", findSchedaPazientePatologieByScheda(idScheda, codPaz));
        scheda.put("diarioStorico", findSchedaPazienteDiario(codPaz));
        return scheda;
    }

    public List<Map<String, String>> findSchedaPazienteDiario(String codPaz) throws SQLException {
        String sql = "SELECT D.ID, TO_CHAR(D.DATA_DIARIO, 'DD/MM/YYYY HH24:MI') AS DATA_DIARIO, "
                + "D.DESCRIZIONE AS DESCRIZIONE "
                + "FROM APO_SCHEDE_PAZIENTE_DIARIO D "
                + "WHERE TRIM(CAST(D.COD_PAZ AS VARCHAR2(100))) = ? "
                + "ORDER BY D.DATA_DIARIO DESC";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                int rowIndex = 0;
                while (rs.next()) {
                    rowIndex += 1;
                    HashMap<String, String> diario = new HashMap<>();
                    diario.put("id", safe(rs.getString("ID")));
                    diario.put("dataDiario", safe(rs.getString("DATA_DIARIO")));
                    diario.put("descrizione", safe(rs.getString("DESCRIZIONE")));
                    results.add(diario);
                }
            }
        }

        return results;
    }

    public int insertSchedaPazienteDiario(String codPaz, String dataDiario, String descrizione,
            String username) throws SQLException {
        try (Connection connection = dataService.getConnection()) {
            BigDecimal idScheda = findLatestSchedaPazienteId(connection, codPaz);
            if (idScheda == null) {
                throw new SQLException("Scheda paziente non trovata per il paziente selezionato.");
            }

            String sql = "INSERT INTO APO_SCHEDE_PAZIENTE_DIARIO ("
                    + "ID_SCHEDA, COD_PAZ, DATA_DIARIO, DESCRIZIONE, UTENTE_INS"
                    + ") VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD HH24:MI'), ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBigDecimal(1, idScheda);
                statement.setString(2, safe(codPaz));
                statement.setString(3, safe(dataDiario));
                statement.setString(4, safe(descrizione));
                statement.setString(5, safe(username));
                return statement.executeUpdate();
            }
        }
    }

    public int deleteSchedaPazienteDiario(String idDiario, String codPaz) throws SQLException {
        BigDecimal id;
        try {
            id = new BigDecimal(safe(idDiario));
        } catch (NumberFormatException ex) {
            throw new SQLException("ID diario non valido.", ex);
        }

        String sql = "DELETE FROM APO_SCHEDE_PAZIENTE_DIARIO "
                + "WHERE ID = ? "
                + "AND TRIM(CAST(COD_PAZ AS VARCHAR2(100))) = ?";
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, id);
            statement.setString(2, safe(codPaz));
            return statement.executeUpdate();
        }
    }

    public List<Map<String, String>> findSchedaPazientePatologie(String codPaz) throws SQLException {
        String sql = "SELECT P.ID, I2.CODICE, I2.DESCRIZIONE PATOLOGIA "
                + "FROM TOPSAN.ICD10 I2 "
                + "JOIN APO_SCHEDE_PAZIENTE_PATOLOGIE P ON P.CODICE_PATOLOGIA = I2.CODICE "
                + "WHERE I2.SOTTOCATEGORIA IS NOT NULL "
                + "AND TRIM(CAST(P.COD_PAZIENTE AS VARCHAR2(100))) = ? "
                + "ORDER BY I2.CODICE, I2.SOTTOCATEGORIA";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                int rowIndex = 0;
                while (rs.next()) {
                    rowIndex += 1;
                    HashMap<String, String> patologia = new HashMap<>();
                    patologia.put("id", safe(rs.getString("ID")));
                    patologia.put("codice", safe(rs.getString("CODICE")));
                    patologia.put("descrizione", safe(rs.getString("PATOLOGIA")));
                    results.add(patologia);
                }
            }
        }

        return results;
    }

    private List<Map<String, String>> findSchedaPazientePatologieByScheda(BigDecimal idScheda,
            String codPaz) throws SQLException {
        if (idScheda == null) {
            return new ArrayList<>();
        }

        String sql = "SELECT P.ID, I2.CODICE, I2.DESCRIZIONE PATOLOGIA "
                + "FROM TOPSAN.ICD10 I2 "
                + "JOIN APO_SCHEDE_PAZIENTE_PATOLOGIE P ON P.CODICE_PATOLOGIA = I2.CODICE "
                + "WHERE I2.SOTTOCATEGORIA IS NOT NULL "
                + "AND P.ID_SCHEDA = ? "
                + "AND TRIM(CAST(P.COD_PAZIENTE AS VARCHAR2(100))) = ? "
                + "ORDER BY I2.CODICE, I2.SOTTOCATEGORIA";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, idScheda);
            statement.setString(2, safe(codPaz));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, String> patologia = new HashMap<>();
                    patologia.put("id", safe(rs.getString("ID")));
                    patologia.put("codice", safe(rs.getString("CODICE")));
                    patologia.put("descrizione", safe(rs.getString("PATOLOGIA")));
                    results.add(patologia);
                }
            }
        }

        return results;
    }

    public int insertSchedaPazientePatologia(String codPaz, String codicePatologia, String username) throws SQLException {
        try (Connection connection = dataService.getConnection()) {
            BigDecimal idScheda = findLatestSchedaPazienteId(connection, codPaz);
            if (idScheda == null) {
                throw new SQLException("Scheda paziente non trovata per il paziente selezionato.");
            }
            if (existsSchedaPazientePatologia(connection, codPaz, codicePatologia)) {
                return 0;
            }

            String sql = "INSERT INTO APO_SCHEDE_PAZIENTE_PATOLOGIE ("
                    + "ID_SCHEDA, COD_PAZIENTE, CODICE_PATOLOGIA, UTENTE_INS"
                    + ") VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBigDecimal(1, idScheda);
                statement.setString(2, safe(codPaz));
                statement.setString(3, safe(codicePatologia));
                statement.setString(4, safe(username));
                return statement.executeUpdate();
            }
        }
    }

    public int deleteSchedaPazientePatologia(String idPatologia, String codPaz) throws SQLException {
        BigDecimal id;
        try {
            id = new BigDecimal(safe(idPatologia));
        } catch (NumberFormatException ex) {
            throw new SQLException("ID patologia non valido.", ex);
        }

        String sql = "DELETE FROM APO_SCHEDE_PAZIENTE_PATOLOGIE "
                + "WHERE ID = ? "
                + "AND TRIM(CAST(COD_PAZIENTE AS VARCHAR2(100))) = ?";
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, id);
            statement.setString(2, safe(codPaz));
            return statement.executeUpdate();
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

    private boolean existsSchedaPazientePatologia(Connection connection, String codPaz,
            String codicePatologia) throws SQLException {
        String sql = "SELECT 1 FROM APO_SCHEDE_PAZIENTE_PATOLOGIE "
                + "WHERE TRIM(CAST(COD_PAZIENTE AS VARCHAR2(100))) = ? "
                + "AND UPPER(TRIM(CODICE_PATOLOGIA)) = UPPER(?) "
                + "AND ROWNUM = 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codPaz));
            statement.setString(2, safe(codicePatologia));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertPrestazioniInfermieristiche(List<String> idCategorie, String operatore, String codServizio,
            String idConsulenza, String codiceFiscalePaz, String note) throws SQLException {
        if (idCategorie == null || idCategorie.isEmpty()) {
            return 0;
        }

        String sql = "INSERT INTO PRESTAZIONI_EFFETTUATE_APO ("
                + "ID_CATEGORIA, OPERATORE, "
                + "DATA_INS, COD_SERVIZIO, ID_CONSULENZA, "
                + "TIPO, COD_RICETTA, COD_PT, "
                + "COD_FISCALE_PAZ, A_DATA, NOTE) "
                + "VALUES (?, ?, SYSDATE, ?, ?, 'PRESTAZIONE INFERMIERISTICA', NULL, NULL, ?, NULL, ?)";

        try (Connection connection = dataService.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int inserted = 0;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (String rawIdCategoria : idCategorie) {
                        String idCategoria = safe(rawIdCategoria);
                        if (idCategoria.isEmpty()) {
                            continue;
                        }
                        statement.setString(1, idCategoria);
                        statement.setString(2, safe(operatore));
                        statement.setString(3, safe(codServizio));
                        statement.setString(4, safe(idConsulenza));
                        statement.setString(5, safe(codiceFiscalePaz));
                        statement.setString(6, safe(note));
                        inserted += statement.executeUpdate();
                    }
                }
                connection.commit();
                return inserted;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public List<Map<String, String>> findRicetteFarmaci(String codiceFiscale) throws SQLException {
        String sql = "SELECT "
                + "TRIM(CAST(R.COD_RICETTA AS VARCHAR2(100))) AS COD_RICETTA, "
                + "TRIM(CAST(R.COD_SERVIZIO AS VARCHAR2(100))) AS COD_SERVIZIO, "
                + "TRIM(CAST(S.DESCRIZIONE AS VARCHAR2(255))) AS DESCRIZIONE, "
                + "TRIM(CAST(R.CFMEDICO1 AS VARCHAR2(32))) AS CFMEDICO1, "
                + "TRIM(CAST(U.COGNOME || ' ' || U.NOME AS VARCHAR2(255))) AS MEDICO, "
                + "TRIM(CAST(NVL(R.NRE, R.BAR1 || R.BAR2) AS VARCHAR2(100))) AS RICETTA, "
                + "TRIM(CAST(R.COD_ACCETTAZIONE AS VARCHAR2(100))) AS COD_ACCETTAZIONE, "
                + "TO_CHAR(R.DATA_RICETTA, 'DD/MM/YYYY HH24:MI') AS DATA_RICETTA, "
                + "TO_CHAR(R.DATA_RICETTA, 'YYYY-MM-DD HH24:MI:SS') AS DATA_RICETTA_SORT, "
                + "TRIM(CAST(E.DESCRIZIONE_ESAME AS VARCHAR2(500))) AS FARMACO, "
                + "TRIM(CAST(E.COD_ESENZIONE AS VARCHAR2(100))) AS COD_ESENZIONE "
                + "FROM ricette_farmaci.intestazione_ricetta R "
                + "JOIN ricette_farmaci.esami_ricetta E ON R.cod_ricetta = E.cod_ricetta "
                + "JOIN SERVIZI S ON S.COD_SERVIZIO = R.COD_SERVIZIO "
                + "JOIN ricette_farmaci.UTENTI U ON U.COD_FISCALE = R.CFMEDICO1 AND U.COD_SERVIZIO = R.COD_SERVIZIO "
                + "WHERE UPPER(TRIM(CAST(R.COD_FISCALE AS VARCHAR2(32)))) = UPPER(?) "
                + "AND (R.COD_SERVIZIO LIKE 'APO%' OR R.COD_SERVIZIO LIKE 'GMED%') "
                + "ORDER BY R.DATA_RICETTA DESC";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRicettaFarmaco(rs));
                }
            }
        }

        return results;
    }

    public List<Map<String, String>> findRicetteDematerializzate(String codiceFiscale) throws SQLException {
        String sql = "SELECT "
                + "TRIM(CAST(R.COD_RICHIESTA AS VARCHAR2(100))) AS COD_RICHIESTA, "
                + "TRIM(CAST(R.COD_SERVIZIO AS VARCHAR2(100))) AS COD_SERVIZIO, "
                + "TRIM(CAST(S.DESCRIZIONE AS VARCHAR2(255))) AS DESCRIZIONE, "
                + "TRIM(CAST(R.CFMEDICO1 AS VARCHAR2(32))) AS CFMEDICO1, "
                + "TRIM(CAST(U.COGNOME || ' ' || U.NOME AS VARCHAR2(255))) AS MEDICO, "
                + "TRIM(CAST(NVL(R.NRE, R.BAR1 || R.BAR2) AS VARCHAR2(100))) AS RICETTA, "
                + "TRIM(CAST(R.COD_ACCETTAZIONE AS VARCHAR2(100))) AS COD_ACCETTAZIONE, "
                + "TO_CHAR(R.DATA_RICETTA, 'DD/MM/YYYY HH24:MI') AS DATA_RICETTA, "
                + "TO_CHAR(R.DATA_RICETTA, 'YYYY-MM-DD HH24:MI:SS') AS DATA_RICETTA_SORT, "
                + "TRIM(CAST(E.DESCRIZIONE_ESAME AS VARCHAR2(500))) AS ESAME, "
                + "TRIM(CAST(E.COD_ESENZIONE AS VARCHAR2(100))) AS COD_ESENZIONE "
                + "FROM ricette.intestazione_ricetta R "
                + "JOIN ricette.esami_ricetta E ON R.cod_richiesta = E.cod_richiesta "
                + "JOIN SERVIZI S ON S.COD_SERVIZIO = R.COD_SERVIZIO "
                + "JOIN ricette_farmaci.UTENTI U ON U.COD_FISCALE = R.CFMEDICO1 AND U.COD_SERVIZIO = R.COD_SERVIZIO "
                + "WHERE UPPER(TRIM(CAST(R.COD_FISCALE AS VARCHAR2(32)))) = UPPER(?) "
                + "AND (R.COD_SERVIZIO LIKE 'APO%' OR R.COD_SERVIZIO LIKE 'GMED%') "
                + "ORDER BY R.DATA_RICETTA DESC";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRicettaDematerializzata(rs));
                }
            }
        }

        return results;
    }

    public List<Map<String, String>> findPianiTerapeutici(String codiceFiscale) throws SQLException {
        String sql = "SELECT "
                + "TRIM(CAST(PT.COD_PIANO AS VARCHAR2(100))) AS COD_PIANO, "
                + "TRIM(CAST(S.DESCRIZIONE AS VARCHAR2(255))) AS SERVIZIO, "
                + "TO_CHAR(PT.DATA_PIANO, 'DD/MM/YYYY') AS DATA_PIANO, "
                + "TO_CHAR(PT.DATA_PIANO, 'YYYY-MM-DD HH24:MI:SS') AS DATA_PIANO_SORT, "
                + "TO_CHAR(PT.DATA_SCADENZA, 'DD/MM/YYYY') AS DATA_SCADENZA, "
                + "TO_CHAR(PT.DATA_SCADENZA, 'YYYY-MM-DD HH24:MI:SS') AS DATA_SCADENZA_SORT, "
                + "TRIM(CAST(PT.MEDICO_CURANTE AS VARCHAR2(255))) AS MEDICO_CURANTE, "
                + "TRIM(CAST(PT.DURATA AS VARCHAR2(100))) AS DURATA, "
                + "TRIM(CAST(PT.NOTE AS VARCHAR2(2000))) AS NOTE, "
                + "TRIM(CAST(PT.SOSTANZA AS VARCHAR2(500))) AS SOSTANZA, "
                + "TRIM(CAST(PT.FARMACO AS VARCHAR2(500))) AS FARMACO, "
                + "TRIM(CAST(PT.DOSAGGIO AS VARCHAR2(255))) AS DOSAGGIO, "
                + "TRIM(CAST(PT.UNITA_POSOLOGICA AS VARCHAR2(500))) AS UNITA_POSOLOGICA, "
                + "TRIM(CAST(PT.DESC_UNITA_POSOLOGICA_FREQ AS VARCHAR2(500))) AS POSOLOGIA, "
                + "TRIM(CAST(PT.DIAGNOSI AS VARCHAR2(2000))) AS DIAGNOSI "
                + "FROM AMBULATORI.VIEW_PIANI_TERAPEUTICI PT "
                + "JOIN AMBULATORI.SERVIZI S ON PT.COD_SERVIZIO = S.COD_SERVIZIO "
                + "WHERE UPPER(TRIM(CAST(PT.COD_FISCALE AS VARCHAR2(32)))) = UPPER(?) "
                + "AND PT.DATA_SCADENZA >= SYSDATE "
                + "ORDER BY PT.DATA_PIANO DESC";

        List<Map<String, String>> results = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapPianoTerapeutico(rs));
                }
            }
        }

        return results;
    }

    public Map<String, String> findAllegatoM(String codConsulenza, String codServizio) throws SQLException {
        String sql = "SELECT * FROM ALLEGATOM WHERE COD_CONSULENZA = ? AND COD_SERVIZIO = ?";

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codConsulenza));
            statement.setString(2, safe(codServizio));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapAllegatoM(rs);
                }
            }
        }

        return new HashMap<>();
    }

    public String saveAllegatoM(Map<String, String> allegato, String username, String workstation) throws SQLException {
        String codPaz = safe(allegato.get("codPaz"));
        String codConsulenza = safe(allegato.get("codConsulenza"));
        String codServizio = safe(allegato.get("codServizio"));
        String operation;

        syncAllegatoM118Alias(allegato);
        allegato.put("esitoInterv", buildAllegatoMEsitoIntervento(allegato));

        try (Connection connection = dataService.getConnection()) {
            List<String> existingColumns = getAllegatoMColumns(connection);
            if (existsAllegatoM(connection, codPaz, codConsulenza, codServizio)) {
                updateAllegatoM(connection, allegato, username, workstation, existingColumns);
                operation = "update";
            } else {
                insertAllegatoM(connection, allegato, username, workstation, existingColumns);
                operation = "insert";
            }
        }

        return operation;
    }

    private Map<String, String> mapMedicoCurante(ResultSet rs) throws SQLException {
        HashMap<String, String> medico = new HashMap<>();
        String nome = safe(rs.getString("MED_NOME"));
        String cognome = safe(rs.getString("MED_COGNOME"));
        String descrizione = (cognome + " " + nome).trim();

        medico.put("pinAzienda", safe(rs.getString("PIN_AZIENDA")));
        medico.put("matricola", safe(rs.getString("MED_MATRICOLA")));
        medico.put("nome", nome);
        medico.put("cognome", cognome);
        medico.put("codFiscale", safe(rs.getString("CF_MEDICO")));
        medico.put("descrizione", descrizione);
        return medico;
    }

    private Map<String, String> mapAnagrafePaziente(ResultSet rs) throws SQLException {
        HashMap<String, String> paziente = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int index = 1; index <= columnCount; index += 1) {
            String columnName = safe(metaData.getColumnLabel(index));
            if (columnName.isEmpty()) {
                columnName = safe(metaData.getColumnName(index));
            }
            if (!columnName.isEmpty()) {
                paziente.put(toCamelCase(columnName), safe(rs.getString(index)));
            }
        }

        String cognome = safe(paziente.get("cognome"));
        String nome = safe(paziente.get("nome"));
        String codFiscale = firstNonBlank(paziente.get("codFiscale"), paziente.get("codiceFiscale"), paziente.get("cf"));
        paziente.put("cognome", cognome);
        paziente.put("nome", nome);
        paziente.put("paziente", (cognome + " " + nome).trim());
        paziente.put("id", codFiscale);
        paziente.put("dataNascita", firstNonBlank(paziente.get("dataNascita"), paziente.get("dataNas"), paziente.get("dtNascita")));
        paziente.put("codFiscale", codFiscale);
        paziente.put("sesso", safe(paziente.get("sesso")));
        paziente.put("eta", safe(paziente.get("eta")));
        paziente.put("codComuneRes", firstNonBlank(paziente.get("codComuneRes"), paziente.get("codComRes"), paziente.get("codComumeRes")));
        paziente.put("codPaz", safe(paziente.get("codPaz")));
        paziente.put("pin", safe(paziente.get("pin")));
        return paziente;
    }

    private boolean isAnagrafeCodiceFiscalePresent(Connection connection, String codiceFiscale) throws SQLException {
        String sql = "SELECT 1 FROM VIEW_ANAGRAFE_DETTAGLIO_GM "
                + "WHERE UPPER(TRIM(CAST(COD_FISCALE AS VARCHAR2(32)))) = UPPER(?) AND ROWNUM = 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Map<String, String> findAnagrafeByCodiceFiscale(Connection connection, String codiceFiscale) throws SQLException {
        String sql = "SELECT * FROM VIEW_ANAGRAFE_DETTAGLIO_GM "
                + "WHERE UPPER(TRIM(CAST(COD_FISCALE AS VARCHAR2(32)))) = UPPER(?) "
                + "AND ROWNUM = 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codiceFiscale));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapAnagrafePaziente(rs);
                }
            }
        }

        return new HashMap<>();
    }

    private int insertPazienteBuffer(Connection connection, Map<String, String> payload,
            Date dataNascita, String codiceFiscale, String username) throws SQLException {
        String sql = "INSERT INTO TOPSAN.PAZIENTI_BUFFER ("
                + "COD_COM_NASCITA, COD_FISCALE, COGNOME, DATA_MODIFICA, DATA_NASCITA, "
                + "MOD_UTENTE, NOME, SESSO, WS_UTENTE) "
                + "VALUES (?, ?, ?, SYSDATE, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, new String[]{"PROGRESSIVO"})) {
            statement.setString(1, safe(payload.get("codComuneNascita")));
            statement.setString(2, codiceFiscale);
            statement.setString(3, safe(payload.get("cognome")).toUpperCase(Locale.ITALY));
            statement.setDate(4, dataNascita);
            statement.setString(5, safe(username));
            statement.setString(6, safe(payload.get("nome")).toUpperCase(Locale.ITALY));
            statement.setString(7, safe(payload.get("sesso")).toUpperCase(Locale.ITALY));
            statement.setString(8, ANAGRAFE_BUFFER_WORKSTATION);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int progressivo = keys.getInt(1);
                    if (progressivo > 0) {
                        return progressivo;
                    }
                }
            }
        }

        return loadPazientiBufferProgressivo(connection);
    }

    private int loadPazientiBufferProgressivo(Connection connection) throws SQLException {
        String sql = "SELECT TOPSAN.PAZIENTI_BUFFER_SEQ.CURRVAL AS PROGRESSIVO FROM DUAL";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("PROGRESSIVO");
            }
        }

        throw new SQLException("Impossibile recuperare il progressivo del paziente in anagrafe buffer.");
    }

    private void insertPazienteStoricaBuffer(Connection connection, int progressivo,
            Map<String, String> payload, String codiceFiscale, String username) throws SQLException {
        String sql = "INSERT INTO TOPSAN.PAZIENTI_STORICA_BUFFER ("
                + "CAP_RES, COD_ASL_RES, COD_COM_RES, COD_FISCALE, COD_SERVIZIO, "
                + "DA_DATA, DATA_MODIFICA, IND_RES, TEL_RES, COD_COM_DOM, CAP_DOM, "
                + "IND_DOM, TEL_DOM, COD_ASL_ISCR, MOD_UTENTE, PROGRESSIVO, WS_UTENTE) "
                + "VALUES (?, ?, ?, ?, ?, SYSDATE, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String comuneDomicilio = safe(payload.get("codComuneDomicilio"));

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(payload.get("capResidenza")));
            statement.setString(2, safe(payload.get("codAslResidenza")));
            statement.setString(3, safe(payload.get("codComuneResidenza")));
            statement.setString(4, codiceFiscale);
            statement.setString(5, ANAGRAFE_BUFFER_SERVICE);
            statement.setString(6, safe(payload.get("indirizzoResidenza")));
            statement.setString(7, safe(payload.get("telResidenza")));
            statement.setString(8, hasText(comuneDomicilio) ? comuneDomicilio : null);
            statement.setString(9, safe(payload.get("capDomicilio")));
            statement.setString(10, safe(payload.get("indirizzoDomicilio")));
            statement.setString(11, safe(payload.get("telDomicilio")));
            statement.setString(12, safe(payload.get("codAslIscrizione")));
            statement.setString(13, safe(username));
            statement.setInt(14, progressivo);
            statement.setString(15, ANAGRAFE_BUFFER_WORKSTATION);
            statement.executeUpdate();
        }
    }

    private int registraPazienteBuffer(Connection connection, int progressivo) throws SQLException {
        try (CallableStatement statement = connection.prepareCall("{call TOPSAN.BUFFER_TO_PRODUCTION(?)}")) {
            statement.setInt(1, progressivo);
            statement.execute();
        }
        return 1;
    }

    private String findBufferCodPaz(Connection connection, int progressivo) throws SQLException {
        String sql = "SELECT TRIM(CAST(COD_PAZ AS VARCHAR2(20))) AS COD_PAZ "
                + "FROM TOPSAN.PAZIENTI_BUFFER WHERE PROGRESSIVO = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, progressivo);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return safe(rs.getString("COD_PAZ"));
                }
            }
        }

        return "";
    }

    private String resolveComuneCode(Connection connection, String value, String label, boolean required) throws SQLException {
        return resolveComuneField(connection, value, label, "COD_COMUNE", required);
    }

    private String resolveComuneFiscalCode(Connection connection, String value, String label, boolean required) throws SQLException {
        return resolveComuneField(connection, value, label, "COD_FISCALE", required);
    }

    private String resolveComuneField(Connection connection, String value, String label,
            String fieldName, boolean required) throws SQLException {
        String normalizedValue = safe(value);
        if (!hasText(normalizedValue)) {
            if (required) {
                throw new SQLException(label + " non valorizzato.");
            }
            return "";
        }

        String exactSql = "SELECT COD_COMUNE, COD_FISCALE FROM TOPSAN.COMUNI "
                + "WHERE A_DATA IS NULL "
                + "AND (UPPER(TRIM(COD_COMUNE)) = UPPER(?) OR UPPER(TRIM(DESCRIZIONE)) = UPPER(?))";
        String exactValue = findComuneField(connection, exactSql, normalizedValue, normalizedValue, fieldName, label, false);
        if (hasText(exactValue)) {
            return exactValue;
        }

        String likeSql = "SELECT COD_COMUNE, COD_FISCALE FROM TOPSAN.COMUNI "
                + "WHERE A_DATA IS NULL AND UPPER(TRIM(DESCRIZIONE)) LIKE UPPER(?)";
        String likeValue = "%" + normalizedValue + "%";
        String resolvedValue = findComuneField(connection, likeSql, likeValue, null, fieldName, label, true);
        if (hasText(resolvedValue)) {
            return resolvedValue;
        }

        throw new SQLException(label + " non trovato: " + normalizedValue);
    }

    private String findComuneField(Connection connection, String sql, String firstParameter,
            String secondParameter, String fieldName, String label, boolean failOnMultiple) throws SQLException {
        ArrayList<String> values = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstParameter);
            if (secondParameter != null) {
                statement.setString(2, secondParameter);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next() && values.size() < 2) {
                    values.add(safe(rs.getString(fieldName)));
                }
            }
        }

        if (values.size() > 1 && failOnMultiple) {
            throw new SQLException(label + " non univoco. Inserire una descrizione piu precisa o il codice comune.");
        }
        if (values.isEmpty()) {
            return "";
        }
        if (!hasText(values.get(0))) {
            throw new SQLException(label + " senza " + fieldName + ".");
        }
        return values.get(0);
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index += 1) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    private String likeValue(String value) {
        return "%" + safe(value).toUpperCase(Locale.ITALY) + "%";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String fiscalCodeNamePart(String value, boolean isName) {
        String normalized = normalizeFiscalCodeText(value);
        StringBuilder consonants = new StringBuilder();
        StringBuilder vowels = new StringBuilder();

        for (int index = 0; index < normalized.length(); index += 1) {
            char current = normalized.charAt(index);
            if ("AEIOU".indexOf(current) >= 0) {
                vowels.append(current);
            } else {
                consonants.append(current);
            }
        }

        if (isName && consonants.length() > 3) {
            return new StringBuilder()
                    .append(consonants.charAt(0))
                    .append(consonants.charAt(2))
                    .append(consonants.charAt(3))
                    .toString();
        }

        String result = consonants.append(vowels).append("XXX").toString();
        return result.substring(0, 3);
    }

    private String normalizeFiscalCodeText(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ITALY);
        return normalized.replaceAll("[^A-Z]", "");
    }

    private String fiscalCodeMonthCode(int month) {
        String codes = "ABCDEHLMPRST";
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mese di nascita non valido.");
        }
        return String.valueOf(codes.charAt(month - 1));
    }

    private String fiscalCodeDayCode(int day, String sesso) {
        int fiscalDay = "F".equals(sesso) ? day + 40 : day;
        return String.format(Locale.ITALY, "%02d", Integer.valueOf(fiscalDay));
    }

    private char fiscalCodeCheckChar(String partialCode) {
        int sum = 0;
        String normalized = safe(partialCode).toUpperCase(Locale.ITALY);

        for (int index = 0; index < normalized.length(); index += 1) {
            char current = normalized.charAt(index);
            if ((index + 1) % 2 == 1) {
                sum += oddFiscalCodeValue(current);
            } else {
                sum += evenFiscalCodeValue(current);
            }
        }

        return (char) ('A' + (sum % 26));
    }

    private int evenFiscalCodeValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'A' && value <= 'Z') {
            return value - 'A';
        }
        return 0;
    }

    private int oddFiscalCodeValue(char value) {
        switch (value) {
            case '0':
            case 'A':
                return 1;
            case '1':
            case 'B':
                return 0;
            case '2':
            case 'C':
                return 5;
            case '3':
            case 'D':
                return 7;
            case '4':
            case 'E':
                return 9;
            case '5':
            case 'F':
                return 13;
            case '6':
            case 'G':
                return 15;
            case '7':
            case 'H':
                return 17;
            case '8':
            case 'I':
                return 19;
            case '9':
            case 'J':
                return 21;
            case 'K':
                return 2;
            case 'L':
                return 4;
            case 'M':
                return 18;
            case 'N':
                return 20;
            case 'O':
                return 11;
            case 'P':
                return 3;
            case 'Q':
                return 6;
            case 'R':
                return 8;
            case 'S':
                return 12;
            case 'T':
                return 14;
            case 'U':
                return 16;
            case 'V':
                return 10;
            case 'W':
                return 22;
            case 'X':
                return 25;
            case 'Y':
                return 24;
            case 'Z':
                return 23;
            default:
                return 0;
        }
    }

    private Map<String, String> mapPrestazioneInfermieristica(ResultSet rs) throws SQLException {
        HashMap<String, String> prestazione = new HashMap<>();
        prestazione.put("id", safe(rs.getString("ID")));
        prestazione.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
        prestazione.put("descrizione", safe(rs.getString("DESCRIZIONE")));
        prestazione.put("dataIns", safe(rs.getString("DATA_INS")));
        prestazione.put("dataInsSort", safe(rs.getString("DATA_INS_SORT")));
        prestazione.put("operatore", safe(rs.getString("OPERATORE")));
        prestazione.put("descOperatore", safe(rs.getString("DESC_OPERATORE")));
        prestazione.put("idConsulenza", safe(rs.getString("ID_CONSULENZA")));
        prestazione.put("codPrestazione", safe(rs.getString("COD_PRESTAZIONE")));
        prestazione.put("categoria", safe(rs.getString("CATEGORIA")));
        prestazione.put("sottoCategoria", safe(rs.getString("SOTTO_CATEGORIA")));
        prestazione.put("prestazione", safe(rs.getString("PRESTAZIONE")));
        return prestazione;
    }

    private Map<String, String> mapPatologiaCronicaIcd10(ResultSet rs) throws SQLException {
        HashMap<String, String> patologia = new HashMap<>();
        patologia.put("codiceCategoria", safe(rs.getString("CODICE_CATEGORIA")));
        patologia.put("categoria", safe(rs.getString("CATEGORIA")));
        patologia.put("codice", safe(rs.getString("CODICE")));
        patologia.put("descrizione", safe(rs.getString("PATOLOGIA")));
        return patologia;
    }

    private Map<String, String> mapRicettaFarmaco(ResultSet rs) throws SQLException {
        HashMap<String, String> ricetta = new HashMap<>();
        ricetta.put("codRicetta", safe(rs.getString("COD_RICETTA")));
        ricetta.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
        ricetta.put("descrizione", safe(rs.getString("DESCRIZIONE")));
        ricetta.put("cfMedico", safe(rs.getString("CFMEDICO1")));
        ricetta.put("medico", safe(rs.getString("MEDICO")));
        ricetta.put("ricetta", safe(rs.getString("RICETTA")));
        ricetta.put("codAccettazione", safe(rs.getString("COD_ACCETTAZIONE")));
        ricetta.put("dataRicetta", safe(rs.getString("DATA_RICETTA")));
        ricetta.put("dataRicettaSort", safe(rs.getString("DATA_RICETTA_SORT")));
        ricetta.put("farmaco", safe(rs.getString("FARMACO")));
        ricetta.put("codEsenzione", safe(rs.getString("COD_ESENZIONE")));
        return ricetta;
    }

    private Map<String, String> mapRicettaDematerializzata(ResultSet rs) throws SQLException {
        HashMap<String, String> ricetta = new HashMap<>();
        ricetta.put("codRichiesta", safe(rs.getString("COD_RICHIESTA")));
        ricetta.put("codServizio", safe(rs.getString("COD_SERVIZIO")));
        ricetta.put("descrizione", safe(rs.getString("DESCRIZIONE")));
        ricetta.put("cfMedico", safe(rs.getString("CFMEDICO1")));
        ricetta.put("medico", safe(rs.getString("MEDICO")));
        ricetta.put("ricetta", safe(rs.getString("RICETTA")));
        ricetta.put("codAccettazione", safe(rs.getString("COD_ACCETTAZIONE")));
        ricetta.put("dataRicetta", safe(rs.getString("DATA_RICETTA")));
        ricetta.put("dataRicettaSort", safe(rs.getString("DATA_RICETTA_SORT")));
        ricetta.put("esame", safe(rs.getString("ESAME")));
        ricetta.put("codEsenzione", safe(rs.getString("COD_ESENZIONE")));
        return ricetta;
    }

    private Map<String, String> mapPianoTerapeutico(ResultSet rs) throws SQLException {
        HashMap<String, String> piano = new HashMap<>();
        piano.put("codPiano", safe(rs.getString("COD_PIANO")));
        piano.put("servizio", safe(rs.getString("SERVIZIO")));
        piano.put("dataPiano", safe(rs.getString("DATA_PIANO")));
        piano.put("dataPianoSort", safe(rs.getString("DATA_PIANO_SORT")));
        piano.put("dataScadenza", safe(rs.getString("DATA_SCADENZA")));
        piano.put("dataScadenzaSort", safe(rs.getString("DATA_SCADENZA_SORT")));
        piano.put("medicoCurante", safe(rs.getString("MEDICO_CURANTE")));
        piano.put("durata", safe(rs.getString("DURATA")));
        piano.put("note", safe(rs.getString("NOTE")));
        piano.put("sostanza", safe(rs.getString("SOSTANZA")));
        piano.put("farmaco", safe(rs.getString("FARMACO")));
        piano.put("dosaggio", safe(rs.getString("DOSAGGIO")));
        piano.put("unitaPosologica", safe(rs.getString("UNITA_POSOLOGICA")));
        piano.put("posologia", safe(rs.getString("POSOLOGIA")));
        piano.put("diagnosi", safe(rs.getString("DIAGNOSI")));
        return piano;
    }

    private Map<String, String> mapAllegatoM(ResultSet rs) throws SQLException {
        HashMap<String, String> allegato = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int index = 1; index <= columnCount; index += 1) {
            String columnName = safe(metaData.getColumnLabel(index));
            if (columnName.isEmpty()) {
                columnName = safe(metaData.getColumnName(index));
            }
            if (!columnName.isEmpty()) {
                allegato.put(toCamelCase(columnName), safe(rs.getString(index)));
            }
        }
        syncAllegatoM118Alias(allegato);

        return allegato;
    }

    private boolean existsAllegatoM(Connection connection, String codPaz, String codConsulenza, String codServizio) throws SQLException {
        String sql = "SELECT 1 FROM ALLEGATOM "
                + "WHERE COD_PAZ = ? AND COD_CONSULENZA = ? AND COD_SERVIZIO = ? AND ROWNUM = 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, safe(codPaz));
            statement.setString(2, safe(codConsulenza));
            statement.setString(3, safe(codServizio));
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updateAllegatoM(Connection connection, Map<String, String> allegato,
            String username, String workstation, List<String> existingColumns) throws SQLException {
        List<String> editableColumns = getExistingAllegatoMEditableColumns(existingColumns);
        boolean hasDataModifica = hasAllegatoMColumn(existingColumns, "DATA_MODIFICA");
        boolean hasModUtente = hasAllegatoMColumn(existingColumns, "MOD_UTENTE");
        boolean hasWsUtente = hasAllegatoMColumn(existingColumns, "WS_UTENTE");
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ALLEGATOM SET ");

        for (int index = 0; index < editableColumns.size(); index += 1) {
            if (index > 0) {
                sql.append(", ");
            }
            sql.append(editableColumns.get(index)).append(" = ?");
        }

        if (hasDataModifica) {
            appendSqlAssignmentSeparator(sql);
            sql.append("DATA_MODIFICA = SYSDATE");
        }
        if (hasModUtente) {
            appendSqlAssignmentSeparator(sql);
            sql.append("MOD_UTENTE = ?");
        }
        if (hasWsUtente) {
            appendSqlAssignmentSeparator(sql);
            sql.append("WS_UTENTE = ?");
        }

        sql.append("WHERE COD_PAZ = ? AND COD_CONSULENZA = ? AND COD_SERVIZIO = ?");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = bindAllegatoMEditableColumns(statement, 1, editableColumns, allegato);
            if (hasModUtente) {
                statement.setString(parameterIndex++, safe(username));
            }
            if (hasWsUtente) {
                statement.setString(parameterIndex++, safe(workstation));
            }
            statement.setString(parameterIndex++, safe(allegato.get("codPaz")));
            statement.setString(parameterIndex++, safe(allegato.get("codConsulenza")));
            statement.setString(parameterIndex, safe(allegato.get("codServizio")));
            statement.executeUpdate();
        }
    }

    private void insertAllegatoM(Connection connection, Map<String, String> allegato,
            String username, String workstation, List<String> existingColumns) throws SQLException {
        List<String> editableColumns = getExistingAllegatoMEditableColumns(existingColumns);
        boolean hasDataModifica = hasAllegatoMColumn(existingColumns, "DATA_MODIFICA");
        boolean hasModUtente = hasAllegatoMColumn(existingColumns, "MOD_UTENTE");
        boolean hasWsUtente = hasAllegatoMColumn(existingColumns, "WS_UTENTE");
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ALLEGATOM (COD_PAZ, COD_CONSULENZA, COD_SERVIZIO");

        for (String columnName : editableColumns) {
            sql.append(", ").append(columnName);
        }
        if (hasDataModifica) {
            sql.append(", DATA_MODIFICA");
        }
        if (hasModUtente) {
            sql.append(", MOD_UTENTE");
        }
        if (hasWsUtente) {
            sql.append(", WS_UTENTE");
        }
        sql.append(") VALUES (?, ?, ?");

        for (int index = 0; index < editableColumns.size(); index += 1) {
            sql.append(", ?");
        }
        if (hasDataModifica) {
            sql.append(", SYSDATE");
        }
        if (hasModUtente) {
            sql.append(", ?");
        }
        if (hasWsUtente) {
            sql.append(", ?");
        }
        sql.append(")");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int parameterIndex = 1;
            statement.setString(parameterIndex++, safe(allegato.get("codPaz")));
            statement.setString(parameterIndex++, safe(allegato.get("codConsulenza")));
            statement.setString(parameterIndex++, safe(allegato.get("codServizio")));
            parameterIndex = bindAllegatoMEditableColumns(statement, parameterIndex, editableColumns, allegato);
            if (hasModUtente) {
                statement.setString(parameterIndex++, safe(username));
            }
            if (hasWsUtente) {
                statement.setString(parameterIndex++, safe(workstation));
            }
            statement.executeUpdate();
        }
    }

    private int bindAllegatoMEditableColumns(PreparedStatement statement, int startIndex,
            List<String> editableColumns, Map<String, String> allegato) throws SQLException {
        int parameterIndex = startIndex;
        for (String columnName : editableColumns) {
            String fieldName = toCamelCase(columnName);
            if (isAllegatoMNumericColumn(columnName)) {
                statement.setInt(parameterIndex++, toFlagValue(allegato.get(fieldName)));
            } else {
                statement.setString(parameterIndex++, safe(allegato.get(fieldName)));
            }
        }
        return parameterIndex;
    }

    private List<String> getAllegatoMColumns(Connection connection) throws SQLException {
        ArrayList<String> columns = new ArrayList<>();
        String sql = "SELECT * FROM ALLEGATOM WHERE 1 = 0";

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

    private List<String> getExistingAllegatoMEditableColumns(List<String> existingColumns) {
        ArrayList<String> editableColumns = new ArrayList<>();
        for (String columnName : ALLEGATO_M_EDITABLE_COLUMNS) {
            if (hasAllegatoMColumn(existingColumns, columnName)) {
                editableColumns.add(columnName);
            }
        }
        return editableColumns;
    }

    private boolean hasAllegatoMColumn(List<String> existingColumns, String columnName) {
        return existingColumns != null && existingColumns.contains(safe(columnName).toUpperCase(Locale.ITALY));
    }

    private void appendSqlAssignmentSeparator(StringBuilder sql) {
        if (sql.lastIndexOf("SET ") != sql.length() - 4) {
            sql.append(", ");
        }
    }

    private boolean isAllegatoMNumericColumn(String columnName) {
        return ALLEGATO_M_NUMERIC_COLUMNS.contains("|" + safe(columnName).toUpperCase(Locale.ITALY) + "|");
    }

    private int toFlagValue(String value) {
        return isTruthy(value) ? 1 : 0;
    }

    private void syncAllegatoM118Alias(Map<String, String> allegato) {
        if (allegato == null) {
            return;
        }

        String inv118 = safe(allegato.get("inv118"));
        String inv188 = safe(allegato.get("inv188"));
        if (inv118.isEmpty() && !inv188.isEmpty()) {
            allegato.put("inv118", inv188);
        } else if (inv188.isEmpty() && !inv118.isEmpty()) {
            allegato.put("inv188", inv118);
        }
    }

    private String buildAllegatoMEsitoIntervento(Map<String, String> allegato) {
        ArrayList<String> esiti = new ArrayList<>();
        if (isTruthy(allegato.get("mmg"))) {
            esiti.add("MMG");
        }
        if (isTruthy(allegato.get("ps"))) {
            esiti.add("PS");
        }
        if (isTruthy(allegato.get("cot"))) {
            esiti.add("COT");
        }
        if (isTruthy(allegato.get("inv118"))) {
            esiti.add("118");
        }
        return joinValues(esiti, ",");
    }

    private String joinValues(List<String> values, String separator) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(value);
        }
        return result.toString();
    }

    private boolean isTruthy(String value) {
        String normalized = safe(value).toUpperCase(Locale.ITALY);
        return "1".equals(normalized)
                || "S".equals(normalized)
                || "SI".equals(normalized)
                || "Y".equals(normalized)
                || "YES".equals(normalized)
                || "TRUE".equals(normalized)
                || "T".equals(normalized)
                || "X".equals(normalized);
    }

    private String toCamelCase(String value) {
        String normalized = safe(value).toLowerCase(Locale.ITALY);
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;

        for (int index = 0; index < normalized.length(); index += 1) {
            char current = normalized.charAt(index);
            if (current == '_' || current == '-' || Character.isWhitespace(current)) {
                upperNext = result.length() > 0;
                continue;
            }
            result.append(upperNext ? Character.toUpperCase(current) : current);
            upperNext = false;
        }

        return result.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String normalized = safe(value);
            if (!normalized.isEmpty()) {
                return normalized;
            }
        }
        return "";
    }
}
