package com.example.servlet;

import com.example.dto.ApiResponse;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Renders application error responses without exposing container or exception details.
 */
public class ErrorPageServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int statusCode = getStatusCode(req);
        ErrorContent content = getErrorContent(statusCode);

        resp.setStatus(statusCode);
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");

        if (isApiRequest(req)) {
            resp.setContentType("application/json;charset=UTF-8");
            JsonUtil.write(resp, ApiResponse.ko(content.message));
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        if ("HEAD".equalsIgnoreCase(req.getMethod())) {
            return;
        }

        String contextPath = req.getContextPath() == null ? "" : req.getContextPath();
        String destination = contextPath.isEmpty() ? "/" : contextPath + "/";
        String destinationLabel = "Torna all'applicazione";
        if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
            destination = contextPath + "/Login";
            destinationLabel = "Torna alla pagina di accesso";
        }

        PrintWriter writer = resp.getWriter();
        writer.print("<!doctype html>");
        writer.print("<html lang=\"it\"><head>");
        writer.print("<meta charset=\"UTF-8\">");
        writer.print("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        writer.print("<meta name=\"robots\" content=\"noindex,nofollow\">");
        writer.print("<title>Errore " + statusCode + " - APO</title>");
        writer.print("<link rel=\"stylesheet\" href=\""
                + escapeHtml(contextPath + "/CSS/ERROR.css") + "\">");
        writer.print("</head><body>");
        writer.print("<main class=\"error-card\" role=\"main\">");
        writer.print("<p class=\"error-code\">Errore " + statusCode + "</p>");
        writer.print("<h1>" + escapeHtml(content.title) + "</h1>");
        writer.print("<p class=\"error-message\">" + escapeHtml(content.message) + "</p>");
        writer.print("<a class=\"error-action\" href=\"" + escapeHtml(destination) + "\">"
                + escapeHtml(destinationLabel) + "</a>");
        writer.print("</main></body></html>");
    }

    private int getStatusCode(HttpServletRequest req) {
        Object status = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Number) {
            int value = ((Number) status).intValue();
            return value >= 400 && value <= 599
                    ? value : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private boolean isApiRequest(HttpServletRequest req) {
        Object errorUriValue = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String errorUri = errorUriValue == null ? req.getRequestURI() : String.valueOf(errorUriValue);
        String contextPath = req.getContextPath() == null ? "" : req.getContextPath();
        String apiPrefix = contextPath + "/api/";
        if (errorUri != null && errorUri.startsWith(apiPrefix)) {
            return true;
        }

        String accept = req.getHeader("Accept");
        return accept != null && accept.toLowerCase(java.util.Locale.ROOT).contains("application/json");
    }

    private ErrorContent getErrorContent(int statusCode) {
        switch (statusCode) {
            case 400:
                return new ErrorContent("Richiesta non valida", "La richiesta non può essere elaborata.");
            case 401:
                return new ErrorContent("Autenticazione richiesta", "Accedi nuovamente per continuare.");
            case 402:
                return new ErrorContent("Operazione non disponibile", "L'operazione richiesta non è disponibile.");
            case 403:
                return new ErrorContent("Accesso non consentito", "Non disponi delle autorizzazioni necessarie.");
            case 404:
                return new ErrorContent("Risorsa non trovata", "La pagina o la risorsa richiesta non è disponibile.");
            case 405:
                return new ErrorContent("Metodo non consentito", "Il metodo usato non è supportato per questa risorsa.");
            case 406:
                return new ErrorContent("Risposta non accettabile", "Il formato di risposta richiesto non è disponibile.");
            case 407:
                return new ErrorContent("Autenticazione di rete richiesta", "È necessaria l'autenticazione di rete.");
            case 408:
                return new ErrorContent("Tempo della richiesta scaduto", "La richiesta ha impiegato troppo tempo.");
            case 409:
                return new ErrorContent("Conflitto nella richiesta", "La richiesta è in conflitto con lo stato corrente.");
            case 410:
                return new ErrorContent("Risorsa non più disponibile", "La risorsa richiesta non è più disponibile.");
            case 411:
                return new ErrorContent("Lunghezza richiesta obbligatoria", "La richiesta non contiene una lunghezza valida.");
            case 412:
                return new ErrorContent("Precondizione non soddisfatta", "Una condizione della richiesta non è stata soddisfatta.");
            case 413:
                return new ErrorContent("Richiesta troppo grande", "La dimensione della richiesta supera il limite consentito.");
            case 414:
                return new ErrorContent("Indirizzo troppo lungo", "L'indirizzo della richiesta supera il limite consentito.");
            case 415:
                return new ErrorContent("Formato non supportato", "Il formato dei dati inviati non è supportato.");
            case 416:
                return new ErrorContent("Intervallo non valido", "L'intervallo richiesto non è disponibile.");
            case 417:
                return new ErrorContent("Condizione non soddisfatta", "La richiesta contiene una condizione non supportata.");
            case 421:
                return new ErrorContent("Richiesta non indirizzata correttamente", "Il server non può elaborare questa richiesta.");
            case 422:
                return new ErrorContent("Dati non elaborabili", "I dati inviati non possono essere elaborati.");
            case 423:
                return new ErrorContent("Risorsa bloccata", "La risorsa richiesta è temporaneamente bloccata.");
            case 424:
                return new ErrorContent("Dipendenza non disponibile", "Una risorsa necessaria non è disponibile.");
            case 425:
                return new ErrorContent("Richiesta non ancora elaborabile", "Riprova tra qualche istante.");
            case 426:
                return new ErrorContent("Aggiornamento del protocollo richiesto", "Il protocollo usato deve essere aggiornato.");
            case 428:
                return new ErrorContent("Precondizione richiesta", "La richiesta deve includere una precondizione.");
            case 429:
                return new ErrorContent("Troppe richieste", "Attendi qualche istante prima di riprovare.");
            case 431:
                return new ErrorContent("Intestazioni troppo grandi", "Le intestazioni della richiesta superano il limite consentito.");
            case 451:
                return new ErrorContent("Risorsa non disponibile", "La risorsa richiesta non può essere resa disponibile.");
            case 500:
                return new ErrorContent("Errore interno", "Si è verificato un errore. Riprova più tardi.");
            case 501:
                return new ErrorContent("Funzionalità non disponibile", "La funzionalità richiesta non è supportata.");
            case 502:
                return new ErrorContent("Risposta del servizio non valida", "Un servizio necessario ha restituito una risposta non valida.");
            case 503:
                return new ErrorContent("Servizio temporaneamente non disponibile", "Il servizio non è disponibile. Riprova più tardi.");
            case 504:
                return new ErrorContent("Tempo del servizio scaduto", "Un servizio necessario non ha risposto in tempo.");
            case 505:
                return new ErrorContent("Versione HTTP non supportata", "La versione del protocollo usata non è supportata.");
            case 506:
                return new ErrorContent("Configurazione non valida", "Il server non può completare la richiesta.");
            case 507:
                return new ErrorContent("Spazio insufficiente", "Il server non dispone delle risorse necessarie.");
            case 508:
                return new ErrorContent("Operazione ciclica rilevata", "Il server non può completare la richiesta.");
            case 510:
                return new ErrorContent("Estensione richiesta", "La richiesta necessita di un'estensione non disponibile.");
            case 511:
                return new ErrorContent("Autenticazione di rete richiesta", "Autenticati sulla rete per continuare.");
            default:
                if (statusCode >= 400 && statusCode < 500) {
                    return new ErrorContent("Richiesta non completata", "Non è stato possibile completare la richiesta.");
                }
                return new ErrorContent("Servizio non disponibile", "Si è verificato un errore. Riprova più tardi.");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class ErrorContent {

        private final String title;
        private final String message;

        private ErrorContent(String title, String message) {
            this.title = title;
            this.message = message;
        }
    }
}
