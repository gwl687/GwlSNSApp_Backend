package gwl.pojo.CommonPojo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class WebCommand {
    public int toUserId;
    public String command;
    public WebCommand(JsonNode json) {
        this.toUserId = json.get("userId").asInt();
        this.command = json.get("command").asText();
    }
}
