package tracker.http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import tracker.model.Epic;

import java.io.IOException;

public class EpicTypeAdapter extends TypeAdapter<Epic> {

    @Override
    public void write(JsonWriter out, Epic epic) throws IOException {
        if (epic == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("id").value(epic.getId());
        out.name("type").value(epic.getType().toString());
        out.name("name").value(epic.getName());
        out.name("description").value(epic.getDescription());
        out.name("status").value(epic.getStatus().toString());
        if (epic.getDuration() != null) {
            out.name("duration").value(epic.getDuration().toMinutes());
        } else {
            out.name("duration").nullValue();
        }
        if (epic.getStartTime() != null) {
            out.name("startTime").value(epic.getStartTime().toString());
        } else {
            out.name("startTime").nullValue();
        }
        if (epic.getEndTime() != null) {
            out.name("endTime").value(epic.getEndTime().toString());
        } else {
            out.name("endTime").nullValue();
        }
        out.endObject();
    }

    @Override
    public Epic read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        int id = 0;
        String name = null;
        String description = null;

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id": id = in.nextInt(); break;
                case "name": name = in.nextString(); break;
                case "description":
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                    } else {
                        description = in.nextString();
                    }
                    break;
                default: in.skipValue(); break;
            }
        }
        in.endObject();

        return new Epic(id, name != null ? name : "", description != null ? description : "");
    }
}
