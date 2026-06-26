package tracker.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            String path = h.getRequestURI().getPath();
            String[] parts = path.split("/");

            switch (method) {
                case "GET" -> handleGet(h, parts);
                case "POST" -> handlePost(h);
                case "DELETE" -> handleDelete(h, parts);
                default -> sendNotFound(h, "Метод не поддерживается");
            }
        } catch (Exception e) {
            sendInternalError(h, e.getMessage() != null ? e.getMessage() : "Внутренняя ошибка сервера");
        }
    }

    private void handleGet(HttpExchange h, String[] parts) throws IOException {
        if (parts.length == 2) {
            // GET /epics
            List<Epic> epics = manager.getEpics();
            sendText(h, gson.toJson(epics));
        } else if (parts.length == 3) {
            // GET /epics/{id}
            int id = Integer.parseInt(parts[2]);
            try {
                Epic epic = manager.getEpic(id);
                sendText(h, gson.toJson(epic));
            } catch (NotFoundException e) {
                sendNotFound(h, e.getMessage());
            }
        } else if (parts.length == 4 && "subtasks".equals(parts[3])) {
            // GET /epics/{id}/subtasks
            int id = Integer.parseInt(parts[2]);
            try {
                manager.getEpic(id); // check existence
                List<Subtask> subtasks = manager.getEpicSubtasks(id);
                sendText(h, gson.toJson(subtasks));
            } catch (NotFoundException e) {
                sendNotFound(h, e.getMessage());
            }
        } else {
            sendNotFound(h, "Неверный путь");
        }
    }

    private void handlePost(HttpExchange h) throws IOException {
        String body = readBody(h);
        if (body.isBlank()) {
            sendNotFound(h, "Тело запроса пустое");
            return;
        }
        Epic epic = gson.fromJson(body, Epic.class);
        if (epic.getId() == 0) {
            manager.createEpic(epic);
        } else {
            try {
                manager.getEpic(epic.getId()); // check existence
            } catch (NotFoundException e) {
                sendNotFound(h, e.getMessage());
                return;
            }
            manager.updateEpic(epic);
        }
        sendCreated(h);
    }

    private void handleDelete(HttpExchange h, String[] parts) throws IOException {
        if (parts.length != 3) {
            sendNotFound(h, "Неверный путь");
            return;
        }
        int id = Integer.parseInt(parts[2]);
        try {
            manager.getEpic(id); // check existence
            manager.removeEpicById(id);
            sendText(h, "{\"message\":\"Эпик удалён\"}");
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        }
    }
}
