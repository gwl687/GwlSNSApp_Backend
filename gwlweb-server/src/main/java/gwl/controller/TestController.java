package gwl.controller;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.apache.dubbo.config.annotation.DubboReference;
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
import gwl.dubboService.DubboGreetingService;
import gwl.entity.GroupChat;
import gwl.entity.User;
import gwl.mapper.UserMapper;
import gwl.pojo.DTO.AddFriendToChatListDTO;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.pojo.DTO.GetGroupChatDTO;
import gwl.pojo.DTO.GroupmessageDTO;
import gwl.pojo.DTO.UserInfoDTO;
import gwl.pojo.DTO.UserLoginDTO;
import gwl.pojo.VO.FriendListVO;
import gwl.pojo.VO.GroupChatVO;
import gwl.pojo.VO.GroupMessagesVO;
import gwl.pojo.VO.UserInfoVO;
import gwl.pojo.VO.UserLoginVO;
import gwl.result.Result;
import gwl.service.CommonService;
import gwl.service.UserService;
import gwl.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
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
    @Autowired
    private StringRedisTemplate redis;
    @Autowired
    private CommonService commonService;
    @Autowired
    private UserMapper userMapper;

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
            redis.opsForValue().set("push_token:" + user.getId(), userLoginDTO.getPushToken());
            // 登录成功后生成令牌
            String token = JwtUtil.generateToken(user.getId());
            UserLoginVO userloginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .userName(user.getUsername())
                    .name(user.getName())
                    .avatarUrl(user.getAvatarurl())
                    .token(token)
                    .build();
            return Result.success(userloginVO);
        } catch (Exception e) {
            System.out.println("出现异常：" + e.getMessage());
            e.printStackTrace();
        }
        return Result.success(new UserLoginVO());
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
                .username(user.getUsername())
                .sex(user.getSex())
                .avatarurl(user.getAvatarurl())
                .emailaddress(user.getEmailadress())
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

    /**
     * 获取好友列表
     * 
     * @return
     */
    @GetMapping(path = "/getfriendList", produces = "application/json")
    @Operation(summary = "获取好友列表")
    Result<List<FriendListVO>> getFriendList() {
        List<FriendListVO> friendListVOs = new ArrayList<FriendListVO>();
        List<User> users = userService.getFriendList();
        for (User user : users) {
            friendListVOs.add(FriendListVO
                    .builder()
                    .userName(user.getUsername())
                    .id(user.getId())
                    .build());
        }
        log.info("用户{}获取好友列表", BaseContext.getCurrentId());
        log.info("好友列表长度: ", users.size());
        return Result.success(friendListVOs);
    }

    Result<FriendListVO> Register() {
        return Result.success(new FriendListVO());
    }

    /**
     * 创建群聊
     * 
     * @param createGroupChatDTO
     * @return
     */
    @PostMapping(path = "/creategroupchat", produces = "application/json")
    @Operation(summary = "创建群聊")
    Result<GroupChatVO> createGroupChat(
            @org.springframework.web.bind.annotation.RequestBody CreateGroupChatDTO createGroupChatDTO) {
        log.info("创建群：{}", createGroupChatDTO);
        GroupChatVO groupChatVO = new GroupChatVO();
        groupChatVO = userService.createGroupChat(createGroupChatDTO);
        return Result.success(groupChatVO);
    }

    /**
     * 获取群聊信息
     * 
     * @param getGroupChatDTO
     * @return
     */
    @GetMapping(path = "/getgroupchat", produces = "application/json")
    @Operation(summary = "获取群信息")
    Result<GroupChatVO> getGroupChat(@RequestParam("groupId") Long groupId) {
        GroupChat groupChat = userService.getGroupChat(groupId);
        List<Long> memberIds = Arrays.stream(groupChat.getMemberIds()
                .split(","))
                .filter(s -> !s.isBlank())
                .map(Long::valueOf)
                .toList();
        GroupChatVO groupChatVO = GroupChatVO.builder()
                .groupId(groupChat.getGroupId())
                .memberIds(memberIds)
                .ownerId(groupChat.getOwnerId())
                .groupName(groupChat.getGroupName())
                .avatarUrl("")
                .build();
        return Result.success(groupChatVO);
    }

    /**
     * 添加朋友或群到聊天列表
     * 
     * @param addFriendToChatListDTO
     * @return
     */
    @PostMapping(path = "/addfriendtochatlist", produces = "application/json")
    @Operation(summary = "添加朋友或群到聊天列表")
    Result addFriendToChatList(
            @org.springframework.web.bind.annotation.RequestBody AddFriendToChatListDTO addFriendToChatListDTO) {
        log.info("添加朋友或群到聊天列表：{}", addFriendToChatListDTO);
        userService.addFriendToChatList(addFriendToChatListDTO);
        return Result.success();
    }

    @GetMapping(path = "/getchatlist", produces = "application/json")
    @Operation(summary = "获取聊天列表")
    // 用GroupChat，包含了返回普通朋友所需字段
    Result<?> getChatList() {
        log.info("获取聊天列表");
        List<?> result = userService.getChatList();
        return Result.success(result);
    }

    /**
     * 保存群消息
     * 
     * @param groupmessageDTO
     * @return
     */
    @PostMapping(path = "/savegroupmessage", produces = "application/json")
    @Operation(summary = "保存群聊消息")
    Result<Boolean> saveGroupMessage(
            @org.springframework.web.bind.annotation.RequestBody GroupmessageDTO groupMessageDTO) {
        // userService.saveGroupMessage(groupMessageDTO);
        return Result.success(true);
    }

    /**
     * 获取群消息
     * 
     * @param groupId
     * @return
     */
    @GetMapping(path = "/getgroupmessages", produces = "application/json")
    @Operation(summary = "获取群聊消息")
    Result<List<GroupMessagesVO>> getGroupMessages(@RequestParam("groupId") Long groupId) {
        return Result.success(userService.getGroupMessages(groupId));
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
    Result<Boolean> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String uploadKey = "avatar/" + BaseContext.getCurrentId();
        commonService.uploadToS3(file, uploadKey);
        return Result.success(true);
    }
    /**
     * 获取用户头像url
     * @param userId
     * @return
     */
    @GetMapping(path = "/getuseravatar", produces = "application/json")
    @Operation(summary = "getUserAvatar")
    Result<String> getUserAvatar(@RequestParam("userId") Long userId) {
        return Result.success(userMapper.getUserAvatarUrl(userId));
    }
}
