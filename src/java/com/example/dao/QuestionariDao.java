package com.example.dao;

import com.example.service.DataService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Accesso ai questionari salvati per paziente. */
public class QuestionariDao {

    private static final String TIPO_MAMMOGRAFIA = "MAMMO";
    private static final String TIPO_FASCIA65 = "FASCIA65";
    private static final String TIPO_FASCIA625 = "FASCIA625";
    private static final String TIPO_FASCIA2664 = "FASCIA2664";
    private static final String RIFIUTA_MEDICO = "SI_MEDICO";
    private final DataService dataService;

    public QuestionariDao(DataService dataService) {
        this.dataService = dataService;
    }

    public long insertMammografia(String codPaz, String codConsulenza, String utenteInsert, String questionario) throws SQLException {
        String sql = "INSERT INTO APO_QUESTIONARI "
                + "(COD_PAZ, COD_CONSULENZA, DATA, UTENTE_INSERT, TIPO, RIFIUTA, QUESTIONARIO) "
                + "VALUES (?, ?, SYSDATE, ?, 'MAMMO', ?, ?)";
        byte[] payload = questionario.getBytes(StandardCharsets.UTF_8);

        try (Connection connection = dataService.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                JsonObject fields = getQuestionarioFields(questionario);
                String rifiuta = getRifiuta(fields);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, codPaz);
                    statement.setString(2, codConsulenza);
                    statement.setString(3, utenteInsert);
                    statement.setString(4, rifiuta);
                    // Il campo QUESTIONARIO è un BLOB: setBlob evita il binding come LONGVARBINARY
                    // che con alcuni driver Oracle fallisce quando il JSON supera 4 KB.
                    statement.setBlob(5, new ByteArrayInputStream(payload));
                    statement.executeUpdate();
                }

                long idQuestionario = getCurrentQuestionarioId(connection);
                insertQuestionarioDettagli(connection, idQuestionario, buildMammografiaDettagli(fields));
                connection.commit();
                return idQuestionario;
            } catch (Exception ex) {
                rollback(connection);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Dati del questionario mammografia non validi.", ex);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public long insertFascia65(String codPaz, String codConsulenza, String utenteInsert, String questionario) throws SQLException {
        String sql = "INSERT INTO APO_QUESTIONARI "
                + "(COD_PAZ, COD_CONSULENZA, DATA, UTENTE_INSERT, TIPO, RIFIUTA, QUESTIONARIO) "
                + "VALUES (?, ?, SYSDATE, ?, 'FASCIA65', ?, ?)";
        byte[] payload = questionario.getBytes(StandardCharsets.UTF_8);

        try (Connection connection = dataService.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                JsonObject fields = getQuestionarioFields(questionario);
                String rifiuta = getRifiutaFascia(fields);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, codPaz);
                    statement.setString(2, codConsulenza);
                    statement.setString(3, utenteInsert);
                    statement.setString(4, rifiuta);
                    statement.setBlob(5, new ByteArrayInputStream(payload));
                    statement.executeUpdate();
                }

                long idQuestionario = getCurrentQuestionarioId(connection);
                insertQuestionarioDettagli(connection, idQuestionario, buildFascia65Dettagli(fields));
                connection.commit();
                return idQuestionario;
            } catch (Exception ex) {
                rollback(connection);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Dati del questionario fascia dai 65 anni non validi.", ex);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public long insertFascia625(String codPaz, String codConsulenza, String utenteInsert, String questionario) throws SQLException {
        String sql = "INSERT INTO APO_QUESTIONARI "
                + "(COD_PAZ, COD_CONSULENZA, DATA, UTENTE_INSERT, TIPO, RIFIUTA, QUESTIONARIO) "
                + "VALUES (?, ?, SYSDATE, ?, 'FASCIA625', ?, ?)";
        byte[] payload = questionario.getBytes(StandardCharsets.UTF_8);

        try (Connection connection = dataService.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                JsonObject fields = getQuestionarioFields(questionario);
                String rifiuta = getRifiutaFascia(fields);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, codPaz);
                    statement.setString(2, codConsulenza);
                    statement.setString(3, utenteInsert);
                    statement.setString(4, rifiuta);
                    statement.setBlob(5, new ByteArrayInputStream(payload));
                    statement.executeUpdate();
                }

                long idQuestionario = getCurrentQuestionarioId(connection);
                insertQuestionarioDettagli(connection, idQuestionario, buildFascia625Dettagli(fields));
                connection.commit();
                return idQuestionario;
            } catch (Exception ex) {
                rollback(connection);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Dati del questionario fascia 6-25 anni non validi.", ex);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    public long insertFascia2664(String codPaz, String codConsulenza, String utenteInsert, String questionario) throws SQLException {
        String sql = "INSERT INTO APO_QUESTIONARI "
                + "(COD_PAZ, COD_CONSULENZA, DATA, UTENTE_INSERT, TIPO, RIFIUTA, QUESTIONARIO) "
                + "VALUES (?, ?, SYSDATE, ?, 'FASCIA2664', ?, ?)";
        byte[] payload = questionario.getBytes(StandardCharsets.UTF_8);

        try (Connection connection = dataService.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                JsonObject fields = getQuestionarioFields(questionario);
                String rifiuta = getRifiutaFascia(fields);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, codPaz);
                    statement.setString(2, codConsulenza);
                    statement.setString(3, utenteInsert);
                    statement.setString(4, rifiuta);
                    statement.setBlob(5, new ByteArrayInputStream(payload));
                    statement.executeUpdate();
                }

                long idQuestionario = getCurrentQuestionarioId(connection);
                insertQuestionarioDettagli(connection, idQuestionario, buildFascia2664Dettagli(fields));
                connection.commit();
                return idQuestionario;
            } catch (Exception ex) {
                rollback(connection);
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Dati del questionario fascia 26-64 anni non validi.", ex);
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    private long getCurrentQuestionarioId(Connection connection) throws SQLException {
        String sql = "SELECT APO_QUESTIONARI_SEQ.CURRVAL AS IDQUESTIONARIO FROM DUAL";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new SQLException("Impossibile recuperare l'identificativo del questionario salvato.");
            }
            return resultSet.getLong("IDQUESTIONARIO");
        }
    }

    private void insertQuestionarioDettagli(Connection connection, long idQuestionario,
            List<QuestionarioDettaglio> dettagli) throws SQLException {
        String sql = "INSERT INTO APO_QUESTIONARI_DETT "
                + "(IDQUESTIONARIO, PROGRESSIVO, DOMANDA, RISPOSTA) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int progressivo = 1;
            for (QuestionarioDettaglio dettaglio : dettagli) {
                statement.setLong(1, idQuestionario);
                statement.setInt(2, progressivo);
                statement.setString(3, dettaglio.domanda);
                statement.setString(4, dettaglio.risposta);
                statement.addBatch();
                progressivo += 1;
            }
            statement.executeBatch();
        }
    }

    private List<QuestionarioDettaglio> buildMammografiaDettagli(JsonObject fields) {
        List<QuestionarioDettaglio> dettagli = new ArrayList<>();
        dettagli.add(new QuestionarioDettaglio(
                "Il paziente rifiuta la compilazione del questionario?",
                getFieldValue(fields, "rifiuta_compilazione")));
        dettagli.add(new QuestionarioDettaglio(
                "1) Lei effettua con regolarità la mammografia per la prevenzione dei tumori al seno?",
                getFieldValue(fields, "q1_regolarita")));
        dettagli.add(new QuestionarioDettaglio(
                "2) Se ha risposto No, può spiegare perché?",
                getFieldValue(fields, "q2_motivazione")));
        dettagli.add(new QuestionarioDettaglio(
                "3) Per effettuare la mammografia utilizza il servizio di screening fornito dalla sua ASL?",
                getFieldValue(fields, "q3_screening_asl")));
        dettagli.add(new QuestionarioDettaglio(
                "4) Se ha risposto No, può spiegare perché?",
                getFieldValue(fields, "q4_motivazione")));
        dettagli.add(new QuestionarioDettaglio(
                "5) Se non aderisce allo screening della ASL, quali altri canali utilizza per effettuare la mammografia?",
                buildCanaliRisposta(fields)));
        dettagli.add(new QuestionarioDettaglio(
                "6) Lei effettua la mammografia preventiva ogni due anni?",
                getFieldValue(fields, "q6_biennale")));
        dettagli.add(new QuestionarioDettaglio(
                "7) Se ha risposto No, con quale intervallo effettua la mammografia e in base a quale indicazione?",
                getIntervalloRisposta(getFieldValue(fields, "q7_intervallo"))));
        dettagli.add(new QuestionarioDettaglio(
                "8) Assieme alla mammografia effettua anche l'ecografia mammaria?",
                getFieldValue(fields, "q8_ecografia")));
        dettagli.add(new QuestionarioDettaglio(
                "9) Per quale motivo effettua anche l'ecografia mammaria?",
                buildEcografiaRisposta(fields)));
        dettagli.add(new QuestionarioDettaglio(
                "10) Ritiene di avere ricevuto informazioni sufficienti sull'importanza della prevenzione mammografica?",
                getFieldValue(fields, "q10_informazioni")));
        return dettagli;
    }

    private List<QuestionarioDettaglio> buildFascia65Dettagli(JsonObject fields) {
        List<QuestionarioDettaglio> dettagli = new ArrayList<>();
        dettagli.add(new QuestionarioDettaglio(
                "Il medico nega adesione al progetto",
                getFieldValue(fields, "rifiuta_medico")));
        dettagli.add(new QuestionarioDettaglio(
                "Non e' stato possibile somministrare il questionario per motivi clinici",
                getFieldValue(fields, "impossibile_somministrare_motivi_clinici")));
        dettagli.add(new QuestionarioDettaglio(
                "Il paziente nega l'adesione al progetto",
                getFieldValue(fields, "nega_adesione_progetto")));
        dettagli.add(new QuestionarioDettaglio(
                "1) Dimentica mai di assumere i suoi farmaci?",
                getFieldValue(fields, "q1_dimentica_farmaci")));
        dettagli.add(new QuestionarioDettaglio(
                "2) E' capitato negli ultimi 14 giorni?",
                getFieldValue(fields, "q2_ultimi_quattordici_giorni")));
        dettagli.add(new QuestionarioDettaglio(
                "3) Ha mai smesso di assumere la terapia di sua iniziativa?",
                getFieldValue(fields, "q3_sospende_iniziativa")));
        dettagli.add(new QuestionarioDettaglio(
                "4) Non ha preso i farmaci ieri?",
                getFieldValue(fields, "q4_non_presi_ieri")));
        dettagli.add(new QuestionarioDettaglio(
                "5) Quando si sente meglio smette di prendere la sua terapia?",
                getFieldValue(fields, "q5_smette_quando_meglio")));
        dettagli.add(new QuestionarioDettaglio(
                "6) Fatica ad attenersi alla terapia prescritta?",
                getFieldValue(fields, "q6_fatica_terapia")));
        dettagli.add(new QuestionarioDettaglio(
                "7) Le capita mai di dimenticarsi le medicine a casa (es. vacanza)?",
                getFieldValue(fields, "q7_dimentica_medicine_casa")));
        dettagli.add(new QuestionarioDettaglio(
                "8) Si scorda mai di assumere parte della sua terapia?",
                getFieldValue(fields, "q8_dimentica_parte_terapia")));
        return dettagli;
    }

    private List<QuestionarioDettaglio> buildFascia625Dettagli(JsonObject fields) {
        List<QuestionarioDettaglio> dettagli = new ArrayList<>();
        dettagli.add(new QuestionarioDettaglio(
                "Il medico nega adesione al progetto",
                getFieldValue(fields, "rifiuta_medico")));
        dettagli.add(new QuestionarioDettaglio(
                "Non e' stato possibile somministrare il questionario per motivi clinici",
                getFieldValue(fields, "impossibile_somministrare_motivi_clinici")));
        dettagli.add(new QuestionarioDettaglio(
                "Il paziente nega l'adesione al progetto",
                getFieldValue(fields, "nega_adesione_progetto")));
        dettagli.add(new QuestionarioDettaglio("1) Non riesco a fare un lavoro pianificato a causa dell'utilizzo del cellulare",
                getScalaAccordoRisposta(getFieldValue(fields, "q1_lavoro_pianificato"))));
        dettagli.add(new QuestionarioDettaglio("2) Ho difficolta' di concentrazione in classe, mentre si fanno i compiti o mentre si sta lavorando, a causa dell'utilizzo del cellulare",
                getScalaAccordoRisposta(getFieldValue(fields, "q2_concentrazione"))));
        dettagli.add(new QuestionarioDettaglio("3) Sento dolore ai polsi, alla schiena o al collo mentre uso il cellulare",
                getScalaAccordoRisposta(getFieldValue(fields, "q3_dolore"))));
        dettagli.add(new QuestionarioDettaglio("4) Non sarei capace di resistere senza il cellulare",
                getScalaAccordoRisposta(getFieldValue(fields, "q4_resistere_senza"))));
        dettagli.add(new QuestionarioDettaglio("5) Mi sento impaziente ed irritabile quando non ho con me il cellulare",
                getScalaAccordoRisposta(getFieldValue(fields, "q5_impaziente_irritabile"))));
        dettagli.add(new QuestionarioDettaglio("6) Ho il mio cellulare in mente anche quando non Io sto usando",
                getScalaAccordoRisposta(getFieldValue(fields, "q6_cellulare_mente"))));
        dettagli.add(new QuestionarioDettaglio("7) Non rinuncerei mai all'uso del cellulare dal momento che la mia vita quotidiana e' molto influenzata da esso",
                getScalaAccordoRisposta(getFieldValue(fields, "q7_non_rinuncia"))));
        dettagli.add(new QuestionarioDettaglio("8) Controllo costantemente il mio cellulare in modo da non perdere le conversazioni tra le altre persone sulle messaggistiche social",
                getScalaAccordoRisposta(getFieldValue(fields, "q8_controllo_costante"))));
        dettagli.add(new QuestionarioDettaglio("9) So di usare il mio cellulare piu' a lungo di quanto dovrei",
                getScalaAccordoRisposta(getFieldValue(fields, "q9_uso_piu_lungo"))));
        dettagli.add(new QuestionarioDettaglio("10) Le persone intorno a me dicono che uso troppo il mio cellulare",
                getScalaAccordoRisposta(getFieldValue(fields, "q10_persone_dicono_troppo"))));
        return dettagli;
    }

    private List<QuestionarioDettaglio> buildFascia2664Dettagli(JsonObject fields) {
        List<QuestionarioDettaglio> dettagli = new ArrayList<>();
        dettagli.add(new QuestionarioDettaglio(
                "Il medico nega adesione al progetto",
                getFieldValue(fields, "rifiuta_medico")));
        dettagli.add(new QuestionarioDettaglio(
                "Non e' stato possibile somministrare il questionario per motivi clinici",
                getFieldValue(fields, "impossibile_somministrare_motivi_clinici")));
        dettagli.add(new QuestionarioDettaglio(
                "Il paziente nega l'adesione al progetto",
                getFieldValue(fields, "nega_adesione_progetto")));
        dettagli.add(new QuestionarioDettaglio("1) Fuma attualmente?", getFieldValue(fields, "q1_fuma")));
        dettagli.add(new QuestionarioDettaglio(
                "Se ha risposto SI, con quale tipologia di abitudine al fumo?",
                getTipologiaFumoRisposta(getFieldValue(fields, "q1_tipologia_fumo"))));
        dettagli.add(new QuestionarioDettaglio("2) Le sono mai stati riscontrati valori alti di PRESSIONE?",
                getFieldValue(fields, "q2_pressione_alta")));
        dettagli.add(new QuestionarioDettaglio("3) Le e' mai stato detto da un medico che il suo peso e' eccessivo?",
                getFieldValue(fields, "q3_peso_eccessivo")));
        dettagli.add(new QuestionarioDettaglio("4) Nei suoi esami del sangue e' mai comparso un valore alto di GLICEMIA?",
                getFieldValue(fields, "q4_glicemia_alta")));
        dettagli.add(new QuestionarioDettaglio("5) Nei suoi esami del sangue e' mai comparso un valore alto di COLESTEROLO?",
                getFieldValue(fields, "q5_colesterolo_alto")));
        dettagli.add(new QuestionarioDettaglio("6) Svolge adeguata attivita' fisica?",
                getFieldValue(fields, "q6_attivita_fisica")));
        dettagli.add(new QuestionarioDettaglio("7) Nella sua Famiglia ci sono stati casi di infarto cardiaco o ictus cerebrale?",
                getFieldValue(fields, "q7_familiarita_eventi")));
        return dettagli;
    }

    private String getRifiuta(JsonObject fields) {
        return "SI".equalsIgnoreCase(getFieldValue(fields, "rifiuta_compilazione")) ? "SI" : "NO";
    }

    private String getRifiutaFascia(JsonObject fields) {
        boolean rifiutaMedico = "SI".equalsIgnoreCase(getFieldValue(fields, "rifiuta_medico"));
        boolean motiviClinici = "SI".equalsIgnoreCase(getFieldValue(fields, "impossibile_somministrare_motivi_clinici"));
        boolean negaAdesione = "SI".equalsIgnoreCase(getFieldValue(fields, "nega_adesione_progetto"));
        if (rifiutaMedico) {
            return RIFIUTA_MEDICO;
        }
        if (motiviClinici) {
            return "SI_CLINICI";
        }
        return negaAdesione ? "SI" : "NO";
    }

    private String getScalaAccordoRisposta(String valore) {
        if ("1".equals(valore)) {
            return "1 - Non sono per nulla d'accordo";
        }
        if ("2".equals(valore)) {
            return "2 - Non sono d'accordo";
        }
        if ("3".equals(valore)) {
            return "3 - Non sono molto d'accordo";
        }
        if ("4".equals(valore)) {
            return "4 - Sono poco d'accordo";
        }
        if ("5".equals(valore)) {
            return "5 - Sono d'accordo";
        }
        if ("6".equals(valore)) {
            return "6 - Sono molto d'accordo";
        }
        return valore == null ? "" : valore.trim();
    }

    private String getTipologiaFumoRisposta(String valore) {
        if ("SIGARETTA_TRADIZIONALE".equals(valore)) {
            return "Sigaretta tradizionale";
        }
        if ("SIGARETTA_ELETTRONICA".equals(valore)) {
            return "Sigaretta elettronica";
        }
        if ("ALTRO".equals(valore)) {
            return "Altro";
        }
        return valore == null ? "" : valore.trim();
    }

    private JsonObject getQuestionarioFields(String questionario) {
        JsonElement rootElement = new JsonParser().parse(questionario);
        if (!rootElement.isJsonObject()) {
            throw new IllegalArgumentException("Il questionario non contiene un oggetto JSON.");
        }
        JsonObject root = rootElement.getAsJsonObject();
        JsonElement fieldsElement = root.get("fields");
        if (fieldsElement != null && fieldsElement.isJsonObject()) {
            return fieldsElement.getAsJsonObject();
        }
        return root;
    }

    private String getFieldValue(JsonObject fields, String fieldName) {
        if (fields == null || fieldName == null) {
            return "";
        }
        JsonElement value = fields.get(fieldName);
        if (value == null || value.isJsonNull()) {
            return "";
        }
        if (value.isJsonArray()) {
            List<String> values = new ArrayList<>();
            for (JsonElement item : value.getAsJsonArray()) {
                String normalized = item == null || item.isJsonNull() ? "" : item.getAsString().trim();
                if (!normalized.isEmpty()) {
                    values.add(normalized);
                }
            }
            return joinValues(values);
        }
        return value.getAsString().trim();
    }

    private String buildCanaliRisposta(JsonObject fields) {
        List<String> canali = new ArrayList<>();
        JsonElement selectedElement = fields.get("q5_canali");
        if (selectedElement != null && selectedElement.isJsonArray()) {
            for (JsonElement item : selectedElement.getAsJsonArray()) {
                String codice = item == null || item.isJsonNull() ? "" : item.getAsString().trim();
                String descrizione = getCanaleDescrizione(codice);
                if (!descrizione.isEmpty()) {
                    canali.add(descrizione);
                }
            }
        } else {
            String codice = getFieldValue(fields, "q5_canali");
            String descrizione = getCanaleDescrizione(codice);
            if (!descrizione.isEmpty()) {
                canali.add(descrizione);
            }
        }
        String altro = getFieldValue(fields, "q5_altro");
        if (!altro.isEmpty()) {
            canali.add("Altro: " + altro);
        }
        return joinValues(canali);
    }

    private String getCanaleDescrizione(String codice) {
        if ("PRIVATAMENTE".equalsIgnoreCase(codice)) {
            return "Esame eseguito privatamente";
        }
        if ("ASSICURAZIONE".equalsIgnoreCase(codice)) {
            return "Esame eseguito attraverso polizze assicurative";
        }
        if ("OSPEDALE_ASL".equalsIgnoreCase(codice)) {
            return "Esame prenotato in Ospedale o alla ASL";
        }
        if ("CENTRI_CONVENZIONATI".equalsIgnoreCase(codice)) {
            return "Esame prenotato nei centri privati convenzionati con la ASL";
        }
        if ("ALTRO".equalsIgnoreCase(codice)) {
            return "Altro";
        }
        return codice == null ? "" : codice.trim();
    }

    private String getIntervalloRisposta(String codice) {
        if ("ANNUALE_REFERTO".equalsIgnoreCase(codice)) {
            return "Ogni anno come indicato sul referto della mammografia";
        }
        if ("ANNUALE_TRANQUILLITA".equalsIgnoreCase(codice)) {
            return "Ogni anno per tranquillità";
        }
        if ("ANNUALE_INDICAZIONE".equalsIgnoreCase(codice)) {
            return "Ogni anno su indicazione del medico";
        }
        if ("ALTRO".equalsIgnoreCase(codice)) {
            return "Altro";
        }
        return codice == null ? "" : codice.trim();
    }

    private String buildEcografiaRisposta(JsonObject fields) {
        String codice = getFieldValue(fields, "q9_motivo_ecografia");
        String risposta;
        if ("REFERTO".equalsIgnoreCase(codice)) {
            risposta = "Come indicato sul referto della mammografia";
        } else if ("TRANQUILLITA".equalsIgnoreCase(codice)) {
            risposta = "Per tranquillità";
        } else if ("INDICAZIONE".equalsIgnoreCase(codice)) {
            risposta = "Su indicazione del medico";
        } else {
            risposta = codice;
        }
        String indicazione = getFieldValue(fields, "q9_indicazione");
        if (!indicazione.isEmpty()) {
            risposta = (risposta.isEmpty() ? "" : risposta + " - ") + indicazione;
        }
        return risposta;
    }

    private String joinValues(List<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(value.trim());
        }
        return builder.toString();
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignore) {
        }
    }

    private static final class QuestionarioDettaglio {

        private final String domanda;
        private final String risposta;

        private QuestionarioDettaglio(String domanda, String risposta) {
            this.domanda = domanda == null ? "" : domanda;
            this.risposta = risposta == null ? "" : risposta;
        }
    }

    public Map<String, Object> findLatestMammografia(String codPaz) throws SQLException, IOException {
        return findLatestQuestionario(codPaz, TIPO_MAMMOGRAFIA);
    }

    public Map<String, Object> findLatestFascia65(String codPaz) throws SQLException, IOException {
        return findLatestQuestionario(codPaz, TIPO_FASCIA65);
    }

    public Map<String, Object> findLatestFascia625(String codPaz) throws SQLException, IOException {
        return findLatestQuestionario(codPaz, TIPO_FASCIA625);
    }

    public Map<String, Object> findLatestFascia2664(String codPaz) throws SQLException, IOException {
        return findLatestQuestionario(codPaz, TIPO_FASCIA2664);
    }

    private Map<String, Object> findLatestQuestionario(String codPaz, String tipo) throws SQLException, IOException {
        String sql = "SELECT ID, COD_PAZ, DATA, UTENTE_INSERT, TIPO, RIFIUTA, QUESTIONARIO "
                + "FROM APO_QUESTIONARI "
                + "WHERE COD_PAZ = ? AND TIPO = ? "
                + "ORDER BY DATA DESC";

        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codPaz);
            statement.setString(2, tipo);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                return mapQuestionario(resultSet);
            }
        }
    }

    public Map<String, Object> findMammografiaById(String codPaz, long idQuestionario) throws SQLException, IOException {
        return findQuestionarioById(codPaz, idQuestionario, TIPO_MAMMOGRAFIA);
    }

    public Map<String, Object> findFascia65ById(String codPaz, long idQuestionario) throws SQLException, IOException {
        return findQuestionarioById(codPaz, idQuestionario, TIPO_FASCIA65);
    }

    public Map<String, Object> findFascia625ById(String codPaz, long idQuestionario) throws SQLException, IOException {
        return findQuestionarioById(codPaz, idQuestionario, TIPO_FASCIA625);
    }

    public Map<String, Object> findFascia2664ById(String codPaz, long idQuestionario) throws SQLException, IOException {
        return findQuestionarioById(codPaz, idQuestionario, TIPO_FASCIA2664);
    }

    private Map<String, Object> findQuestionarioById(String codPaz, long idQuestionario, String tipo) throws SQLException, IOException {
        String sql = "SELECT ID, COD_PAZ, DATA, UTENTE_INSERT, TIPO, RIFIUTA, QUESTIONARIO "
                + "FROM APO_QUESTIONARI "
                + "WHERE COD_PAZ = ? AND ID = ? AND TIPO = ?";
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codPaz);
            statement.setLong(2, idQuestionario);
            statement.setString(3, tipo);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapQuestionario(resultSet) : null;
            }
        }
    }

    public List<Map<String, Object>> findMammografieByPaziente(String codPaz) throws SQLException {
        return findQuestionariByPaziente(codPaz);
    }

    public List<Map<String, Object>> findQuestionariByPaziente(String codPaz) throws SQLException {
        String sql = "SELECT q.ID, q.DATA, q.UTENTE_INSERT, q.TIPO, q.RIFIUTA "
                + "FROM APO_QUESTIONARI q "
                + "WHERE q.COD_PAZ = ? AND q.TIPO IN ('MAMMO', 'FASCIA65', 'FASCIA625', 'FASCIA2664') "
                + "ORDER BY DATA DESC";
        List<Map<String, Object>> questionari = new ArrayList<>();
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codPaz);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    HashMap<String, Object> questionario = new HashMap<>();
                    questionario.put("idQuestionario", Long.valueOf(resultSet.getLong("ID")));
                    questionario.put("dataInserimento", resultSet.getTimestamp("DATA"));
                    questionario.put("utenteInsert", resultSet.getString("UTENTE_INSERT"));
                    questionario.put("tipo", resultSet.getString("TIPO"));
                    questionario.put("rifiuta", resultSet.getString("RIFIUTA"));
                    questionari.add(questionario);
                }
            }
        }
        return questionari;
    }

    /**
     * Per la stessa consulenza il questionario non viene mai riaperto
     * automaticamente, anche in caso di rifiuto. Nelle consulenze successive,
     * invece, bloccano la riproposizione soltanto i questionari somministrati
     * (RIFIUTA = NO) negli ultimi 365 giorni.
     */
    public boolean isQuestionarioApribileAutomaticamente(String codPaz, String codConsulenza) throws SQLException {
        String sql = "SELECT 1 "
                + "FROM APO_QUESTIONARI q "
                + "WHERE q.COD_PAZ = ? "
                + "AND (q.COD_CONSULENZA = ? "
                + "     OR (q.DATA > SYSDATE - 365 "
                + "         AND NVL(UPPER(TRIM(q.RIFIUTA)), 'NO') = 'NO')) "
                + "AND ROWNUM = 1";
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codPaz);
            statement.setString(2, codConsulenza);
            try (ResultSet resultSet = statement.executeQuery()) {
                return !resultSet.next();
            }
        }
    }

    /**
     * Il questionario rimane valido per 365 giorni soltanto se, nel dettaglio
     * della domanda sul rifiuto (progressivo 1), la risposta e' NO.
     * Per gli eventuali record precedenti privi di dettaglio viene usato il
     * campo di testata RIFIUTA come compatibilita' retroattiva.
     */
    public boolean isMammografiaCompilabile(String codPaz) throws SQLException {
        return isQuestionarioCompilabile(codPaz, TIPO_MAMMOGRAFIA);
    }

    public boolean isFascia65Compilabile(String codPaz) throws SQLException {
        return !hasFasciaRecenteConRifiuta(codPaz, TIPO_FASCIA65, "NO");
    }

    public boolean hasFascia65RifiutatoRecente(String codPaz) throws SQLException {
        return hasFasciaRecenteConRifiuta(codPaz, TIPO_FASCIA65, "SI");
    }

    public boolean isFascia625Compilabile(String codPaz) throws SQLException {
        return !hasFasciaRecenteConRifiuta(codPaz, TIPO_FASCIA625, "NO");
    }

    public boolean hasFascia625RifiutatoRecente(String codPaz) throws SQLException {
        return hasFasciaRecenteConRifiuta(codPaz, TIPO_FASCIA625, "SI");
    }

    public boolean isFascia2664Compilabile(String codPaz) throws SQLException {
        return !hasFasciaRecenteConRifiuta(codPaz, TIPO_FASCIA2664, "NO");
    }

    public boolean hasFascia2664RifiutatoRecente(String codPaz) throws SQLException {
        return hasFasciaRecenteConRifiuta(codPaz, TIPO_FASCIA2664, "SI");
    }

    private boolean hasFasciaRecenteConRifiuta(String codPaz, String tipo, String rifiuta) throws SQLException {
        String sql = "SELECT 1 "
                + "FROM APO_QUESTIONARI q "
                + "WHERE q.COD_PAZ = ? AND q.TIPO = ? "
                + "AND q.DATA >= SYSDATE - 365 "
                + "AND NVL(UPPER(TRIM(q.RIFIUTA)), 'NO') = ? "
                + "AND ROWNUM = 1";
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codPaz);
            statement.setString(2, tipo);
            statement.setString(3, rifiuta);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean isQuestionarioCompilabile(String codPaz, String tipo) throws SQLException {
        String sql = "SELECT 1 "
                + "FROM APO_QUESTIONARI q "
                + "WHERE q.COD_PAZ = ? AND q.TIPO = ? "
                + "AND q.DATA >= SYSDATE - 365 "
                + "AND ("
                + "    EXISTS (SELECT 1 FROM APO_QUESTIONARI_DETT d "
                + "            WHERE d.IDQUESTIONARIO = q.ID "
                + "            AND d.PROGRESSIVO = 1 "
                + "            AND UPPER(TRIM(d.RISPOSTA)) = 'NO') "
                + "    OR (NOT EXISTS (SELECT 1 FROM APO_QUESTIONARI_DETT d "
                + "                    WHERE d.IDQUESTIONARIO = q.ID "
                + "                    AND d.PROGRESSIVO = 1) "
                + "        AND NVL(UPPER(TRIM(q.RIFIUTA)), 'NO') = 'NO')"
                + ") AND ROWNUM = 1";
        try (Connection connection = dataService.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codPaz);
            statement.setString(2, tipo);
            try (ResultSet resultSet = statement.executeQuery()) {
                return !resultSet.next();
            }
        }
    }

    private Map<String, Object> mapQuestionario(ResultSet resultSet) throws SQLException, IOException {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idQuestionario", Long.valueOf(resultSet.getLong("ID")));
        result.put("codPaz", resultSet.getString("COD_PAZ"));
        result.put("dataInserimento", resultSet.getTimestamp("DATA"));
        result.put("utenteInsert", resultSet.getString("UTENTE_INSERT"));
        result.put("tipo", resultSet.getString("TIPO"));
        result.put("rifiuta", resultSet.getString("RIFIUTA"));
        result.put("questionario", readBlob(resultSet.getBlob("QUESTIONARIO")));
        return result;
    }

    private String readBlob(Blob blob) throws SQLException, IOException {
        if (blob == null) {
            return "";
        }
        long length = blob.length();
        if (length <= 0) {
            return "";
        }
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Questionario troppo grande.");
        }
        byte[] bytes = blob.getBytes(1L, (int) length);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
