package tracker.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerHistoryTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.removeTasks();
        manager.removeSubtasks();
        manager.removeEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testHistoryIsEmptyInitially() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> history = gson.fromJson(response.body(), listType);
        assertTrue(history.isEmpty());
    }

    @Test
    public void testHistoryAfterViewing() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task(0, "Task 1", "Desc"));
        Epic epic = manager.createEpic(new Epic(0, "Epic 1", "Desc"));

        // Просматриваем задачи через HTTP GET /tasks/{id} и /epics/{id}
        HttpClient client = HttpClient.newHttpClient();
        client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/" + task.getId())).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/" + epic.getId())).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        URI historyUrl = URI.create("http://localhost:8080/history");
        HttpRequest historyRequest = HttpRequest.newBuilder().uri(historyUrl).GET().build();
        HttpResponse<String> response = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> history = gson.fromJson(response.body(), listType);
        assertEquals(2, history.size());
    }

    @Test
    public void testHistoryNoDuplicates() throws IOException, InterruptedException {
        Task task = manager.createTask(new Task(0, "Task", "Desc"));

        HttpClient client = HttpClient.newHttpClient();
        URI taskUrl = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest req = HttpRequest.newBuilder().uri(taskUrl).GET().build();

        // Просматриваем задачу три раза
        client.send(req, HttpResponse.BodyHandlers.ofString());
        client.send(req, HttpResponse.BodyHandlers.ofString());
        client.send(req, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/history")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> history = gson.fromJson(response.body(), listType);
        assertEquals(1, history.size(), "История не должна содержать дубликаты");
    }
}
