package gwl.pojo.CommonPojo;

import com.fasterxml.jackson.databind.JsonNode;

import gwl.components.NettyHandlers.DispatcherHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Message {
    private long toUser;
    private long fromUser;
    private String content;

    public Message(JsonNode json) {
        this.toUser = json.get("toUser").asLong();
        this.content = json.get("content").asText();
    }
}
