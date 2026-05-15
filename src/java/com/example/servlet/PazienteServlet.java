package com.example.servlet;

import com.example.dao.PazienteDao;
import com.example.dto.ApiResponse;
import com.example.service.DataService;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PazienteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PazienteServlet.class.getName());
    private PazienteDao pazienteDao;

    @Override
    public void init() {
        String jndiName = trimToNull(getServletContext().getInitParameter("dbJndi"));
        String dbDriver = trimToNull(getServletContext().getInitParameter("dbDriver"));
        String dbUrl = trimToNull(getServletContext().getInitParameter("dbUrl"));
        String dbUser = trimToNull(getServletContext().getInitParameter("dbUser"));
        String dbPassword = trimToNull(getServletContext().getInitParameter("dbPassword"));

        DataService dataService = new DataService(jndiName, dbDriver, dbUrl, dbUser, dbPassword);
        pazienteDao = new PazienteDao(dataService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/medico-curante".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadMedicoCurante(req, resp);
            return;
        }
        if ("/consenso-dse".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadConsensoDse(req, resp);
            return;
        }
        if ("/anagrafe/search".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadAnagrafe(req, resp);
            return;
        }
        if ("/codice-fiscale".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            calculateCodiceFiscale(req, resp);
            return;
        }
        if ("/comuni".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadComuni(req, resp);
            return;
        }
        if ("/asl".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadAsl(req, resp);
            return;
        }
        if ("/prestazioni-infermieristiche".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadPrestazioniInfermieristiche(req, resp);
            return;
        }
        if ("/prestazioni-infermieristiche/categorie".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadPrestazioniInfermieristicheCategorie(req, resp);
            return;
        }
        if ("/prestazioni-infermieristiche/sottocategorie".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadPrestazioniInfermieristicheSottocategorie(req, resp);
            return;
        }
        if ("/icd10/patologie-croniche".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadPatologieCronicheIcd10(req, resp);
            return;
        }
        if ("/scheda-paziente".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadSchedaPaziente(req, resp);
            return;
        }
        if ("/scheda-paziente/diario".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadSchedaPazienteDiario(req, resp);
            return;
        }
        if ("/scheda-paziente/patologie".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadSchedaPazientePatologie(req, resp);
            return;
        }
        if ("/ricette-farmaci".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadRicetteFarmaci(req, resp);
            return;
        }
        if ("/ricette-dematerializzate".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadRicetteDematerializzate(req, resp);
            return;
        }
        if ("/piani-terapeutici".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadPianiTerapeutici(req, resp);
            return;
        }
        if ("/allegato-m".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadAllegatoM(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione GET non valida");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/anagrafe/new".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            createAnagrafe(req, resp);
            return;
        }
        if ("/allegato-m".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            saveAllegatoM(req, resp);
            return;
        }
        if ("/scheda-paziente/diario".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            saveSchedaPazienteDiario(req, resp);
            return;
        }
        if ("/scheda-paziente/patologie".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            saveSchedaPazientePatologia(req, resp);
            return;
        }
        if ("/prestazioni-infermieristiche".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            savePrestazioniInfermieristiche(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione POST non valida");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/scheda-paziente/diario".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            deleteSchedaPazienteDiario(req, resp);
            return;
        }
        if ("/scheda-paziente/patologie".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            deleteSchedaPazientePatologia(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione DELETE non valida");
    }

    private void loadMedicoCurante(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }

        try {
            Map<String, String> medicoCurante = pazienteDao.findMedicoCurante(codiceFiscale);
            HashMap<String, Object> data = new HashMap<>();
            data.put("medicoCurante", medicoCurante);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del medico curante.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del medico curante.")));
        }
    }

    private void loadConsensoDse(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }

        try {
            String consensoHtml = pazienteDao.findConsensoDseHtml(codiceFiscale);
            HashMap<String, Object> data = new HashMap<>();
            data.put("html", consensoHtml);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del consenso DSE.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del consenso DSE.")));
        }
    }

    private void loadAnagrafe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        String cognome = trimToNull(req.getParameter("cognome"));
        String nome = trimToNull(req.getParameter("nome"));
        if (codiceFiscale == null && cognome == null && nome == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Valorizza almeno un filtro di ricerca."));
            return;
        }

        try {
            List<Map<String, String>> pazienti = pazienteDao.searchAnagrafe(codiceFiscale, cognome, nome);
            HashMap<String, Object> data = new HashMap<>();
            data.put("pazienti", pazienti);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nella ricerca anagrafica.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nella ricerca anagrafica.")));
        }
    }

    private void calculateCodiceFiscale(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String cognome = trimToNull(req.getParameter("cognome"));
        String nome = trimToNull(req.getParameter("nome"));
        String dataNascita = trimToNull(req.getParameter("dataNascita"));
        String sesso = trimToNull(req.getParameter("sesso"));
        String comuneNascita = firstNonNull(
                trimToNull(req.getParameter("codComuneNascita")),
                trimToNull(req.getParameter("comuneNascita")));
        if (cognome == null || nome == null || dataNascita == null || sesso == null || comuneNascita == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Cognome, nome, data nascita, sesso e comune nascita sono obbligatori."));
            return;
        }

        try {
            String codiceFiscale = pazienteDao.calculateCodiceFiscale(cognome, nome, dataNascita, sesso, comuneNascita);
            HashMap<String, Object> data = new HashMap<>();
            data.put("codiceFiscale", codiceFiscale);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel calcolo del codice fiscale.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel calcolo del codice fiscale.")));
        }
    }

    private void loadComuni(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        try {
            List<Map<String, String>> comuni = pazienteDao.loadComuniSuggerimenti();
            HashMap<String, Object> data = new HashMap<>();
            data.put("comuni", comuni);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dei comuni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento dei comuni.")));
        }
    }

    private void loadAsl(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        try {
            List<Map<String, String>> asl = pazienteDao.loadAslSuggerimenti();
            HashMap<String, Object> data = new HashMap<>();
            data.put("asl", asl);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle ASL.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle ASL.")));
        }
    }

    private void createAnagrafe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> payload = readRequestParameters(req);
        String missingField = findMissingCensimentoField(payload);
        if (missingField != null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Campo obbligatorio non valorizzato: " + missingField + "."));
            return;
        }

        String sesso = safeString(payload.get("sesso")).toUpperCase();
        if (!"M".equals(sesso) && !"F".equals(sesso)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Sesso non valido."));
            return;
        }

        String codiceFiscale = safeString(payload.get("codiceFiscale")).toUpperCase();
        if (codiceFiscale.length() != 16) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Il codice fiscale deve essere di 16 caratteri."));
            return;
        }
        if (trimToNull(payload.get("comuneDomicilio")) != null
                && trimToNull(payload.get("codComuneDomicilio")) == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Seleziona il Comune domicilio dai suggerimenti."));
            return;
        }

        try {
            HashMap<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
            Map<String, String> paziente = pazienteDao.createAnagrafePaziente(
                    payload,
                    safeString(session.getAttribute("authUser")),
                    safeString(req.getRemoteAddr()),
                    safeString(serviceData.get("codServizio")));
            HashMap<String, Object> data = new HashMap<>();
            data.put("paziente", paziente);
            JsonUtil.write(resp, ApiResponse.ok(data, "Paziente censito correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel censimento del paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel censimento del paziente.")));
        }
    }

    private void loadPrestazioniInfermieristiche(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }

        try {
            List<Map<String, String>> prestazioni = pazienteDao.findPrestazioniInfermieristiche(codiceFiscale);
            HashMap<String, Object> data = new HashMap<>();
            data.put("prestazioni", prestazioni);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle prestazioni infermieristiche.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle prestazioni infermieristiche.")));
        }
    }

    private void loadPrestazioniInfermieristicheCategorie(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
        String codServizio = trimToNull(serviceData.get("codServizio"));
        if (codServizio == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Servizio loggato non disponibile."));
            return;
        }

        try {
            List<Map<String, String>> categorie = pazienteDao.loadPrestazioniInfermieristicheCategorie(codServizio);
            HashMap<String, Object> data = new HashMap<>();
            data.put("categorie", categorie);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle categorie prestazioni infermieristiche.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle categorie.")));
        }
    }

    private void loadPrestazioniInfermieristicheSottocategorie(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
        String categoria = trimToNull(req.getParameter("categoria"));
        if ("__ICD10_PATOLOGIE_CRONICHE__".equals(categoria)) {
            loadPatologieCronicheIcd10(req, resp);
            return;
        }

        String codServizio = trimToNull(serviceData.get("codServizio"));
        if (codServizio == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Servizio loggato non disponibile."));
            return;
        }
        if (categoria == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Categoria non valorizzata."));
            return;
        }

        try {
            List<Map<String, String>> sottocategorie = pazienteDao.loadPrestazioniInfermieristicheSottocategorie(codServizio, categoria);
            HashMap<String, Object> data = new HashMap<>();
            data.put("sottocategorie", sottocategorie);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle sottocategorie prestazioni infermieristiche.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle sottocategorie.")));
        }
    }

    private void loadPatologieCronicheIcd10(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String filtro = trimToNull(req.getParameter("filtro"));
        if (filtro == null) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("patologie", new ArrayList<Map<String, String>>());
            JsonUtil.write(resp, ApiResponse.ok(data));
            return;
        }

        try {
            List<Map<String, String>> patologie = pazienteDao.searchPatologieCronicheIcd10(filtro);
            HashMap<String, Object> data = new HashMap<>();
            data.put("patologie", patologie);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle patologie croniche ICD-10.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle patologie croniche.")));
        }
    }

    private void loadSchedaPaziente(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            Map<String, Object> scheda = pazienteDao.findLatestSchedaPaziente(codPaz);
            HashMap<String, Object> data = new HashMap<>();
            data.put("exists", Boolean.valueOf(scheda != null));
            data.put("scheda", scheda == null ? new HashMap<String, Object>() : scheda);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento della scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento della scheda paziente.")));
        }
    }

    private void loadSchedaPazienteDiario(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            List<Map<String, String>> diario = pazienteDao.findSchedaPazienteDiario(codPaz);
            HashMap<String, Object> data = new HashMap<>();
            data.put("diario", diario);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del diario scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del diario.")));
        }
    }

    private void saveSchedaPazienteDiario(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String username = trimToNull(safeString(session.getAttribute("authUser")));
        String codPaz = trimToNull(req.getParameter("codPaz"));
        String dataDiario = trimToNull(req.getParameter("dataDiario"));
        String descrizione = trimToNull(req.getParameter("descrizione"));
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }
        if (dataDiario == null || !dataDiario.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Data diario non valida."));
            return;
        }
        if (descrizione == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Descrizione diario non valorizzata."));
            return;
        }

        try {
            int inserted = pazienteDao.insertSchedaPazienteDiario(codPaz, dataDiario, descrizione, username);
            HashMap<String, Object> data = new HashMap<>();
            data.put("inserted", Integer.valueOf(inserted));
            JsonUtil.write(resp, ApiResponse.ok(data, "Diario aggiunto correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio del diario scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio del diario.")));
        }
    }

    private void deleteSchedaPazienteDiario(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String idDiario = trimToNull(req.getParameter("id"));
        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (idDiario == null) {
            idDiario = trimToNull(req.getParameter("idDiario"));
        }
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (idDiario == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("ID diario non valorizzato."));
            return;
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            int deleted = pazienteDao.deleteSchedaPazienteDiario(idDiario, codPaz);
            HashMap<String, Object> data = new HashMap<>();
            data.put("deleted", Integer.valueOf(deleted));
            JsonUtil.write(resp, ApiResponse.ok(data,
                    deleted > 0 ? "Diario eliminato correttamente." : "Diario gia' eliminato."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nell'eliminazione del diario scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nell'eliminazione del diario.")));
        }
    }

    private void loadSchedaPazientePatologie(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            List<Map<String, String>> patologie = pazienteDao.findSchedaPazientePatologie(codPaz);
            HashMap<String, Object> data = new HashMap<>();
            data.put("patologie", patologie);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle patologie scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle patologie croniche.")));
        }
    }

    private void saveSchedaPazientePatologia(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String username = trimToNull(safeString(session.getAttribute("authUser")));
        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codicePatologia = trimToNull(req.getParameter("codicePatologia"));
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (codicePatologia == null) {
            codicePatologia = trimToNull(req.getParameter("codice"));
        }
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }
        if (codicePatologia == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice patologia non valorizzato."));
            return;
        }

        try {
            int inserted = pazienteDao.insertSchedaPazientePatologia(codPaz, codicePatologia, username);
            HashMap<String, Object> data = new HashMap<>();
            data.put("inserted", Integer.valueOf(inserted));
            JsonUtil.write(resp, ApiResponse.ok(data,
                    inserted > 0 ? "Patologia cronica aggiunta correttamente." : "Patologia cronica gia' presente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio della patologia scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio della patologia cronica.")));
        }
    }

    private void deleteSchedaPazientePatologia(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String idPatologia = trimToNull(req.getParameter("id"));
        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (idPatologia == null) {
            idPatologia = trimToNull(req.getParameter("idPatologia"));
        }
        if (codPaz == null) {
            codPaz = trimToNull(req.getParameter("codPaziente"));
        }
        if (idPatologia == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("ID patologia non valorizzato."));
            return;
        }
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            int deleted = pazienteDao.deleteSchedaPazientePatologia(idPatologia, codPaz);
            HashMap<String, Object> data = new HashMap<>();
            data.put("deleted", Integer.valueOf(deleted));
            JsonUtil.write(resp, ApiResponse.ok(data,
                    deleted > 0 ? "Patologia cronica eliminata correttamente." : "Patologia cronica gia' eliminata."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nell'eliminazione della patologia scheda paziente.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nell'eliminazione della patologia cronica.")));
        }
    }

    private void savePrestazioniInfermieristiche(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
        String operatore = trimToNull(safeString(session.getAttribute("authUser")));
        String codServizio = trimToNull(serviceData.get("codServizio"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        String note = safeString(req.getParameter("note"));
        List<String> idCategorie = new ArrayList<>();
        String[] rawIdCategorie = req.getParameterValues("idCategoria");

        if (codConsulenza == null) {
            codConsulenza = trimToNull(req.getParameter("idConsulenza"));
        }
        if (codiceFiscale == null) {
            codiceFiscale = trimToNull(req.getParameter("codFiscale"));
        }
        if (rawIdCategorie == null || rawIdCategorie.length == 0) {
            rawIdCategorie = req.getParameterValues("idCategoria[]");
        }
        if (rawIdCategorie != null) {
            for (String rawIdCategoria : rawIdCategorie) {
                String idCategoria = trimToNull(rawIdCategoria);
                if (idCategoria != null && !idCategorie.contains(idCategoria)) {
                    idCategorie.add(idCategoria);
                }
            }
        }

        if (operatore == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }
        if (codServizio == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Servizio loggato non disponibile."));
            return;
        }
        if (codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice consulenza non valorizzato."));
            return;
        }
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }
        if (idCategorie.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Seleziona almeno una sottocategoria."));
            return;
        }

        try {
            int inserted = pazienteDao.insertPrestazioniInfermieristiche(
                    idCategorie,
                    operatore,
                    codServizio,
                    codConsulenza,
                    codiceFiscale,
                    note);
            HashMap<String, Object> data = new HashMap<>();
            data.put("inserted", Integer.valueOf(inserted));
            JsonUtil.write(resp, ApiResponse.ok(data, "Prestazioni infermieristiche aggiunte correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio delle prestazioni infermieristiche.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio delle prestazioni infermieristiche.")));
        }
    }

    private void loadRicetteFarmaci(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }

        try {
            List<Map<String, String>> ricette = pazienteDao.findRicetteFarmaci(codiceFiscale);
            HashMap<String, Object> data = new HashMap<>();
            data.put("ricette", ricette);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle ricette farmaci.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle ricette farmaci.")));
        }
    }

    private void loadRicetteDematerializzate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }

        try {
            List<Map<String, String>> ricette = pazienteDao.findRicetteDematerializzate(codiceFiscale);
            HashMap<String, Object> data = new HashMap<>();
            data.put("ricette", ricette);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento delle ricette dematerializzate.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento delle ricette dematerializzate.")));
        }
    }

    private void loadPianiTerapeutici(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codiceFiscale"));
        if (codiceFiscale == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice fiscale paziente non valorizzato."));
            return;
        }

        try {
            List<Map<String, String>> piani = pazienteDao.findPianiTerapeutici(codiceFiscale);
            HashMap<String, Object> data = new HashMap<>();
            data.put("piani", piani);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dei piani terapeutici.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento dei piani terapeutici.")));
        }
    }

    private void loadAllegatoM(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String codServizio = trimToNull(req.getParameter("codServizio"));
        if (codConsulenza == null) {
            codConsulenza = trimToNull(req.getParameter("COD_CONSULENZA"));
        }
        if (codServizio == null) {
            codServizio = trimToNull(req.getParameter("COD_SERVIZIO"));
        }
        if (codConsulenza == null || codServizio == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice consulenza o codice servizio non valorizzato."));
            return;
        }

        try {
            Map<String, String> allegatoM = pazienteDao.findAllegatoM(codConsulenza, codServizio);
            HashMap<String, Object> data = new HashMap<>();
            data.put("allegatoM", allegatoM);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dell'Allegato M.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento dell'Allegato M.")));
        }
    }

    private void saveAllegatoM(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> allegato = readRequestParameters(req);
        String codPaz = trimToNull(allegato.get("codPaz"));
        String codConsulenza = trimToNull(allegato.get("codConsulenza"));
        String codServizio = trimToNull(allegato.get("codServizio"));
        String username = trimToNull(safeString(session.getAttribute("authUser")));

        if (codPaz == null || codConsulenza == null || codServizio == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente, codice consulenza o codice servizio non valorizzato."));
            return;
        }
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }

        try {
            Map<String, String> existingAllegatoM = pazienteDao.findAllegatoM(codConsulenza, codServizio);
            if (!canSaveAllegatoM(existingAllegatoM, username)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.write(resp, ApiResponse.ko("Allegato M gia' salvato da un altro utente."));
                return;
            }

            String operation = pazienteDao.saveAllegatoM(
                    allegato,
                    username,
                    safeString(req.getRemoteAddr()));
            Map<String, String> allegatoM = pazienteDao.findAllegatoM(codConsulenza, codServizio);
            HashMap<String, Object> data = new HashMap<>();
            data.put("operation", operation);
            data.put("allegatoM", allegatoM);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio dell'Allegato M.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio dell'Allegato M.")));
        }
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("authUser") != null;
    }

    private void setNoCache(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
    }

    private String normalizePath(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }

    private String firstNonNull(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String getAllegatoMSavedBy(Map<String, String> allegato) {
        if (allegato == null || allegato.isEmpty()) {
            return null;
        }

        return firstNonBlank(
                allegato.get("modUtente"),
                allegato.get("codUtente"),
                allegato.get("utente"),
                allegato.get("username"));
    }

    private boolean canSaveAllegatoM(Map<String, String> existingAllegato, String username) {
        if (existingAllegato == null || existingAllegato.isEmpty()) {
            return true;
        }

        String savedBy = getAllegatoMSavedBy(existingAllegato);
        String currentUser = trimToNull(username);
        return savedBy != null && currentUser != null && savedBy.equalsIgnoreCase(currentUser);
    }

    private HashMap<String, String> readRequestParameters(HttpServletRequest req) {
        HashMap<String, String> parameters = new HashMap<>();
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String key = safeString(entry.getKey());
            String[] values = entry.getValue();
            if (!key.isEmpty() && values != null && values.length > 0) {
                parameters.put(key, safeString(values[0]));
            }
        }
        return parameters;
    }

    private String findMissingCensimentoField(Map<String, String> payload) {
        String[][] requiredFields = {
            {"codiceFiscale", "Codice fiscale"},
            {"cognome", "Cognome"},
            {"nome", "Nome"},
            {"dataNascita", "Data nascita"},
            {"sesso", "Sesso"},
            {"codComuneNascita", "Comune nascita"},
            {"indirizzoResidenza", "Indirizzo residenza"},
            {"codComuneResidenza", "Comune residenza"},
            {"capResidenza", "Cap residenza"},
            {"codAslResidenza", "Asl residenza"},
            {"codAslIscrizione", "Asl iscrizione"}
        };

        for (String[] field : requiredFields) {
            if (trimToNull(payload.get(field[0])) == null) {
                return field[1];
            }
        }
        return null;
    }

    private HashMap<String, String> toStringMap(Object value) {
        HashMap<String, String> result = new HashMap<>();
        if (!(value instanceof Map<?, ?>)) {
            return result;
        }

        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            result.put(String.valueOf(entry.getKey()), safeString(entry.getValue()));
        }

        return result;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String buildErrorMessage(Exception ex, String fallbackMessage) {
        String message = ex == null ? "" : safeString(ex.getMessage());
        if (message.isEmpty() && ex != null && ex.getCause() != null) {
            message = safeString(ex.getCause().getMessage());
        }
        if (message.isEmpty()) {
            return fallbackMessage;
        }
        String prefix = fallbackMessage.endsWith(".") ? fallbackMessage.substring(0, fallbackMessage.length() - 1) : fallbackMessage;
        return prefix + ": " + message;
    }
}
