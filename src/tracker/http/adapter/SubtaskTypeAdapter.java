package tracker.http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.TaskStatus;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskTypeAdapter extends TypeAdapter<Subtask> {

    @Override
    public void write(JsonWriter out, Subtask subtask) throws IOException {
        if (subtask == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("id").value(subtask.getId());
        out.name("type").value(subtask.getType().toString());
        out.name("name").value(subtask.getName());
        out.name("description").value(subtask.getDescription());
        out.name("status").value(subtask.getStatus().toString());
        if (subtask.getDuration() != null) {
            out.name("duration").value(subtask.getDuration().toMinutes());
        } else {
            out.name("duration").nullValue();
        }
        if (subtask.getStartTime() != null) {
            out.name("startTime").value(subtask.getStartTime().toString());
        } else {
            out.name("startTime").nullValue();
        }
        out.name("epicId").value(subtask.getEpic() != null ? subtask.getEpic().getId() : 0);
        out.endObject();
    }

    @Override
    public Subtask read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        int id = 0;
        int epicId = 0;
        String name = null;
        String description = null;
        TaskStatus status = TaskStatus.NEW;
        Duration duration = null;
        LocalDateTime startTime = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id": id = in.nextInt(); break;
                case "epicId": epicId = in.nextInt(); break;
                case "name": name = in.nextString(); break;
                case "description":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        description = in.nextString();
                    }
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

        // Create a dummy Epic with the epicId so the manager can look it up
        Epic dummyEpic = new Epic(epicId, "");
        Subtask subtask = new Subtask(id, name != null ? name : "", description != null ? description : "", dummyEpic);
        // restoreStatus bypasses notifyEpic
        subtask.restoreStatus(status);
        // setDuration/setStartTime call notifyEpic which calls dummyEpic.recalculate() — harmless
        if (duration != null) subtask.setDuration(duration);
        if (startTime != null) subtask.setStartTime(startTime);
        return subtask;
    }
}
