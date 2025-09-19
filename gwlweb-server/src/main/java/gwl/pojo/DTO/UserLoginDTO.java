package gwl.pojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserLoginDTO {
    @Schema(description = "邮箱")
    private String emailaddress;
    @Schema(description = "密码")
    private String password;
}
