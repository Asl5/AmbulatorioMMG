package com.example.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public class StampaReport extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StampaReport.class.getName());
    private static final String ALLEGATO_M_REPORT = "/Report/AllegatoM2.jasper";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("authUser") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non attiva.");
            return;
        }

        String codConsulenza = trimToNull(req.getParameter("COD_CONSULENZA"));
        String codServizio = trimToNull(req.getParameter("COD_SERVIZIO"));

        if (codConsulenza == null || codServizio == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri COD_CONSULENZA e COD_SERVIZIO obbligatori.");
            return;
        }

        try {
            byte[] pdfBytes = generateReportPdf(codConsulenza, codServizio);
            
            String fileName = "AllegatoM_" + codConsulenza + ".pdf";
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            resp.setContentLength(pdfBytes.length);
            
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "Report Allegato M non trovato: " + ALLEGATO_M_REPORT, e);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore generazione report per consulenza: " + codConsulenza, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Errore nella generazione del report: " + e.getMessage());
        }
    }

    private byte[] generateReportPdf(String codConsulenza, String codServizio) throws Exception {
        try (Connection conn = getConnection();
             InputStream reportStream = getReportStream()) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("COD_CONSULENZA", codConsulenza);
            parameters.put("COD_SERVIZIO", codServizio);

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            
            return JasperExportManager.exportReportToPdf(jasperPrint);
        }
    }

    private InputStream getReportStream() throws FileNotFoundException {
        InputStream reportStream = getServletContext().getResourceAsStream(ALLEGATO_M_REPORT);
        if (reportStream == null) {
            throw new FileNotFoundException("File report non trovato: " + ALLEGATO_M_REPORT);
        }
        return reportStream;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Connection getConnection() throws Exception {
        String dbDriver = getServletContext().getInitParameter("dbDriver");
        String dbUrl = getServletContext().getInitParameter("dbUrl");
        String dbUser = getServletContext().getInitParameter("dbUser");
        String dbPassword = getServletContext().getInitParameter("dbPassword");

        Class.forName(dbDriver);
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
