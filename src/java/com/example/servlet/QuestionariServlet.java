package com.example.servlet;

import com.example.dao.QuestionariDao;
import com.example.dto.ApiResponse;
import com.example.service.DataService;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class QuestionariServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(QuestionariServlet.class.getName());
    private QuestionariDao questionariDao;

    @Override
    public void init() {
        String jndiName = trimToNull(getServletContext().getInitParameter("dbJndi"));
        String dbDriver = trimToNull(getServletContext().getInitParameter("dbDriver"));
        String dbUrl = trimToNull(getServletContext().getInitParameter("dbUrl"));
        String dbUser = trimToNull(getServletContext().getInitParameter("dbUser"));
        String dbPassword = trimToNull(getServletContext().getInitParameter("dbPassword"));
        questionariDao = new QuestionariDao(new DataService(jndiName, dbDriver, dbUrl, dbUser, dbPassword));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Il caricamento del questionario richiede una richiesta POST");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = normalizePath(req.getPathInfo());
        if (!"/mammo".equals(pathInfo) && !"/fascia65".equals(pathInfo) && !"/fascia6-25".equals(pathInfo) && !"/fascia26-64".equals(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione POST non valida");
            return;
        }
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        String requestMode = trimToNull(req.getParameter("_requestMode"));
        if (!isQuestionariReadRequest(requestMode)) {
            HttpSession session = req.getSession(false);
            if (!isAuthenticated(session)) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
                return;
            }
            if (!isQuestionariMedico(session)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                JsonUtil.write(resp, ApiResponse.ko("I questionari sono compilabili solo da utenti medici."));
                return;
            }
        }
        if ("/fascia65".equals(pathInfo)) {
            if ("availability".equalsIgnoreCase(requestMode)) {
                loadFascia65Disponibilita(req, resp);
            } else if ("load".equalsIgnoreCase(requestMode)) {
                loadFascia65(req, resp);
            } else if ("rifiuto-medico".equalsIgnoreCase(requestMode)) {
                saveFasciaRifiutoMedico(req, resp, "FASCIA65", "fascia dai 65 anni");
            } else {
                saveFascia65(req, resp);
            }
            return;
        }
        if ("/fascia6-25".equals(pathInfo)) {
            if ("availability".equalsIgnoreCase(requestMode)) {
                loadFascia625Disponibilita(req, resp);
            } else if ("load".equalsIgnoreCase(requestMode)) {
                loadFascia625(req, resp);
            } else if ("rifiuto-medico".equalsIgnoreCase(requestMode)) {
                saveFasciaRifiutoMedico(req, resp, "FASCIA625", "fascia 6-25 anni");
            } else {
                saveFascia625(req, resp);
            }
            return;
        }
        if ("/fascia26-64".equals(pathInfo)) {
            if ("availability".equalsIgnoreCase(requestMode)) {
                loadFascia2664Disponibilita(req, resp);
            } else if ("load".equalsIgnoreCase(requestMode)) {
                loadFascia2664(req, resp);
            } else if ("rifiuto-medico".equalsIgnoreCase(requestMode)) {
                saveFasciaRifiutoMedico(req, resp, "FASCIA2664", "fascia 26-64 anni");
            } else {
                saveFascia2664(req, resp);
            }
            return;
        }
        if ("availability".equalsIgnoreCase(requestMode)) {
            loadMammografiaDisponibilita(req, resp);
        } else if ("history".equalsIgnoreCase(requestMode)) {
            loadMammografieStorico(req, resp);
        } else if ("load".equalsIgnoreCase(requestMode)) {
            loadMammografia(req, resp);
        } else {
            saveMammografia(req, resp);
        }
    }

    private void loadMammografia(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            Long idQuestionario = parsePositiveLong(req.getParameter("idQuestionario"));
            boolean readOnly = idQuestionario != null;
            boolean canCompile = readOnly || questionariDao.isMammografiaCompilabile(codPaz);
            Map<String, Object> questionario = readOnly
                    ? questionariDao.findMammografiaById(codPaz, idQuestionario.longValue())
                    : canCompile ? questionariDao.findLatestMammografia(codPaz) : null;
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(canCompile));
            data.put("readOnly", Boolean.valueOf(readOnly));
            data.put("found", Boolean.valueOf(questionario != null));
            data.put("questionario", questionario);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del questionario mammografia.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del questionario mammografia.")));
        }
    }

    private void loadMammografieStorico(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("questionari", questionariDao.findQuestionariByPaziente(codPaz));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dello storico questionari mammografia.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento dello storico questionari mammografia.")));
        }
    }

    private void loadMammografiaDisponibilita(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        if (codPaz == null || codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(codPaz == null
                    ? "Codice paziente non valorizzato."
                    : "Codice consulenza non valorizzato."));
            return;
        }

        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(questionariDao.isQuestionarioApribileAutomaticamente(codPaz, codConsulenza)));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nella verifica di validita' del questionario mammografia.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nella verifica di validita' del questionario mammografia.")));
        }
    }

    private void saveMammografia(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String questionario = trimToNull(req.getParameter("questionario"));
        String utenteInsert = resolveLoggedUser(session);
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }
        if (codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice consulenza non valorizzato."));
            return;
        }
        if (questionario == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Dati del questionario non valorizzati."));
            return;
        }
        if (utenteInsert == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }

        try {
            if (!questionariDao.isMammografiaCompilabile(codPaz)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                JsonUtil.write(resp, ApiResponse.ko("Esiste gia' un questionario mammografia valido per i prossimi 365 giorni."));
                return;
            }
            long idQuestionario = questionariDao.insertMammografia(codPaz, codConsulenza, utenteInsert, questionario);
            HashMap<String, Object> data = new HashMap<>();
            data.put("idQuestionario", Long.valueOf(idQuestionario));
            data.put("codPaz", codPaz);
            data.put("codConsulenza", codConsulenza);
            data.put("utenteInsert", utenteInsert);
            data.put("tipo", "MAMMO");
            JsonUtil.write(resp, ApiResponse.ok(data, "Questionario salvato correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio del questionario mammografia.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio del questionario mammografia.")));
        }
    }

    private void loadFascia65(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            Long idQuestionario = parsePositiveLong(req.getParameter("idQuestionario"));
            boolean readOnly = idQuestionario != null;
            boolean canCompile = readOnly || questionariDao.isFascia65Compilabile(codPaz);
            Map<String, Object> questionario = readOnly
                    ? questionariDao.findFascia65ById(codPaz, idQuestionario.longValue())
                    : canCompile ? questionariDao.findLatestFascia65(codPaz) : null;
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(canCompile));
            data.put("readOnly", Boolean.valueOf(readOnly));
            data.put("canContinue", Boolean.valueOf(!readOnly && canCompile && questionariDao.hasFascia65RifiutatoRecente(codPaz)));
            data.put("found", Boolean.valueOf(questionario != null));
            data.put("questionario", questionario);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del questionario fascia dai 65 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del questionario fascia dai 65 anni.")));
        }
    }

    private void loadFascia65Disponibilita(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        if (codPaz == null || codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(codPaz == null
                    ? "Codice paziente non valorizzato."
                    : "Codice consulenza non valorizzato."));
            return;
        }

        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(questionariDao.isQuestionarioApribileAutomaticamente(codPaz, codConsulenza)));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nella verifica di validita' del questionario fascia dai 65 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nella verifica di validita' del questionario fascia dai 65 anni.")));
        }
    }

    private void saveFascia65(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String questionario = trimToNull(req.getParameter("questionario"));
        String utenteInsert = resolveLoggedUser(session);
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }
        if (codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice consulenza non valorizzato."));
            return;
        }
        if (questionario == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Dati del questionario non valorizzati."));
            return;
        }
        if (utenteInsert == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }

        try {
            if (!questionariDao.isFascia65Compilabile(codPaz)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                JsonUtil.write(resp, ApiResponse.ko("Esiste gia' un questionario fascia dai 65 anni valido per i prossimi 365 giorni."));
                return;
            }
            long idQuestionario = questionariDao.insertFascia65(codPaz, codConsulenza, utenteInsert, questionario);
            HashMap<String, Object> data = new HashMap<>();
            data.put("idQuestionario", Long.valueOf(idQuestionario));
            data.put("codPaz", codPaz);
            data.put("codConsulenza", codConsulenza);
            data.put("utenteInsert", utenteInsert);
            data.put("tipo", "FASCIA65");
            JsonUtil.write(resp, ApiResponse.ok(data, "Questionario salvato correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio del questionario fascia dai 65 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio del questionario fascia dai 65 anni.")));
        }
    }

    private void loadFascia625(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }

        try {
            Long idQuestionario = parsePositiveLong(req.getParameter("idQuestionario"));
            boolean readOnly = idQuestionario != null;
            boolean canCompile = readOnly || questionariDao.isFascia625Compilabile(codPaz);
            Map<String, Object> questionario = readOnly
                    ? questionariDao.findFascia625ById(codPaz, idQuestionario.longValue())
                    : canCompile ? questionariDao.findLatestFascia625(codPaz) : null;
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(canCompile));
            data.put("readOnly", Boolean.valueOf(readOnly));
            data.put("canContinue", Boolean.valueOf(!readOnly && canCompile && questionariDao.hasFascia625RifiutatoRecente(codPaz)));
            data.put("found", Boolean.valueOf(questionario != null));
            data.put("questionario", questionario);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del questionario fascia 6-25 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del questionario fascia 6-25 anni.")));
        }
    }

    private void loadFascia625Disponibilita(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        if (codPaz == null || codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(codPaz == null
                    ? "Codice paziente non valorizzato."
                    : "Codice consulenza non valorizzato."));
            return;
        }

        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(questionariDao.isQuestionarioApribileAutomaticamente(codPaz, codConsulenza)));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nella verifica di validita' del questionario fascia 6-25 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nella verifica di validita' del questionario fascia 6-25 anni.")));
        }
    }

    private void saveFascia625(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String questionario = trimToNull(req.getParameter("questionario"));
        String utenteInsert = resolveLoggedUser(session);
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }
        if (codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice consulenza non valorizzato."));
            return;
        }
        if (questionario == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Dati del questionario non valorizzati."));
            return;
        }
        if (utenteInsert == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente loggato non disponibile."));
            return;
        }

        try {
            if (!questionariDao.isFascia625Compilabile(codPaz)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                JsonUtil.write(resp, ApiResponse.ko("Esiste gia' un questionario fascia 6-25 anni valido per i prossimi 365 giorni."));
                return;
            }
            long idQuestionario = questionariDao.insertFascia625(codPaz, codConsulenza, utenteInsert, questionario);
            HashMap<String, Object> data = new HashMap<>();
            data.put("idQuestionario", Long.valueOf(idQuestionario));
            data.put("codPaz", codPaz);
            data.put("codConsulenza", codConsulenza);
            data.put("utenteInsert", utenteInsert);
            data.put("tipo", "FASCIA625");
            JsonUtil.write(resp, ApiResponse.ok(data, "Questionario salvato correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio del questionario fascia 6-25 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio del questionario fascia 6-25 anni.")));
        }
    }

    private void loadFascia2664(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }
        String codPaz = trimToNull(req.getParameter("codPaz"));
        if (codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice paziente non valorizzato."));
            return;
        }
        try {
            Long idQuestionario = parsePositiveLong(req.getParameter("idQuestionario"));
            boolean readOnly = idQuestionario != null;
            boolean canCompile = readOnly || questionariDao.isFascia2664Compilabile(codPaz);
            Map<String, Object> questionario = readOnly
                    ? questionariDao.findFascia2664ById(codPaz, idQuestionario.longValue())
                    : canCompile ? questionariDao.findLatestFascia2664(codPaz) : null;
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(canCompile));
            data.put("readOnly", Boolean.valueOf(readOnly));
            data.put("canContinue", Boolean.valueOf(!readOnly && canCompile && questionariDao.hasFascia2664RifiutatoRecente(codPaz)));
            data.put("found", Boolean.valueOf(questionario != null));
            data.put("questionario", questionario);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del questionario fascia 26-64 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel caricamento del questionario fascia 26-64 anni.")));
        }
    }

    private void loadFascia2664Disponibilita(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }
        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        if (codPaz == null || codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(codPaz == null
                    ? "Codice paziente non valorizzato."
                    : "Codice consulenza non valorizzato."));
            return;
        }
        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("canCompile", Boolean.valueOf(questionariDao.isQuestionarioApribileAutomaticamente(codPaz, codConsulenza)));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nella verifica di validita' del questionario fascia 26-64 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nella verifica di validita' del questionario fascia 26-64 anni.")));
        }
    }

    private void saveFascia2664(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }
        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String questionario = trimToNull(req.getParameter("questionario"));
        String utenteInsert = resolveLoggedUser(session);
        if (codPaz == null || codConsulenza == null || questionario == null || utenteInsert == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(codPaz == null ? "Codice paziente non valorizzato."
                    : codConsulenza == null ? "Codice consulenza non valorizzato."
                    : questionario == null ? "Dati del questionario non valorizzati."
                    : "Utente loggato non disponibile."));
            return;
        }
        try {
            if (!questionariDao.isFascia2664Compilabile(codPaz)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                JsonUtil.write(resp, ApiResponse.ko("Esiste gia' un questionario fascia 26-64 anni valido per i prossimi 365 giorni."));
                return;
            }
            long idQuestionario = questionariDao.insertFascia2664(codPaz, codConsulenza, utenteInsert, questionario);
            HashMap<String, Object> data = new HashMap<>();
            data.put("idQuestionario", Long.valueOf(idQuestionario));
            data.put("codPaz", codPaz);
            data.put("codConsulenza", codConsulenza);
            data.put("utenteInsert", utenteInsert);
            data.put("tipo", "FASCIA2664");
            JsonUtil.write(resp, ApiResponse.ok(data, "Questionario salvato correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio del questionario fascia 26-64 anni.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex, "Errore nel salvataggio del questionario fascia 26-64 anni.")));
        }
    }

    private void saveFasciaRifiutoMedico(HttpServletRequest req, HttpServletResponse resp,
            String tipo, String descrizioneFascia) throws IOException {
        setNoCache(resp);
        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codPaz = trimToNull(req.getParameter("codPaz"));
        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        String utenteInsert = resolveLoggedUser(session);
        if (codPaz == null || codConsulenza == null || utenteInsert == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(codPaz == null
                    ? "Codice paziente non valorizzato."
                    : codConsulenza == null ? "Codice consulenza non valorizzato."
                    : "Utente loggato non disponibile."));
            return;
        }

        try {
            if (!isFasciaCompilabile(codPaz, tipo)) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                JsonUtil.write(resp, ApiResponse.ko("Esiste gia' un questionario "
                        + descrizioneFascia + " valido per i prossimi 365 giorni."));
                return;
            }
            long idQuestionario = questionariDao.insertFasciaRifiutoMedico(codPaz, codConsulenza, utenteInsert, tipo);
            HashMap<String, Object> data = new HashMap<>();
            data.put("idQuestionario", Long.valueOf(idQuestionario));
            data.put("codPaz", codPaz);
            data.put("codConsulenza", codConsulenza);
            data.put("utenteInsert", utenteInsert);
            data.put("tipo", tipo);
            data.put("rifiuta", "SI_MEDICO");
            JsonUtil.write(resp, ApiResponse.ok(data, "Rinuncia del medico registrata correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio della rinuncia del medico per il questionario "
                    + descrizioneFascia + ".", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex,
                    "Errore nel salvataggio della rinuncia del medico al questionario.")));
        }
    }

    private boolean isFasciaCompilabile(String codPaz, String tipo) throws Exception {
        if ("FASCIA65".equals(tipo)) {
            return questionariDao.isFascia65Compilabile(codPaz);
        }
        if ("FASCIA625".equals(tipo)) {
            return questionariDao.isFascia625Compilabile(codPaz);
        }
        if ("FASCIA2664".equals(tipo)) {
            return questionariDao.isFascia2664Compilabile(codPaz);
        }
        throw new IllegalArgumentException("Tipo di questionario non valido.");
    }

    private String resolveLoggedUser(HttpSession session) {
        Map<String, Object> userData = toObjectMap(session.getAttribute("authUserData"));
        String cognome = firstNonBlank(value(userData, "sn"), value(userData, "surname"), value(userData, "cognome"));
        String nome = firstNonBlank(value(userData, "givenName"), value(userData, "nome"));
        String fullName = join(cognome, nome);
        if (fullName == null) {
            fullName = firstNonBlank(value(userData, "displayName"), value(userData, "cn"), value(userData, "name"));
        }
        return firstNonBlank(fullName, safeString(session.getAttribute("authUser")));
    }

    private Map<String, Object> toObjectMap(Object source) {
        HashMap<String, Object> result = new HashMap<>();
        if (!(source instanceof Map<?, ?>)) {
            return result;
        }
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return result;
    }

    private String value(Map<String, Object> source, String key) {
        if (source.containsKey(key)) {
            return safeString(source.get(key));
        }
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return safeString(entry.getValue());
            }
        }
        return "";
    }

    private String join(String first, String second) {
        String result = ((first == null ? "" : first) + " " + (second == null ? "" : second)).trim();
        return result.isEmpty() ? null : result;
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && trimToNull(safeString(session.getAttribute("authUser"))) != null;
    }

    private boolean isQuestionariMedico(HttpSession session) {
        Map<String, Object> serviceData = toObjectMap(session.getAttribute("authServiceData"));
        return "MED".equalsIgnoreCase(value(serviceData, "codGruppo"))
                && "MED".equalsIgnoreCase(value(serviceData, "codQualifica"));
    }

    private boolean isQuestionariReadRequest(String requestMode) {
        return "availability".equalsIgnoreCase(requestMode)
                || "history".equalsIgnoreCase(requestMode)
                || "load".equalsIgnoreCase(requestMode);
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
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parsePositiveLong(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            long parsed = Long.parseLong(normalized);
            return parsed > 0L ? Long.valueOf(parsed) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String candidate = trimToNull(value);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private String safeString(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) value) {
                String candidate = safeString(item);
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
            return "";
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index += 1) {
                String candidate = safeString(Array.get(value, index));
                if (!candidate.isEmpty()) {
                    return candidate;
                }
            }
            return "";
        }
        return String.valueOf(value).trim();
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
