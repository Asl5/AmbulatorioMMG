package com.example.servlet;

import com.example.dao.UserServiceDao;
import com.example.dto.ApiResponse;
import com.example.service.DataService;
import com.example.service.LdapService;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuthServlet.class.getName());
    private static final int SESSION_TIMEOUT_SECONDS = 60 * 60;
    private static final String LOGIN_PATH = "/Login";

    private final LdapService ldapService = new LdapService();
    private UserServiceDao userServiceDao;

    @Override
    public void init() throws ServletException {
        String jndiName = trimToNull(getServletContext().getInitParameter("dbJndi"));
        String dbDriver = trimToNull(getServletContext().getInitParameter("dbDriver"));
        String dbUrl = trimToNull(getServletContext().getInitParameter("dbUrl"));
        String dbUser = trimToNull(getServletContext().getInitParameter("dbUser"));
        String dbPassword = trimToNull(getServletContext().getInitParameter("dbPassword"));

        DataService dataService = new DataService(jndiName, dbDriver, dbUrl, dbUser, dbPassword);
        userServiceDao = new UserServiceDao(dataService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/services".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadServices(req, resp);
            return;
        }
        if ("/apo-services".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadApoServices(req, resp);
            return;
        }
        if ("/session".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadSessionData(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione GET non valida");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/login".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            login(req, resp);
            return;
        }
        if ("/logout".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            logout(req, resp);
            return;
        }
        if ("/switch-service".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            switchService(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione POST non valida");
    }

    private void loadServices(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        String username = trimToNull(req.getParameter("username"));
        HashMap<String, Object> data = new HashMap<>();
        data.put("username", username == null ? "" : username);

        if (username == null) {
            data.put("services", new java.util.ArrayList<Map<String, String>>());
            JsonUtil.write(resp, ApiResponse.ok(data));
            return;
        }

        try {
            List<Map<String, String>> services = userServiceDao.getServiziByUser(username);
            data.put("services", services);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento servizi per l'utente " + username, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel caricamento dei servizi."));
        }
    }

    private void loadApoServices(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        try {
            String username = safeString(session.getAttribute("authUser"));
            HashMap<String, Object> data = new HashMap<>();
            data.put("services", userServiceDao.getApoServicesByUser(username));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dei servizi APO.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel caricamento dei servizi APO."));
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        String username = trimToNull(req.getParameter("username"));
        String password = req.getParameter("password");
        String serviceCode = trimToNull(req.getParameter("serviceCode"));

        if (username == null || password == null || password.trim().isEmpty() || serviceCode == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Credenziali o servizio mancanti."));
            return;
        }

        try {
            LdapService.LdapConfig ldapConfig = getLdapConfig();
            HashMap<String, Object> userData = ldapService.authenticateAndFetchUserData(username, password, ldapConfig);
            Map<String, String> selectedService = userServiceDao.getServiceByUser(username, serviceCode);
            if (selectedService == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.write(resp, ApiResponse.ko("Servizio non valido per l'utente indicato."));
                return;
            }
            selectedService.putAll(userServiceDao.getUserDataByUsername(username, serviceCode));

            createAuthenticatedSession(req, username, userData, selectedService);

            HashMap<String, Object> data = new HashMap<>();
            data.put("redirectUrl", getLandingUrl(req));
            data.put("serviceData", selectedService);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (javax.naming.NamingException ex) {
            LOGGER.log(Level.WARNING, "Credenziali non valide per l'utente " + username, ex);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Credenziali non valide."));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore di autenticazione per l'utente " + username, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore di autenticazione."));
        }
    }

    private void switchService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String username = safeString(session.getAttribute("authUser"));
        HashMap<String, Object> userData = toObjectMap(session.getAttribute("authUserData"));
        String serviceCode = trimToNull(req.getParameter("serviceCode"));
        if (serviceCode == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Servizio non valorizzato."));
            return;
        }

        try {
            Map<String, String> selectedService = userServiceDao.getApoServiceByUser(username, serviceCode);
            if (selectedService == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.write(resp, ApiResponse.ko("Servizio APO non valido per l'utente indicato."));
                return;
            }
            selectedService.putAll(userServiceDao.getUserDataByUsername(username, serviceCode));

            createAuthenticatedSession(req, username, userData, selectedService);

            HashMap<String, Object> data = new HashMap<>();
            data.put("redirectUrl", getLandingUrl(req));
            data.put("serviceData", selectedService);
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel cambio servizio APO per l'utente " + username, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel cambio servizio APO."));
        }
    }

    private void loadSessionData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        HashMap<String, Object> data = new HashMap<>();
        String username = safeString(session.getAttribute("authUser"));
        data.put("username", username);
        data.put("userData", toObjectMap(session.getAttribute("authUserData")));
        data.put("serviceData", toStringMap(session.getAttribute("authServiceData")));
        data.put("logoutUrl", getLoginUrl(req));
        try {
            data.put("apoServices", userServiceDao.getApoServicesByUser(username));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dei servizi APO per la sessione.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel caricamento dei servizi APO."));
        }
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        HashMap<String, Object> data = new HashMap<>();
        data.put("redirectUrl", getLoginUrl(req));
        JsonUtil.write(resp, ApiResponse.ok(data));
    }

    private void createAuthenticatedSession(HttpServletRequest req, String username,
            Map<String, Object> userData, Map<String, String> serviceData) {
        HttpSession previousSession = req.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute("authUser", username);
        session.setAttribute("authUserData", userData == null ? new HashMap<String, Object>() : new HashMap<>(userData));
        session.setAttribute("authServiceData", serviceData == null ? new HashMap<String, String>() : new HashMap<>(serviceData));
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

    private LdapService.LdapConfig getLdapConfig() throws ServletException {
        ServletConfig config = getServletConfig();
        String ldapUrl = trimToNull(config.getInitParameter("ldapUrl"));
        String ldapUserDnPattern = trimToNull(config.getInitParameter("ldapUserDnPattern"));
        String ldapBaseDn = trimToNull(config.getInitParameter("ldapBaseDn"));
        String ldapSearchFilter = trimToNull(config.getInitParameter("ldapSearchFilter"));
        String ldapAttributes = trimToNull(config.getInitParameter("ldapAttributes"));

        if (ldapUrl == null || ldapUserDnPattern == null || ldapBaseDn == null || ldapSearchFilter == null) {
            throw new ServletException("Parametri LDAP mancanti.");
        }

        return new LdapService.LdapConfig(
                ldapUrl,
                ldapUserDnPattern,
                ldapBaseDn,
                ldapSearchFilter,
                ldapAttributes);
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

    private String getLoginUrl(HttpServletRequest req) {
        return getContextPath(req) + LOGIN_PATH;
    }

    private String getLandingUrl(HttpServletRequest req) {
        String contextPath = getContextPath(req);
        return contextPath.isEmpty() ? "/" : contextPath;
    }

    private String getContextPath(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        return contextPath == null ? "" : contextPath;
    }
}
