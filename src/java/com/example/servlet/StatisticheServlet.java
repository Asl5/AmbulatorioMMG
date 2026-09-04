package com.example.servlet;

import com.example.dao.UserServiceDao;
import com.example.dao.StatisticheDao;
import com.example.dao.StatisticheDao.ReportExportData;
import com.example.dto.ApiResponse;
import com.example.service.DataService;
import com.example.util.ExcelXmlUtil;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class StatisticheServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(StatisticheServlet.class.getName());
    private StatisticheDao statisticheDao;
    private UserServiceDao userServiceDao;

    @Override
    public void init() {
        String jndiName = trimToNull(getServletContext().getInitParameter("dbJndi"));
        String dbDriver = trimToNull(getServletContext().getInitParameter("dbDriver"));
        String dbUrl = trimToNull(getServletContext().getInitParameter("dbUrl"));
        String dbUser = trimToNull(getServletContext().getInitParameter("dbUser"));
        String dbPassword = trimToNull(getServletContext().getInitParameter("dbPassword"));

        DataService dataService = new DataService(jndiName, dbDriver, dbUrl, dbUser, dbPassword);
        statisticheDao = new StatisticheDao(dataService);
        userServiceDao = new UserServiceDao(dataService);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("".equals(action) || "/".equals(action) || "/reports".equals(action)) {
            resp.setContentType("application/json;charset=UTF-8");
            loadReports(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione GET non valida");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = normalizePath(req.getPathInfo());
        if ("/export".equals(action)) {
            exportReport(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Azione POST non valida");
    }

    private void loadReports(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            return;
        }

        try {
            List<java.util.Map<String, String>> reports = statisticheDao.loadActiveReports();
            String username = safeString(session.getAttribute("authUser"));
            HashMap<String, Object> data = new HashMap<>();
            data.put("reports", reports);
            data.put("structures", userServiceDao.getAllActiveServicesByUser(username));
            data.put("currentYear", Year.now().getValue());
            JsonUtil.write(resp, ApiResponse.ok(data));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento dei report statistici.", ex);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonUtil.write(resp, ApiResponse.ko("Errore nel caricamento dei report statistici."));
        }
    }

    private void exportReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setNoCache(resp);

        HttpSession session = req.getSession(false);
        if (!isAuthenticated(session)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non attiva.");
            return;
        }

        String reportId = trimToNull(req.getParameter("reportId"));
        if (reportId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Report non valorizzato.");
            return;
        }

        Integer anno = parseInteger(req.getParameter("anno"));
        List<Integer> mesi = parseIntegerList(req.getParameterValues("mese"));
        List<String> strutture = parseStringList(req.getParameterValues("struttura"));

        if (anno != null && (anno < 2000 || anno > Year.now().getValue())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Anno non valido.");
            return;
        }
        for (int index = 0; index < mesi.size(); index += 1) {
            int mese = mesi.get(index).intValue();
            if (mese < 1 || mese > 12) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mese non valido.");
                return;
            }
        }

        try {
            String username = safeString(session.getAttribute("authUser"));
            List<String> allowedStructures = resolveAllowedStructures(username, strutture);
            ReportExportData exportData = statisticheDao.exportReport(reportId, anno, mesi, allowedStructures);
            String filename = buildFilename(exportData);

            resp.setContentType("application/vnd.ms-excel;charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"; filename*=UTF-8''"
                    + URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replace("+", "%20"));

            ExcelXmlUtil.writeWorkbook(resp.getOutputStream(), exportData.getTitolo(),
                    exportData.getColumns(), exportData.getRows());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore nell'export del report statistico " + reportId, ex);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, buildExportErrorMessage(ex));
        }
    }

    private String buildFilename(ReportExportData exportData) {
        String title = exportData == null ? "report" : safeString(exportData.getTitolo());
        String normalized = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (normalized.isEmpty()) {
            normalized = "report_" + safeString(exportData == null ? "" : exportData.getReportId());
        }
        return normalized + ".xls";
    }

    private String buildExportErrorMessage(Exception ex) {
        String message = ex == null ? "" : safeString(ex.getMessage());
        if (!message.isEmpty()) {
            return message;
        }
        return "Errore nella generazione del report Excel.";
    }

    private List<String> resolveAllowedStructures(String username, List<String> requestedStructures) throws Exception {
        List<Map<String, String>> apoServices = userServiceDao.getAllActiveServicesByUser(username);
        ArrayList<String> allowedStructures = new ArrayList<>();
        HashSet<String> allowedCodes = new HashSet<>();

        for (int index = 0; index < apoServices.size(); index += 1) {
            Map<String, String> service = apoServices.get(index);
            String code = safeString(service.get("codServizio")).toUpperCase(Locale.ROOT);
            if (code.isEmpty() || allowedCodes.contains(code)) {
                continue;
            }
            allowedCodes.add(code);
            allowedStructures.add(code);
        }

        if (allowedStructures.isEmpty()) {
            throw new IllegalStateException("Nessuna struttura APO abilitata per l'utente.");
        }

        if (requestedStructures == null || requestedStructures.isEmpty() || requestedStructures.contains("TUTTE")) {
            return allowedStructures;
        }

        ArrayList<String> selectedStructures = new ArrayList<>();
        for (int index = 0; index < requestedStructures.size(); index += 1) {
            String code = safeString(requestedStructures.get(index)).toUpperCase(Locale.ROOT);
            if (!allowedCodes.contains(code) || selectedStructures.contains(code)) {
                continue;
            }
            selectedStructures.add(code);
        }

        if (selectedStructures.isEmpty()) {
            throw new IllegalStateException("Le strutture selezionate non sono abilitate per l'utente.");
        }

        return selectedStructures;
    }

    private Integer parseInteger(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return null;
        }
        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private List<Integer> parseIntegerList(String[] values) {
        ArrayList<Integer> parsedValues = new ArrayList<>();
        if (values == null) {
            return parsedValues;
        }

        for (int index = 0; index < values.length; index += 1) {
            Integer parsed = parseInteger(values[index]);
            if (parsed != null && !parsedValues.contains(parsed)) {
                parsedValues.add(parsed);
            }
        }

        return parsedValues;
    }

    private List<String> parseStringList(String[] values) {
        ArrayList<String> parsedValues = new ArrayList<>();
        if (values == null) {
            return parsedValues;
        }

        for (int index = 0; index < values.length; index += 1) {
            String normalized = trimToNull(values[index]);
            if (normalized == null) {
                continue;
            }
            String upper = normalized.toUpperCase(Locale.ROOT);
            if (!parsedValues.contains(upper)) {
                parsedValues.add(upper);
            }
        }

        return parsedValues;
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
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
