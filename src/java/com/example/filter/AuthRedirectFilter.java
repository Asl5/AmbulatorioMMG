package com.example.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthRedirectFilter implements Filter {

    private static final String LOGIN_ROUTE = "/Login";
    private static final String LOGIN_FILE = "/LOGIN.html";
    private static final String LANDING_FILE = "/APO.html";

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
        String path = getRequestPath(req);
        boolean authenticated = isAuthenticated(req.getSession(false));

        if (isRootPath(path)) {
            if (authenticated) {
                req.getRequestDispatcher(LANDING_FILE).forward(request, response);
            } else {
                resp.sendRedirect(getLoginUrl(req));
            }
            return;
        }

        if (authenticated && isLoginPath(path)) {
            resp.sendRedirect(getLandingUrl(req));
            return;
        }

        if (!authenticated && isProtectedPath(path)) {
            resp.sendRedirect(getLoginUrl(req));
            return;
        }

        String normalizedPath = String.valueOf(path).trim();
        if (LOGIN_ROUTE.equalsIgnoreCase(normalizedPath)) {
            if (!LOGIN_ROUTE.equals(normalizedPath)) {
                resp.sendRedirect(getLoginUrl(req));
                return;
            }
            req.getRequestDispatcher(LOGIN_FILE).forward(request, response);
            return;
        }

        if (LOGIN_FILE.equalsIgnoreCase(normalizedPath)) {
            resp.sendRedirect(getLoginUrl(req));
            return;
        }

        if (authenticated && LANDING_FILE.equalsIgnoreCase(normalizedPath)) {
            resp.sendRedirect(getLandingUrl(req, req.getQueryString()));
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute("authUser") != null;
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

    private boolean isRootPath(String path) {
        return path == null || path.trim().isEmpty() || "/".equals(path.trim());
    }

    private boolean isLoginPath(String path) {
        String normalizedPath = String.valueOf(path).trim();
        return LOGIN_ROUTE.equalsIgnoreCase(normalizedPath) || LOGIN_FILE.equalsIgnoreCase(normalizedPath);
    }

    private boolean isProtectedPath(String path) {
        return LANDING_FILE.equalsIgnoreCase(String.valueOf(path).trim());
    }

    private String getLoginUrl(HttpServletRequest req) {
        return getContextPath(req) + LOGIN_ROUTE;
    }

    private String getLandingUrl(HttpServletRequest req) {
        return getLandingUrl(req, null);
    }

    private String getLandingUrl(HttpServletRequest req, String queryString) {
        String url = getContextPath(req);
        if (url.isEmpty()) {
            url = "/";
        }
        if (queryString != null && !queryString.trim().isEmpty()) {
            url += "?" + queryString;
        }
        return url;
    }

    private String getContextPath(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        return contextPath == null ? "" : contextPath;
    }
}
