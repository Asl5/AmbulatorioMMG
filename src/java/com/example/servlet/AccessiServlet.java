package com.example.servlet;

import com.example.dao.AccessiDao;
import com.example.dto.ApiResponse;
import com.example.service.DataService;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AccessiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AccessiServlet.class.getName());
    private AccessiDao accessiDao;

    @Override
    public void init() {
        String jndiName = trimToNull(getServletContext().getInitParameter("dbJndi"));
        String dbDriver = trimToNull(getServletContext().getInitParameter("dbDriver"));
        String dbUrl = trimToNull(getServletContext().getInitParameter("dbUrl"));
        String dbUser = trimToNull(getServletContext().getInitParameter("dbUser"));
        String dbPassword = trimToNull(getServletContext().getInitParameter("dbPassword"));

        DataService dataService = new DataService(jndiName, dbDriver, dbUrl, dbUser, dbPassword);
        accessiDao = new AccessiDao(dataService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("".equals(action) || "/".equals(action) || "/search".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadAccessi(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione GET non valida");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/new".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            createAccesso(req, resp);
            return;
        }

        if ("/delete".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            deleteAccesso(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione POST non valida");
    }

    private void loadAccessi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
        String serviceCode = trimToNull(serviceData.get("codServizio"));
        if (isTrue(req.getParameter("allServices"))) {
            serviceCode = null;
        }

        try {
            List<Map<String, String>> accessi = accessiDao.searchAccessi(
                    serviceCode,
                    trimToNull(req.getParameter("data")),
                    trimToNull(req.getParameter("dataOperator")),
                    trimToNull(req.getParameter("consulenza")),
                    trimToNull(req.getParameter("cognome")),
                    trimToNull(req.getParameter("nome")),
                    trimToNull(req.getParameter("sede")),
                    trimToNull(req.getParameter("codiceFiscale")),
                    trimToNull(req.getParameter("medico")));

            HashMap<String, Object> data = new HashMap<>();
            data.put("accessi", accessi);
            data.put("serviceData", serviceData);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (DateTimeParseException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Filtro data non valido."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento degli accessi.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildErrorMessage(ex)));
        }
    }

    private void createAccesso(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
        String serviceCode = trimToNull(serviceData.get("codServizio"));
        if (serviceCode == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Servizio utente non disponibile."));
            return;
        }

        String codiceFiscale = trimToNull(req.getParameter("codFiscale"));
        String pin = trimToNull(req.getParameter("pin"));
        String codPaz = trimToNull(req.getParameter("codPaz"));
        String dataOra = trimToNull(req.getParameter("dataOra"));
        String tipoAccessoValue = trimToNull(req.getParameter("tipoAccessoValue"));
        String codComuneRes = trimToNull(req.getParameter("codComuneRes"));
        if (codiceFiscale == null || dataOra == null || tipoAccessoValue == null || codPaz == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Paziente, identificativo paziente, data/ora o tipo accesso non valorizzati."));
            return;
        }

        HashMap<String, String> payload = new HashMap<>();
        payload.put("codFiscale", safeString(codiceFiscale));
        payload.put("cognome", safeString(req.getParameter("cognome")));
        payload.put("nome", safeString(req.getParameter("nome")));
        payload.put("sesso", safeString(req.getParameter("sesso")));
        payload.put("pin", safeString(pin));
        payload.put("codPaz", safeString(codPaz));
        payload.put("dataOra", safeString(dataOra));
        payload.put("diagnosi", safeString(req.getParameter("diagnosi")));
        payload.put("tipoAccesso", safeString(req.getParameter("tipoAccesso")));
        payload.put("tipoAccessoLabel", safeString(req.getParameter("tipoAccessoLabel")));
        payload.put("tipoAccessoValue", safeString(tipoAccessoValue));
        payload.put("codComuneRes", safeString(codComuneRes));
        payload.put("schedaPaziente", safeString(req.getParameter("schedaPaziente")));
        payload.put("schedaTelefono", safeString(req.getParameter("schedaTelefono")));
        payload.put("schedaStatoCivile", safeString(req.getParameter("schedaStatoCivile")));
        payload.put("schedaFumo", safeString(req.getParameter("schedaFumo")));
        payload.put("schedaIbm", safeString(req.getParameter("schedaIbm")));
        payload.put("schedaIbmKg", safeString(req.getParameter("schedaIbmKg")));
        payload.put("schedaIbmCm", safeString(req.getParameter("schedaIbmCm")));
        payload.put("schedaAllergie", safeString(req.getParameter("schedaAllergie")));
        payload.put("schedaTerapie", safeString(req.getParameter("schedaTerapie")));
        payload.put("schedaCaregiver", safeString(req.getParameter("schedaCaregiver")));
        payload.put("schedaDiario", safeString(req.getParameter("schedaDiario")));
        payload.put("schedaPatologie", safeString(req.getParameter("schedaPatologie")));

        try {
            Map<String, String> created = accessiDao.createAccesso(
                    serviceCode,
                    payload,
                    safeString(session.getAttribute("authUser")),
                    buildMedicoDisplayName(toObjectMap(session.getAttribute("authUserData")),
                            safeString(session.getAttribute("authUser"))),
                    safeString(req.getRemoteAddr()));
            HashMap<String, Object> data = new HashMap<>();
            data.put("accesso", created);
            String successMessage = isTrue(payload.get("schedaPaziente"))
                    ? "Scheda paziente e nuovo accesso inseriti correttamente."
                    : "Nuovo accesso inserito correttamente.";
            JsonUtil.write(resp, ApiResponse.ok(data, successMessage));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nell'inserimento del nuovo accesso.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildCreateErrorMessage(ex)));
        }
    }

    private void deleteAccesso(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String codConsulenza = trimToNull(req.getParameter("codConsulenza"));
        if (codConsulenza == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Codice consulenza non valorizzato."));
            return;
        }

        try {
            int updatedRows = accessiDao.deleteAccesso(codConsulenza);
            if (updatedRows <= 0) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonUtil.write(resp, ApiResponse.ko("Consulenza non trovata."));
                return;
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put("codConsulenza", codConsulenza);
            JsonUtil.write(resp, ApiResponse.ok(data, "Consulenza eliminata correttamente."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nell'eliminazione della consulenza.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko(buildDeleteErrorMessage(ex)));
        }
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("authUser") != null;
    }

    private HashMap<String, Object> toObjectMap(Object value) {
        HashMap<String, Object> result = new HashMap<>();
        if (!(value instanceof Map<?, ?>)) {
            return result;
        }

        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }

        return result;
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

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean isTrue(String value) {
        String normalized = value == null ? "" : value.trim();
        return "true".equalsIgnoreCase(normalized)
                || "1".equals(normalized)
                || "yes".equalsIgnoreCase(normalized)
                || "si".equalsIgnoreCase(normalized);
    }

    private String buildMedicoDisplayName(Map<String, Object> userData, String username) {
        String displayName = safeString(userData.get("displayName"));
        if (!displayName.isEmpty()) {
            return displayName;
        }

        String commonName = safeString(userData.get("cn"));
        if (!commonName.isEmpty()) {
            return commonName;
        }

        String nome = safeString(userData.get("givenName"));
        String cognome = safeString(userData.get("sn"));
        String fullName = (cognome + " " + nome).trim();
        if (!fullName.isEmpty()) {
            return fullName;
        }

        return username;
    }

    private String buildErrorMessage(Exception ex) {
        String message = ex == null ? "" : safeString(ex.getMessage());
        if (message.isEmpty() && ex != null && ex.getCause() != null) {
            message = safeString(ex.getCause().getMessage());
        }
        if (message.isEmpty()) {
            return "Errore nel caricamento degli accessi.";
        }
        return "Errore nel caricamento degli accessi: " + message;
    }

    private String buildCreateErrorMessage(Exception ex) {
        String message = ex == null ? "" : safeString(ex.getMessage());
        if (message.isEmpty() && ex != null && ex.getCause() != null) {
            message = safeString(ex.getCause().getMessage());
        }
        if (message.isEmpty()) {
            return "Errore nell'inserimento del nuovo accesso.";
        }
        return "Errore nell'inserimento del nuovo accesso: " + message;
    }

    private String buildDeleteErrorMessage(Exception ex) {
        String message = ex == null ? "" : safeString(ex.getMessage());
        if (message.isEmpty() && ex != null && ex.getCause() != null) {
            message = safeString(ex.getCause().getMessage());
        }
        if (message.isEmpty()) {
            return "Errore nell'eliminazione della consulenza.";
        }
        return "Errore nell'eliminazione della consulenza: " + message;
    }
}
