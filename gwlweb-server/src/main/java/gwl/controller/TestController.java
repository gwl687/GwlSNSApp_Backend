package gwl.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gwl.context.BaseContext;
import gwl.dubboService.DubboGreetingService;
import gwl.entity.User;
import gwl.pojo.DTO.UserLoginDTO;
import gwl.pojo.VO.FriendListVO;
import gwl.pojo.VO.UserLoginVO;
import gwl.result.Result;
import gwl.service.UserService;
import gwl.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户相关接口")
public class TestController {

    private DubboGreetingService dbs;
    @Autowired
    private UserService userService;

    /**
     * dubbo相关
     */
    // @DubboReference
    @GetMapping("/requestDubbo")
    String requestDubbo(@RequestParam String name) {
        // return dbs.sayHello(name);
        return "";
    }

    /**
     * 用户登录相关
     * 
     * @param userLoginDTO
     * @return
     */
    @PostMapping(path = "/login", produces = "application/json")
    @Operation(summary = "用户登录相关")
    Result<UserLoginVO> Login(@org.springframework.web.bind.annotation.RequestBody UserLoginDTO userLoginDTO) {
        log.info("员工登录：{}", userLoginDTO);
        try {
            User user = userService.userLogin(userLoginDTO);
            // 登录成功后生成令牌
            String token = JwtUtil.generateToken(user.getId());
            UserLoginVO userloginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .userName(user.getUsername())
                    .name(user.getName())
                    .token(token)
                    .build();
            return Result.success(userloginVO);
        } catch (Exception e) {
            log.info("登录失败");
        }
        return Result.success(new UserLoginVO());
    }

    /**
     * 获取好友列表
     * 
     * @return
     */
    @GetMapping(path = "/getFriendList", produces = "application/json")
    @Operation(summary = "获取好友列表")
    Result<List<FriendListVO>> getFriendList() {    
        List<FriendListVO> friendListVOs = new ArrayList<FriendListVO>();
        List<User> users = userService.getFriendList();
        for (User user : users) {
            friendListVOs.add(FriendListVO
                    .builder()
                    .name(user.getName())
                    .userName(user.getUsername())
                    .id(user.getId())
                    .build());
        }
        log.info("用户{}获取好友列表",BaseContext.getCurrentId());
        log.info("好友列表长度: ", users.size());
        return Result.success(friendListVOs);
    }

    Result<FriendListVO> Register() {
        return Result.success(new FriendListVO());
    }

}
