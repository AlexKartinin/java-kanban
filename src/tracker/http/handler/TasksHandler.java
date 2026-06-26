package tracker.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.NotFoundException;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
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
            // GET /tasks
            List<Task> tasks = manager.getTasks();
            sendText(h, gson.toJson(tasks));
        } else if (parts.length == 3) {
            // GET /tasks/{id}
            int id = Integer.parseInt(parts[2]);
            try {
                Task task = manager.getTask(id);
                sendText(h, gson.toJson(task));
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
            Task task = gson.fromJson(body, Task.class);
            if (task.getId() == 0) {
                manager.createTask(task);
            } else {
                try {
                    manager.getTask(task.getId()); // check existence
                } catch (NotFoundException e) {
                    sendNotFound(h, e.getMessage());
                    return;
                }
                manager.updateTask(task);
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
            manager.getTask(id); // check existence
            manager.removeTaskById(id);
            sendText(h, "{\"message\":\"Задача удалена\"}");
        } catch (NotFoundException e) {
            sendNotFound(h, e.getMessage());
        }
    }
}
