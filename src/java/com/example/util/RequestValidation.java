package com.example.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Structural limits only: clinical text is never rewritten or truncated. */
public final class RequestValidation {

    private static final int MAX_PARAMETERS = 512;
    private static final int MAX_VALUES = 2048;
    private static final int MAX_TOTAL_CHARACTERS = 1024 * 1024;
    private static final int MAX_TEXT_LENGTH = 64 * 1024;
    private static final Pattern PARAMETER_NAME = Pattern.compile("[A-Za-z0-9_\\[\\].-]{1,100}");
    private static final Pattern NUMERIC_ID = Pattern.compile("[0-9]{1,38}");
    private static final Pattern YEAR = Pattern.compile("[0-9]{4}");
    private static final Pattern MONTH = Pattern.compile("[0-9]{1,2}");
    private static final Set<String> NUMERIC_FIELDS = new HashSet<>(Arrays.asList(
            "codpaz", "codpaziente", "cod_paz", "codconsulenza", "idconsulenza", "cod_consulenza",
            "iddiario", "idpatologia", "idallergia", "idquestionario"));
    private static final Set<String> REPEATED_FIELDS = new HashSet<>(Arrays.asList(
            "servicecode", "struttura", "mese", "idcategoria", "idcategoria[]"));
    private static final Set<String> DATE_OPERATORS = new HashSet<>(Arrays.asList(
            "=", "<", ">", "<=", ">=", "!=", "<>"));
    private static final Set<String> BOOLEAN_VALUES = new HashSet<>(Arrays.asList(
            "true", "false", "1", "0", "yes", "no", "on", "off", "s", "n", "si"));
    private static final Set<String> REQUEST_MODES = new HashSet<>(Arrays.asList(
            "load", "save", "delete", "availability", "history", "log"));
    private static final DateTimeFormatter DIARY_DATE_TIME = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter ACCESS_DATE_TIME = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm[:ss]", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter ITALIAN_DATE_TIME = DateTimeFormatter
            .ofPattern("dd/MM/uuuu HH:mm[:ss]", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    private RequestValidation() {
    }

    public static String validate(String servletPath, String pathInfo, Map<String, String[]> parameters) {
        if (parameters.size() > MAX_PARAMETERS) {
            return "Troppi parametri nella richiesta.";
        }
        int totalCharacters = 0;
        int totalValues = 0;
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String name = entry.getKey();
            if (name == null || !PARAMETER_NAME.matcher(name).matches()) {
                return "Nome parametro non valido.";
            }
            String key = name.toLowerCase(Locale.ROOT);
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }
            totalValues += values.length;
            if (totalValues > MAX_VALUES || (values.length > 1 && !REPEATED_FIELDS.contains(key))) {
                return "Numero di valori del parametro non valido.";
            }
            // Only user enablement accepts multiple services; login and switching are scalar.
            if ("servicecode".equals(key) && values.length > 1
                    && !("/api/auth".equals(servletPath) && "/enable-user".equals(pathInfo))) {
                return "Numero di servizi non valido.";
            }
            for (String value : values) {
                if (value == null) {
                    continue;
                }
                if (value.length() > maxLength(key)) {
                    return "La lunghezza di un parametro supera il limite consentito.";
                }
                totalCharacters += value.length();
                if (totalCharacters > MAX_TOTAL_CHARACTERS || containsInvalidControl(value)) {
                    return "Contenuto dei parametri non valido o eccessivo.";
                }
                String normalized = value.trim();
                if (("username".equals(key) || "matricola".equals(key)
                        || "servicecode".equals(key) || "codservizio".equals(key) || "cod_servizio".equals(key))
                        && (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0 || value.indexOf('\t') >= 0)) {
                    return "Formato di un parametro non valido.";
                }
                if (!normalized.isEmpty() && !validValue(key, normalized)) {
                    return "Formato di un parametro non valido.";
                }
            }
        }
        return null;
    }

    private static int maxLength(String key) {
        switch (key) {
            case "username": case "matricola": return 256;
            case "password": return 1024;
            case "_csrf": return 128;
            case "servicecode": case "codservizio": case "cod_servizio": case "struttura": return 100;
            case "codicefiscale": case "codfiscale": case "cod_fiscale": return 32;
            case "data": case "datanascita": case "datadiario": case "dataora": return 32;
            case "anno": case "mese": return 4;
            case "dataoperator": return 2;
            case "_requestmode": case "codgruppo": case "codqualifica": return 32;
            default: return NUMERIC_FIELDS.contains(key) ? 38 : MAX_TEXT_LENGTH;
        }
    }

    private static boolean validValue(String key, String value) {
        if (NUMERIC_FIELDS.contains(key)) {
            return NUMERIC_ID.matcher(value).matches();
        }
        try {
            switch (key) {
                case "data": case "datanascita":
                    return value.length() == 10 && LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) != null;
                case "datadiario":
                    return LocalDateTime.parse(value, DIARY_DATE_TIME) != null;
                case "dataora":
                    return LocalDateTime.parse(value.replace('T', ' '),
                            value.indexOf('/') >= 0 ? ITALIAN_DATE_TIME : ACCESS_DATE_TIME) != null;
                case "anno":
                    return YEAR.matcher(value).matches() && Integer.parseInt(value) >= 2000;
                case "mese":
                    return MONTH.matcher(value).matches() && Integer.parseInt(value) >= 1 && Integer.parseInt(value) <= 12;
                case "dataoperator":
                    return DATE_OPERATORS.contains(value);
                case "_requestmode":
                    return REQUEST_MODES.contains(value.toLowerCase(Locale.ROOT));
                case "allservices":
                    return BOOLEAN_VALUES.contains(value.toLowerCase(Locale.ROOT));
                case "codgruppo": case "codqualifica":
                    return "MED".equalsIgnoreCase(value) || "INF".equalsIgnoreCase(value) || "AMM".equalsIgnoreCase(value);
                default:
                    return true;
            }
        } catch (DateTimeParseException | NumberFormatException ex) {
            return false;
        }
    }

    private static boolean containsInvalidControl(String value) {
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if ((current < 32 && current != '\n' && current != '\r' && current != '\t') || current == 127) {
                return true;
            }
        }
        return false;
    }
}
