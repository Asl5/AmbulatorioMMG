package com.example.filter;

import com.example.dto.ApiResponse;
import com.example.util.JsonUtil;
import com.example.util.RequestValidation;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Bounded, in-memory validation before any application or database work. */
public class RequestValidationFilter implements Filter {

    private static final long MAX_REQUEST_BYTES = 2L * 1024 * 1024;

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (req.getCharacterEncoding() == null) {
            req.setCharacterEncoding("UTF-8");
        }
        if (req.getContentLengthLong() > MAX_REQUEST_BYTES) {
            reject(req, resp, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "La dimensione della richiesta supera il limite consentito.");
            return;
        }

        String error;
        try {
            error = RequestValidation.validate(req.getServletPath(), req.getPathInfo(), req.getParameterMap());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            reject(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Parametri della richiesta non validi.");
            return;
        }
        if (error != null) {
            reject(req, resp, HttpServletResponse.SC_BAD_REQUEST, error);
            return;
        }
        chain.doFilter(request, response);
    }

    private void reject(HttpServletRequest req, HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setHeader("Cache-Control", "no-store");
        if ("/StampaReport".equals(req.getServletPath())) {
            resp.sendError(status, message);
        } else {
            resp.setContentType("application/json;charset=UTF-8");
            if (!"HEAD".equals(req.getMethod())) {
                JsonUtil.write(resp, ApiResponse.ko(message));
            }
        }
    }

    @Override
    public void destroy() {
    }
}
