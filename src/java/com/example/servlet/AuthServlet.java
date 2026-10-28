package com.example.servlet;

import com.example.dao.UserServiceDao;
import com.example.dto.ApiResponse;
import com.example.filter.CsrfProtectionFilter;
import com.example.service.DataService;
import com.example.service.LdapService;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String USER_ADMINISTRATOR = "m.manzotti";
    private static final Set<String> USER_ROLE_CODES = new HashSet<>(Arrays.asList("MED", "INF", "AMM"));
    private static final Set<String> LOGGABLE_ACTIONS = new HashSet<>(Arrays.asList(
            "CERTIFICATO MALATTIA",
            "REPOSITORY REFERTI",
            "CRUSCOTTO CONSENSO",
            "STAMPA ALLEGATO M"));

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
        if ("/csrf".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadCsrfToken(req, resp);
            return;
        }
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
        if ("/user-enablement-data".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadUserEnablementData(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione GET non valida");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/services".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadServices(req, resp);
            return;
        }
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
        if ("/action-log".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            logAction(req, resp);
            return;
        }
        if ("/enable-user".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            enableUser(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione POST non valida");
    }

    private void loadCsrfToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(true);
        HashMap<String, Object> data = new HashMap<>();
        data.put("csrfToken", CsrfProtectionFilter.getOrCreateToken(session));
        JsonUtil.write(resp, ApiResponse.ok(data));
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
            String clientIp = getClientIp(req);
            data.put("services", toLoginServices(services));
            data.put("preferredServiceCode", findServiceByClientIp(services, clientIp));
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

    private void loadUserEnablementData(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String username = safeString(session.getAttribute("authUser"));
        if (!isUserAdministrator(username)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.write(resp, ApiResponse.ko("Operazione riservata all'amministratore utenti."));
            return;
        }

        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("services", userServiceDao.getApoServices());
            data.put("roleCodes", new ArrayList<>(USER_ROLE_CODES));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dei dati per l'abilitazione utenti APO.", ex);
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
            if (selectedService == null) 
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonUtil.write(resp, ApiResponse.ko("Servizio non valido per l'utente indicato."));
                return;
            }
            selectedService.putAll(userServiceDao.getUserDataByUsername(username, serviceCode));

            createAuthenticatedSession(req, username, userData, selectedService);
            registerApoLoginAccess(username, userData, selectedService);

            HashMap<String, Object> data = new HashMap<>();
            data.put("redirectUrl", getLandingUrl(req));
            data.put("serviceData", selectedService);
            data.put("csrfToken", getCsrfToken(req.getSession(false)));
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
            registerApoLoginAccess(username, userData, selectedService);

            HashMap<String, Object> data = new HashMap<>();
            data.put("redirectUrl", getLandingUrl(req));
            data.put("serviceData", selectedService);
            data.put("csrfToken", getCsrfToken(req.getSession(false)));
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel cambio servizio APO per l'utente " + username, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel cambio servizio APO."));
        }
    }

    private void logAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String action = trimToNull(req.getParameter("action"));
        if (action == null || !LOGGABLE_ACTIONS.contains(action.toUpperCase())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Azione non valida per il log."));
            return;
        }

        String username = safeString(session.getAttribute("authUser"));
        Map<String, String> serviceData = toStringMap(session.getAttribute("authServiceData"));
        String serviceCode = safeString(serviceData.get("codServizio"));
        if (username.isEmpty() || serviceCode.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko("Utente o servizio di sessione non disponibile."));
            return;
        }

        try {
            userServiceDao.insertApoActionLog(
                    username,
                    serviceCode,
                    safeString(req.getParameter("codConsulenza")),
                    action.toUpperCase(),
                    safeString(req.getParameter("parameters")));
            JsonUtil.write(resp, ApiResponse.ok(null));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel salvataggio del log azione APO.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel salvataggio del log azione."));
        }
    }

    private void enableUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        String adminUsername = safeString(session.getAttribute("authUser"));
        if (!isUserAdministrator(adminUsername)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonUtil.write(resp, ApiResponse.ko("Operazione riservata all'amministratore utenti."));
            return;
        }

        String matricola = safeString(req.getParameter("matricola"));
        String cognome = safeString(req.getParameter("cognome"));
        String nome = safeString(req.getParameter("nome"));
        String codiceFiscale = safeString(req.getParameter("codiceFiscale")).toUpperCase();
        String codGruppo = safeString(req.getParameter("codGruppo")).toUpperCase();
        String codQualifica = safeString(req.getParameter("codQualifica")).toUpperCase();
        String pincode = safeString(req.getParameter("pincode"));
        String codSpec = safeString(req.getParameter("codSpec")).toUpperCase();
        String[] requestedServices = req.getParameterValues("serviceCode");

        String validationError = validateUserEnablementRequest(
                matricola, cognome, nome, codiceFiscale, codGruppo, codQualifica,
                pincode, codSpec, requestedServices);
        if (validationError != null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonUtil.write(resp, ApiResponse.ko(validationError));
            return;
        }

        try {
            List<Map<String, String>> availableServices = userServiceDao.getApoServices();
            Map<String, String> serviceCodes = new HashMap<>();
            for (Map<String, String> service : availableServices) {
                String serviceCode = safeString(service == null ? null : service.get("codServizio"));
                if (!serviceCode.isEmpty()) {
                    serviceCodes.put(serviceCode.toUpperCase(), serviceCode);
                }
            }

            List<String> selectedServices = new ArrayList<>();
            Set<String> selectedServiceKeys = new HashSet<>();
            for (String requestedService : requestedServices) {
                String serviceKey = safeString(requestedService).toUpperCase();
                String serviceCode = serviceCodes.get(serviceKey);
                if (serviceCode == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonUtil.write(resp, ApiResponse.ko("Uno dei servizi selezionati non è un servizio APO attivo."));
                    return;
                }
                if (selectedServiceKeys.add(serviceKey)) {
                    selectedServices.add(serviceCode);
                }
            }

            int enabledServices = userServiceDao.enableApoUser(
                    matricola, cognome, nome, codiceFiscale, codGruppo, codQualifica,
                    pincode, codSpec, selectedServices);
            HashMap<String, Object> data = new HashMap<>();
            data.put("matricola", matricola);
            data.put("enabledServices", enabledServices);
            JsonUtil.write(resp, ApiResponse.ok(data,
                    "Utente abilitato a " + enabledServices + (enabledServices == 1 ? " servizio APO." : " servizi APO.")));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nell'abilitazione dell'utente APO " + matricola
                    + " da parte di " + adminUsername, ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel salvataggio dell'abilitazione utente."));
        }
    }

    private String validateUserEnablementRequest(String matricola, String cognome, String nome,
            String codiceFiscale, String codGruppo, String codQualifica, String pincode,
            String codSpec, String[] requestedServices) {
        if (!matricola.matches("[A-Za-z0-9._-]{1,20}")) {
            return "La matricola è obbligatoria e può contenere al massimo 20 caratteri alfanumerici, punto, trattino o underscore.";
        }
        if (!isValidPlainText(cognome, 50)) {
            return "Il cognome è obbligatorio e può contenere al massimo 50 caratteri.";
        }
        if (!isValidPlainText(nome, 50)) {
            return "Il nome è obbligatorio e può contenere al massimo 50 caratteri.";
        }
        if (!codiceFiscale.matches("[A-Z0-9]{16}")) {
            return "Il codice fiscale deve contenere 16 caratteri alfanumerici.";
        }
        if (!USER_ROLE_CODES.contains(codGruppo)) {
            return "COD_GRUPPO deve essere MED, INF oppure AMM.";
        }
        if (!USER_ROLE_CODES.contains(codQualifica)) {
            return "COD_QUALIFICA deve essere MED, INF oppure AMM.";
        }
        if (requestedServices == null || requestedServices.length == 0) {
            return "Selezionare almeno un servizio APO.";
        }
        if (!pincode.isEmpty() && !isValidPlainText(pincode, 32)) {
            return "Il PINCODE può contenere al massimo 32 caratteri.";
        }
        if (!codSpec.isEmpty() && !isValidPlainText(codSpec, 10)) {
            return "Il COD_SPEC può contenere al massimo 10 caratteri.";
        }
        return null;
    }

    private boolean isValidPlainText(String value, int maximumLength) {
        if (value == null || value.isEmpty() || value.length() > maximumLength) {
            return false;
        }
        for (int index = 0; index < value.length(); index += 1) {
            if (Character.isISOControl(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private boolean isUserAdministrator(String username) {
        return USER_ADMINISTRATOR.equalsIgnoreCase(safeString(username));
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
        data.put("csrfToken", getCsrfToken(session));
        data.put("canManageUsers", isUserAdministrator(username));
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

    private List<Map<String, String>> toLoginServices(List<Map<String, String>> services) {
        List<Map<String, String>> result = new ArrayList<>();
        if (services == null) {
            return result;
        }

        for (Map<String, String> service : services) {
            HashMap<String, String> loginService = new HashMap<>();
            loginService.put("codServizio", safeString(service == null ? null : service.get("codServizio")));
            loginService.put("descrizione", safeString(service == null ? null : service.get("descrizione")));
            result.add(loginService);
        }

        return result;
    }

    private String getClientIp(HttpServletRequest req) {
        String forwardedIp = getForwardedClientIp(req);
        if (!forwardedIp.isEmpty()) {
            return forwardedIp;
        }

        String remoteIp = safeString(req.getRemoteAddr());
        byte[] remoteAddress = parseIpAddress(remoteIp);
        if (remoteAddress == null || !isLoopbackAddress(remoteAddress)) {
            return remoteIp;
        }

        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (localAddress != null && !localAddress.isLoopbackAddress()) {
                return localAddress.getHostAddress();
            }
        } catch (UnknownHostException ex) {
            LOGGER.log(Level.FINE, "Impossibile rilevare l'indirizzo IP locale del server.", ex);
        }

        return remoteIp;
    }

    private String getForwardedClientIp(HttpServletRequest req) {
        String forwardedFor = trimToNull(req.getHeader("X-Forwarded-For"));
        if (forwardedFor == null) {
            return "";
        }

        String[] addresses = forwardedFor.split(",");
        for (String address : addresses) {
            String candidate = trimToNull(address);
            if (parseIpAddress(candidate) != null) {
                return candidate;
            }
        }

        return "";
    }

    private boolean isLoopbackAddress(byte[] address) {
        try {
            return InetAddress.getByAddress(address).isLoopbackAddress();
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    private String findServiceByClientIp(List<Map<String, String>> services, String clientIp) {
        byte[] clientAddress = parseIpAddress(clientIp);
        if (clientAddress == null || services == null) {
            return "";
        }

        for (Map<String, String> service : services) {
            byte[] minimumAddress = parseIpRangeBoundary(service == null ? null : service.get("ipMinimo"), false);
            byte[] maximumAddress = parseIpRangeBoundary(service == null ? null : service.get("ipMassimo"), true);
            if (!isIpInRange(clientAddress, minimumAddress, maximumAddress)) {
                continue;
            }

            return safeString(service.get("codServizio"));
        }

        return "";
    }

    private byte[] parseIpRangeBoundary(String value, boolean maximumBoundary) {
        String address = trimToNull(value);
        if (address != null && isThreeOctetIpv4Prefix(address)) {
            address += maximumBoundary ? ".255" : ".0";
        }

        return parseIpAddress(address);
    }

    private boolean isThreeOctetIpv4Prefix(String address) {
        String[] octets = address.split("\\.", -1);
        if (octets.length != 3) {
            return false;
        }

        for (String octet : octets) {
            try {
                int value = Integer.parseInt(octet);
                if (value < 0 || value > 255) {
                    return false;
                }
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        return true;
    }

    private byte[] parseIpAddress(String value) {
        String address = trimToNull(value);
        if (address == null || !address.matches("[0-9a-fA-F:.]+")) {
            return null;
        }

        try {
            return InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private boolean isIpInRange(byte[] clientAddress, byte[] minimumAddress, byte[] maximumAddress) {
        return clientAddress != null
                && minimumAddress != null
                && maximumAddress != null
                && clientAddress.length == minimumAddress.length
                && clientAddress.length == maximumAddress.length
                && compareIpAddresses(minimumAddress, maximumAddress) <= 0
                && compareIpAddresses(clientAddress, minimumAddress) >= 0
                && compareIpAddresses(clientAddress, maximumAddress) <= 0;
    }

    private int compareIpAddresses(byte[] first, byte[] second) {
        for (int index = 0; index < first.length; index += 1) {
            int comparison = Integer.compare(first[index] & 0xFF, second[index] & 0xFF);
            if (comparison != 0) {
                return comparison;
            }
        }

        return 0;
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
        CsrfProtectionFilter.getOrCreateToken(session);
    }

    private void registerApoLoginAccess(String username, Map<String, Object> userData, Map<String, String> serviceData) throws Exception {
        String matricola = extractLdapMatricola(username, userData);
        String codServizio = safeString(serviceData == null ? null : serviceData.get("codServizio"));
        if (matricola.isEmpty() || codServizio.isEmpty()) {
            return;
        }

        userServiceDao.insertApoUserAccess(matricola, codServizio);
    }

    private String extractLdapMatricola(String username, Map<String, Object> userData) {
        HashMap<String, Object> source = userData == null ? new HashMap<String, Object>() : new HashMap<>(userData);
        String[] candidateKeys = new String[]{"employeeID", "employeeId", "matricola", "employeeNumber"};
        for (int index = 0; index < candidateKeys.length; index += 1) {
            String directValue = safeString(source.get(candidateKeys[index]));
            if (!directValue.isEmpty()) {
                return directValue;
            }
        }

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String key = entry.getKey().trim();
            for (int index = 0; index < candidateKeys.length; index += 1) {
                if (candidateKeys[index].equalsIgnoreCase(key)) {
                    String value = safeString(entry.getValue());
                    if (!value.isEmpty()) {
                        return value;
                    }
                }
            }
        }

        return safeString(username);
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

    private String getCsrfToken(HttpSession session) {
        return session == null ? "" : CsrfProtectionFilter.getOrCreateToken(session);
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
