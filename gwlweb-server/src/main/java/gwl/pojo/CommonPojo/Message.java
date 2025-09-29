package gwl.pojo.CommonPojo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Message {
    public long toUser;
    public String content;

    public Message(JsonNode json) {
        this.toUser = json.get("toUser").asLong();
        this.content = json.get("content").asText();
    }
}
