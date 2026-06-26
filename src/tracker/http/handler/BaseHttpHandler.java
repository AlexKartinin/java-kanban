package tracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected enum HttpStatus {
        OK(200),
        CREATED(201),
        NOT_FOUND(404),
        NOT_ACCEPTABLE(406),
        INTERNAL_ERROR(500);

        final int code;

        HttpStatus(int code) {
            this.code = code;
        }
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpStatus.OK.code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreated(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpStatus.CREATED.code, -1);
        h.close();
    }

    protected void sendNotFound(HttpExchange h, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(HttpStatus.NOT_FOUND.code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        String msg = "Задача пересекается с уже существующей";
        byte[] resp = msg.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(HttpStatus.NOT_ACCEPTABLE.code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendInternalError(HttpExchange h, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(HttpStatus.INTERNAL_ERROR.code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected String readBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected int parseId(String path) {
        String[] parts = path.split("/");
        // path like /tasks/5 → parts = ["", "tasks", "5"]
        return Integer.parseInt(parts[parts.length - 1]);
    }
}
