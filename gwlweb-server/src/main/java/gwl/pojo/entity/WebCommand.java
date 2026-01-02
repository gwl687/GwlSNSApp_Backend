package gwl.pojo.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class WebCommand {
    private long toUser;
    private long fromUser;
    private String command;
    private String type;
    public WebCommand(JsonNode json) {
        this.toUser = json.get("toUser").asLong();
        this.command = json.get("command").asText();
        this.type = json.get("type").asText();
    }
}
