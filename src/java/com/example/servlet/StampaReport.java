package com.example.servlet;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 

public class StampaReport extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StampaReport.class.getName());
    private static final String ALLEGATO_M_REPORT = "/Report/AllegatoM2_703.jasper";
    private static final String PDFA_ICC_PROFILE = "/Report/sRGB_IEC61966-2-1_no_black_scaling.icc";
    private static final String JASPER_703_RUNTIME_PATH = "/WEB-INF/report-jaspersoft7/";
    private static final String[] JASPER_703_RUNTIME_JARS = new String[]{
        "jasperreports-7.0.3.jar",
        "jasperreports-pdf-7.0.3.jar",
        "jasperreports-fonts-7.0.3.jar",
        "com.github.librepdf.openpdf_1.3.32.jar",
        "xmpcore-6.1.11.jar",
        "jackson-annotations-2.18.2.jar",
        "jackson-core-2.18.2.jar",
        "jackson-databind-2.18.2.jar",
        "jackson-dataformat-xml-2.18.2.jar",
        "stax2-api-4.2.2.jar",
        "woodstox-core-7.0.0.jar",
        "commons-beanutils-1.9.4.jar",
        "org.apache.commons.commons-collections4_4.4.0.jar",
        "org.apache.commons.commons-io_2.16.1.jar"
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Usare POST per la generazione del report.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("authUser") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non attiva.");
            return;
        }

        String codConsulenza = trimToNull(req.getParameter("COD_CONSULENZA"));
        String codServizio = trimToNull(req.getParameter("COD_SERVIZIO"));
        Object serviceData = session.getAttribute("authServiceData");
        String sessionServiceCode = null;
        if (serviceData instanceof Map<?, ?>) {
            Object value = ((Map<?, ?>) serviceData).get("codServizio");
            sessionServiceCode = trimToNull(value == null ? null : String.valueOf(value));
        }

        if (codConsulenza == null || codServizio == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri COD_CONSULENZA e COD_SERVIZIO obbligatori.");
            return;
        }
        if (sessionServiceCode == null || !sessionServiceCode.equalsIgnoreCase(codServizio)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Servizio non autorizzato per il report richiesto.");
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
            String message = trimToNull(e.getMessage());
            resp.getWriter().println("Errore nella generazione del report: "
                    + (message == null ? e.getClass().getSimpleName() : message));
        }
    }

    private byte[] generateReportPdf(String codConsulenza, String codServizio) throws Exception {
        try (Connection conn = getConnection();
             InputStream reportStream = getReportStream()) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("COD_CONSULENZA", codConsulenza);
            parameters.put("COD_SERVIZIO", codServizio);

            return generateReportPdfWithJasper703(reportStream, parameters, conn);
        }
    }

    /**
     * AllegatoM2_703 e' serializzato da JasperReports 7.0.3. Il runtime 7 e'
     * volutamente isolato da quello storico dell'applicazione: le sue dipendenze
     * (in particolare Jackson e BeanUtils) non possono interferire con il login.
     */
    private byte[] generateReportPdfWithJasper703(InputStream reportStream,
            Map<String, Object> parameters, Connection conn) throws Exception {
        try (Jasper703ClassLoader runtime = createJasper703ClassLoader()) {
            Thread currentThread = Thread.currentThread();
            ClassLoader previousContextClassLoader = currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(runtime);
            try {
                Class<?> jasperReportClass = runtime.loadClass("net.sf.jasperreports.engine.JasperReport");
                Class<?> jasperPrintClass = runtime.loadClass("net.sf.jasperreports.engine.JasperPrint");
                Class<?> jrLoaderClass = runtime.loadClass("net.sf.jasperreports.engine.util.JRLoader");
                Class<?> fillManagerClass = runtime.loadClass("net.sf.jasperreports.engine.JasperFillManager");

                Object jasperReport = invoke(jrLoaderClass.getMethod("loadObject", InputStream.class), null, reportStream);
                Object jasperPrint = invoke(fillManagerClass.getMethod("fillReport", jasperReportClass, Map.class, Connection.class),
                        null, jasperReport, parameters, conn);

                return exportReportPdfAWithJasper703(runtime, jasperPrint, jasperPrintClass);
            } finally {
                currentThread.setContextClassLoader(previousContextClassLoader);
            }
        }
    }

    private byte[] exportReportPdfAWithJasper703(ClassLoader runtime,
            Object jasperPrint, Class<?> jasperPrintClass) throws Exception {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Class<?> exporterClass = runtime.loadClass("net.sf.jasperreports.pdf.JRPdfExporter");
            Class<?> exporterInputClass = runtime.loadClass("net.sf.jasperreports.export.SimpleExporterInput");
            Class<?> exporterOutputClass = runtime.loadClass("net.sf.jasperreports.export.SimpleOutputStreamExporterOutput");
            Class<?> configurationClass = runtime.loadClass("net.sf.jasperreports.pdf.SimplePdfExporterConfiguration");
            Class<?> pdfaConformanceClass = runtime.loadClass("net.sf.jasperreports.pdf.type.PdfaConformanceEnum");

            Object exporter = newInstance(exporterClass.getConstructor());
            Object exporterInput = newInstance(exporterInputClass.getConstructor(jasperPrintClass), jasperPrint);
            Object exporterOutput = newInstance(exporterOutputClass.getConstructor(OutputStream.class), outputStream);
            Object configuration = newInstance(configurationClass.getConstructor());

            @SuppressWarnings({"rawtypes", "unchecked"})
            Object pdfa1a = Enum.valueOf((Class<? extends Enum>) pdfaConformanceClass.asSubclass(Enum.class), "PDFA_1A");
            invokeCompatible(configuration, "setPdfaConformance", pdfa1a);
            invokeCompatible(configuration, "setIccProfilePath", getPdfaIccProfileLocation());
            invokeCompatible(configuration, "setEmbedIccProfile", Boolean.TRUE);
            invokeCompatible(configuration, "setTagged", Boolean.TRUE);
            invokeCompatible(configuration, "setTagLanguage", "it-IT");
            invokeCompatible(configuration, "setMetadataTitle", "Allegato M");

            invokeCompatible(exporter, "setExporterInput", exporterInput);
            invokeCompatible(exporter, "setExporterOutput", exporterOutput);
            invokeCompatible(exporter, "setConfiguration", configuration);
            invoke(exporterClass.getMethod("exportReport"), exporter);
            return outputStream.toByteArray();
        }
    }

    private Jasper703ClassLoader createJasper703ClassLoader() throws IOException {
        List<URL> runtimeJars = new ArrayList<URL>();
        for (String jarName : JASPER_703_RUNTIME_JARS) {
            URL jarUrl = getServletContext().getResource(JASPER_703_RUNTIME_PATH + jarName);
            if (jarUrl == null) {
                throw new FileNotFoundException("Libreria JasperReports 7.0.3 non trovata: " + jarName);
            }
            runtimeJars.add(jarUrl);
        }
        return new Jasper703ClassLoader(runtimeJars.toArray(new URL[runtimeJars.size()]), StampaReport.class.getClassLoader());
    }

    private Object invokeCompatible(Object target, String methodName, Object argument) throws Exception {
        Method method = findCompatibleMethod(target.getClass(), methodName, argument);
        if (method == null) {
            throw new NoSuchMethodException("Metodo non disponibile nel runtime JasperReports 7.0.3: " + methodName);
        }
        return invoke(method, target, argument);
    }

    private Method findCompatibleMethod(Class<?> targetClass, String methodName, Object argument) {
        for (Method method : targetClass.getMethods()) {
            if (!methodName.equals(method.getName()) || method.getParameterTypes().length != 1) {
                continue;
            }
            if (isCompatibleParameter(method.getParameterTypes()[0], argument)) {
                return method;
            }
        }
        return null;
    }

    private boolean isCompatibleParameter(Class<?> parameterType, Object argument) {
        if (argument == null) {
            return !parameterType.isPrimitive();
        }
        if (!parameterType.isPrimitive()) {
            return parameterType.isAssignableFrom(argument.getClass());
        }
        return (parameterType == Boolean.TYPE && argument instanceof Boolean)
                || (parameterType == Integer.TYPE && argument instanceof Integer)
                || (parameterType == Long.TYPE && argument instanceof Long)
                || (parameterType == Double.TYPE && argument instanceof Double)
                || (parameterType == Float.TYPE && argument instanceof Float);
    }

    private Object newInstance(Constructor<?> constructor, Object... arguments) throws Exception {
        try {
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            rethrowInvocationCause(e);
            return null;
        }
    }

    private Object invoke(Method method, Object target, Object... arguments) throws Exception {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException e) {
            rethrowInvocationCause(e);
            return null;
        }
    }

    private void rethrowInvocationCause(InvocationTargetException exception) throws Exception {
        Throwable cause = exception.getCause();
        if (cause instanceof Exception) {
            throw (Exception) cause;
        }
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        throw new Exception(cause);
    }

    private InputStream getReportStream() throws FileNotFoundException {
        InputStream reportStream = getServletContext().getResourceAsStream(ALLEGATO_M_REPORT);
        if (reportStream == null) {
            throw new FileNotFoundException("File report non trovato: " + ALLEGATO_M_REPORT);
        }
        return reportStream;
    }

    private String getPdfaIccProfileLocation() throws IOException {
        URL profileUrl = getServletContext().getResource(PDFA_ICC_PROFILE);
        if (profileUrl == null) {
            throw new FileNotFoundException("Profilo ICC PDF/A non trovato: " + PDFA_ICC_PROFILE);
        }
        return profileUrl.toExternalForm();
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

    private static final class Jasper703ClassLoader extends URLClassLoader {

        private static final String[] ISOLATED_PACKAGES = new String[]{
            "net.sf.jasperreports.",
            "com.fasterxml.jackson.",
            "com.ctc.wstx.",
            "org.codehaus.stax2.",
            "org.apache.commons.beanutils.",
            "org.apache.commons.collections4.",
            "org.apache.commons.io.",
            "com.lowagie.",
            "com.adobe.xmp."
        };

        private Jasper703ClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected synchronized Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
            Class<?> loadedClass = findLoadedClass(className);
            if (loadedClass == null && isIsolatedPackage(className)) {
                try {
                    loadedClass = findClass(className);
                } catch (ClassNotFoundException ignored) {
                    // La dipendenza puo' essere fornita dal classpath standard dell'applicazione.
                }
            }
            if (loadedClass == null) {
                loadedClass = super.loadClass(className, false);
            }
            if (resolve) {
                resolveClass(loadedClass);
            }
            return loadedClass;
        }

        @Override
        public URL getResource(String resourceName) {
            if (isIsolatedResource(resourceName)) {
                return findResource(resourceName);
            }
            return super.getResource(resourceName);
        }

        @Override
        public Enumeration<URL> getResources(String resourceName) throws IOException {
            if (isIsolatedResource(resourceName)) {
                return findResources(resourceName);
            }
            return super.getResources(resourceName);
        }

        private boolean isIsolatedPackage(String className) {
            for (String packagePrefix : ISOLATED_PACKAGES) {
                if (className.startsWith(packagePrefix)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isIsolatedResource(String resourceName) {
            return "jasperreports_extension.properties".equals(resourceName)
                    || "default.jasperreports.properties".equals(resourceName);
        }
    }
}
