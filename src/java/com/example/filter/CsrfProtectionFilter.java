package com.example.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CsrfProtectionFilter implements Filter {

    public static final String CSRF_SESSION_ATTRIBUTE = "csrfToken";
    private static final String REQUESTED_WITH_HEADER = "X-Requested-With";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!requiresProtection(req)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("authUser") == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non attiva.");
            return;
        }

        String expectedToken = getOrCreateToken(session);
        String providedToken = trimToNull(req.getHeader(CSRF_HEADER));
        if (providedToken == null) {
            providedToken = trimToNull(req.getParameter("_csrf"));
        }
        if (!expectedToken.equals(providedToken)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF non valido.");
            return;
        }

        String requestedWith = trimToNull(req.getHeader(REQUESTED_WITH_HEADER));
        if (!XML_HTTP_REQUEST.equalsIgnoreCase(requestedWith) && !isReportRequest(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Richiesta non consentita.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return "";
        }

        Object current = session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        if (current instanceof String && !((String) current).trim().isEmpty()) {
            return ((String) current).trim();
        }

        String token = UUID.randomUUID().toString();
        session.setAttribute(CSRF_SESSION_ATTRIBUTE, token);
        return token;
    }

    private boolean requiresProtection(HttpServletRequest req) {
        String method = safe(req.getMethod()).toUpperCase();
        if (!"POST".equals(method) && !"PUT".equals(method) && !"PATCH".equals(method) && !"DELETE".equals(method)) {
            return false;
        }

        String path = getRequestPath(req);
        if ("/api/auth/login".equals(path) || "/api/auth/services".equals(path)) {
            return false;
        }

        return path.startsWith("/api/") || "/StampaReport".equals(path);
    }

    private boolean isReportRequest(HttpServletRequest req) {
        return "/StampaReport".equals(getRequestPath(req));
    }

    private String getRequestPath(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (uri == null) {
            return "";
        }
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private String trimToNull(String value) {
        String trimmed = safe(value).trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
