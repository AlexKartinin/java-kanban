package tracker.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
            // GET /subtasks
            List<Subtask> subtasks = manager.getSubtasks();
            sendText(h, gson.toJson(subtasks));
        } else if (parts.length == 3) {
            // GET /subtasks/{id}
            int id = Integer.parseInt(parts[2]);
            try {
                Subtask subtask = manager.getSubtask(id);
                sendText(h, gson.toJson(subtask));
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
        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask.getId() == 0) {
                Subtask created = manager.createSubtask(subtask);
                if (created == null) {
                    sendNotFound(h, "Эпик для подзадачи не найден");
                    return;
                }
            } else {
                try {
                    manager.getSubtask(subtask.getId()); // check existence
                } catch (NotFoundException e) {
                    sendNotFound(h, e.getMessage());
                    return;
                }
                manager.updateSubtask(subtask);
            }
            sendCreated(h);
        } catch (IllegalArgumentException e) {
            sendHasInteractions(h);
        }
    }

    private void handleDelete(HttpExchange h, String[] parts) throws IOException {
        if (parts.length != 3) {
            sendNotFound(h, "Неверный путь");
            return;
        }
        int id = Integer.parseInt(parts[2]);
        try {
            manager.getSubtask(id); // check existence
            manager.removeSubtaskById(id);
            sendText(h, "{\"message\":\"Подзадача удалена\"}");
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        }
    }
}
