package com.example.filter;

import com.example.dto.ApiResponse;
import com.example.util.JsonUtil;
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

/** Authentication boundary for all API and report endpoints, including future ones. */
public class ApiAuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // Use the container's decoded mapping, not a client-controlled URI spelling.
        if (isPublicAuthenticationRequest(req)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("authUser") != null) {
            chain.doFilter(request, response);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setHeader("Cache-Control", "no-store");
        if ("/StampaReport".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione non attiva.");
        } else {
            resp.setContentType("application/json;charset=UTF-8");
            if (!"HEAD".equals(req.getMethod())) {
                JsonUtil.write(resp, ApiResponse.ko("Sessione non attiva."));
            }
        }
    }

    private boolean isPublicAuthenticationRequest(HttpServletRequest req) {
        if (!"/api/auth".equals(req.getServletPath())) {
            return false;
        }
        String action = req.getPathInfo();
        String method = req.getMethod();
        return ("GET".equals(method) && "/csrf".equals(action))
                || ("POST".equals(method) && "/login".equals(action))
                || (("GET".equals(method) || "POST".equals(method)) && "/services".equals(action));
    }

    @Override
    public void destroy() {
    }
}
