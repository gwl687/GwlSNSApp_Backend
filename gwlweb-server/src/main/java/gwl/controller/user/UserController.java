package gwl.controller.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import gwl.context.BaseContext;
import gwl.mapper.UserMapper;
import gwl.pojo.dto.UserInfoDTO;
import gwl.pojo.dto.UserLoginDTO;
import gwl.pojo.entity.User;
import gwl.pojo.vo.FriendListVO;
import gwl.pojo.vo.UserInfoVO;
import gwl.pojo.vo.UserLoginVO;
import gwl.result.Result;
import gwl.service.CommonService;
import gwl.service.UserService;
import gwl.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@Slf4j
@Tag(name = "用户相关接口")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private CommonService commonService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录相关
     * 
     * @param userLoginDTO
     * @return
     */
    @PostMapping(path = "/login", produces = "application/json")
    @Operation(summary = "用户登录相关")
    Result<UserLoginVO> Login(@org.springframework.web.bind.annotation.RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);
        User user = userService.userLogin(userLoginDTO);
        redis.opsForValue().set("push_token:" + user.getId(), userLoginDTO.getPushToken());
        // 登录成功后生成令牌
        String token = JwtUtil.generateToken(user.getId());
        UserLoginVO userloginVO = UserLoginVO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .avatarUrl(user.getAvatarurl())
                .token(token)
                .emailaddress(user.getEmailaddress())
                .build();
        return Result.success(userloginVO);
    }

    /**
     * 获取用户信息
     * 
     * @return
     */
    @GetMapping(path = "getuserinfo", produces = "application/json")
    Result<UserInfoVO> getUserInfo() {
        User user = userService.getUserInfo();
        UserInfoVO userInfoVO = UserInfoVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .sex(user.getSex())
                .avatarurl(user.getAvatarurl())
                .emailaddress(user.getEmailaddress())
                .build();
        return Result.success(userInfoVO);
    }

    /**
     * 更新用户信息
     * 
     * @param updateUserInfoDTO
     * @return
     */
    @PostMapping(path = "/updateuserinfo", produces = "application/json")
    Result<Boolean> updateUserInfo(UserInfoDTO userInfoDTO) {
        return Result.success(userService.updateUserInfo(userInfoDTO));
    }

    // 发送验证码
    Result<Void> sendVerificationCode() {
        return Result.success();
    }

    // 注册
    Result<Void> userRegister() {
        return Result.success();
    }

    /**
     * 上传头像
     * 
     * @param file
     * @return
     * @throws IOException
     */
    @PutMapping(path = "/uploadavatar", produces = "application/json")
    @Operation(summary = "upload avatar")
    Result<Boolean> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String uploadKey = "avatar/" + BaseContext.getCurrentId();
        commonService.uploadToS3(file, uploadKey);
        return Result.success(true);
    }

    /**
     * 获取用户头像url
     * 
     * @param userId
     * @return
     */
    @GetMapping(path = "/getuseravatar", produces = "application/json")
    @Operation(summary = "getUserAvatar")
    Result<String> getUserAvatar(@RequestParam("userId") Long userId) {
        return Result.success(userMapper.getUserAvatarUrl(userId));
    }

}
