package com.example.util;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/** Keeps diagnostics in server logs and exposes only an opaque reference. */
public final class ErrorUtil {

    public static final String ERROR_ID_HEADER = "X-Error-ID";

    private ErrorUtil() {
    }

    public static void log(HttpServletResponse response, Logger logger, Level level,
            String context, Throwable error) {
        String reference = UUID.randomUUID().toString();
        response.setHeader(ERROR_ID_HEADER, reference);
        logger.log(level, "[" + reference + "] " + context, error);
    }

    public static String message(HttpServletResponse response, String publicMessage) {
        String reference = response.getHeader(ERROR_ID_HEADER);
        return reference == null ? publicMessage : publicMessage + " Riferimento: " + reference;
    }
}
