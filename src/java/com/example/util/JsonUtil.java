package com.example.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

public final class JsonUtil {

    private static final Gson GSON = new Gson();

    private JsonUtil() {
    }

    public static void write(HttpServletResponse resp, Object payload) throws IOException {
        String json = GSON.toJson(payload);
        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
        }
    }
}
