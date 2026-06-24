package tracker.http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import tracker.model.Task;
import tracker.model.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class TaskTypeAdapter extends TypeAdapter<Task> {

    @Override
    public void write(JsonWriter out, Task task) throws IOException {
        if (task == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("id").value(task.getId());
        out.name("type").value(task.getType().toString());
        out.name("name").value(task.getName());
        out.name("description").value(task.getDescription());
        out.name("status").value(task.getStatus().toString());
        if (task.getDuration() != null) {
            out.name("duration").value(task.getDuration().toMinutes());
        } else {
            out.name("duration").nullValue();
        }
        if (task.getStartTime() != null) {
            out.name("startTime").value(task.getStartTime().toString());
        } else {
            out.name("startTime").nullValue();
        }
        out.endObject();
    }

    @Override
    public Task read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        int id = 0;
        String name = null;
        String description = null;
        TaskStatus status = TaskStatus.NEW;
        Duration duration = null;
        LocalDateTime startTime = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id": id = in.nextInt(); break;
                case "name": name = in.nextString(); break;
                case "description":
                    if (in.peek() == JsonToken.NULL) { in.nextNull(); } else { description = in.nextString(); }
                    break;
                case "status":
                    if (in.peek() != JsonToken.NULL) status = TaskStatus.valueOf(in.nextString());
                    else in.nextNull();
                    break;
                case "duration":
                    if (in.peek() != JsonToken.NULL) duration = Duration.ofMinutes(in.nextLong());
                    else in.nextNull();
                    break;
                case "startTime":
                    if (in.peek() != JsonToken.NULL) startTime = LocalDateTime.parse(in.nextString());
                    else in.nextNull();
                    break;
                default: in.skipValue(); break;
            }
        }
        in.endObject();

        Task task = new Task(id, name != null ? name : "", description != null ? description : "");
        task.setStatus(status);
        task.setDuration(duration);
        task.setStartTime(startTime);
        return task;
    }
}
