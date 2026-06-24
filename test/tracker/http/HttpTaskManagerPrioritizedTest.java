package tracker.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerPrioritizedTest() throws IOException {
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
    public void testPrioritizedIsEmptyInitially() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> prioritized = gson.fromJson(response.body(), listType);
        assertTrue(prioritized.isEmpty());
    }

    @Test
    public void testPrioritizedSortedByStartTime() throws IOException, InterruptedException {
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 10, 0);

        Task t1 = new Task(0, "T1", "d");
        t1.setStartTime(base.plusHours(2));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task(0, "T2", "d");
        t2.setStartTime(base);
        t2.setDuration(Duration.ofMinutes(30));

        Task t3 = new Task(0, "T3", "d");
        t3.setStartTime(base.plusHours(1));
        t3.setDuration(Duration.ofMinutes(30));

        Task c1 = manager.createTask(t1);
        Task c2 = manager.createTask(t2);
        Task c3 = manager.createTask(t3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> prioritized = gson.fromJson(response.body(), listType);
        assertEquals(3, prioritized.size());
        assertEquals(c2.getId(), prioritized.get(0).getId(), "Первой должна быть самая ранняя задача");
        assertEquals(c3.getId(), prioritized.get(1).getId());
        assertEquals(c1.getId(), prioritized.get(2).getId());
    }

    @Test
    public void testTasksWithoutStartTimeNotInPrioritized() throws IOException, InterruptedException {
        Task withTime = new Task(0, "T1", "d");
        withTime.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        withTime.setDuration(Duration.ofMinutes(30));

        Task withoutTime = new Task(0, "T2", "d");

        manager.createTask(withTime);
        manager.createTask(withoutTime);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> prioritized = gson.fromJson(response.body(), listType);
        assertEquals(1, prioritized.size(), "Задачи без startTime не должны попасть в список");
    }
}
