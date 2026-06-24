package tracker.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (!"GET".equals(h.getRequestMethod())) {
                sendNotFound(h, "Метод не поддерживается");
                return;
            }
            List<Task> prioritized = manager.getPrioritizedTasks();
            sendText(h, gson.toJson(prioritized));
        } catch (Exception e) {
            sendInternalError(h, e.getMessage() != null ? e.getMessage() : "Внутренняя ошибка сервера");
        }
    }
}
