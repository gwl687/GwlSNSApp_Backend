package gwl.service.Impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.login.AccountNotFoundException;
import javax.swing.plaf.synth.Region;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gwl.components.NettyHandlers.DispatcherHandler;
import gwl.constant.MessageConstant;
import gwl.constant.StatusConstant;
import gwl.context.BaseContext;
import gwl.entity.GroupChat;
import gwl.mapper.UserMapper;
import gwl.pojo.CommonPojo.Message;
import gwl.pojo.CommonPojo.WebCommand;
import gwl.pojo.DTO.AddFriendToChatListDTO;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.pojo.DTO.GetGroupChatDTO;
import gwl.pojo.DTO.GroupmessageDTO;
import gwl.pojo.DTO.UserInfoDTO;
import gwl.pojo.DTO.UserLoginDTO;
import gwl.pojo.VO.GroupChatVO;
import gwl.pojo.VO.GroupMessagesVO;
import gwl.pojo.VO.UserInfoVO;
import gwl.pojo.entity.ChatFriend;
import gwl.pojo.entity.ChatListId;
import gwl.service.UserService;
import gwl.util.CommonUtil;
import io.jsonwebtoken.io.IOException;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    // AWS property
    @org.springframework.beans.factory.annotation.Value("${aws.accessKey}")
    private String accessKey;

    @org.springframework.beans.factory.annotation.Value("${aws.secretKey}")
    private String secretKey;

    @org.springframework.beans.factory.annotation.Value("${aws.region}")
    private String region;

    @org.springframework.beans.factory.annotation.Value("${aws.s3.avatar}")
    private String bucketOrAccessPoint;

    @Override
    public gwl.entity.User userLogin(UserLoginDTO userLoginDTO) {
        String emailaddress = userLoginDTO.getEmailaddress();
        String password = userLoginDTO.getPassword();
        gwl.entity.User user = userMapper.getByUserEmail(emailaddress);
        if (user == null) {
            throw new RuntimeException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 密码比对
        if (!password.equals(user.getPassword())) {
            // 密码错误
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new RuntimeException(MessageConstant.ACCOUNT_LOCKED);
        }
        return user;
    }

    /**
     * 更新用户信息
     * 
     * @return
     */
    public gwl.entity.User getUserInfo() {
        gwl.entity.User user = userMapper.getByUserId(BaseContext.getCurrentId());
        return user;
    }

    /**
     * 更新用户信息
     * 
     * @return
     */
    public Boolean updateUserInfo(UserInfoDTO userInfoDTO) {
        Boolean result = userMapper.updateUserInfo(userInfoDTO) > 0 ? true : false;
        return result;
    }

    /*
     * 获取朋友列表
     */
    @Override
    public List<gwl.entity.User> getFriendList() {
        return userMapper.getFriendListByUserId(BaseContext.getCurrentId());
    }

    /*
     * 添加朋友或群到聊天列表
     */
    @Override
    public void addFriendToChatList(AddFriendToChatListDTO addFriendToChatListDTO) {
        userMapper.addFriendToChatList(addFriendToChatListDTO);
    }

    /**
     * 获取聊天列表(朋友)
     */
    @Override
    public List<?> getChatList() {
        // 这里获取好友的，和群的，然后放入同一个list返回
        //
        List<Object> result = new ArrayList<>();
        List<ChatListId> chatListIds = userMapper.getChatListIdById(BaseContext.getCurrentId());
        for (ChatListId chatListId : chatListIds) {
            // 如果是群
            if (chatListId.getIsGroup()) {
                GroupChat groupChat = userMapper.getChatGroupByChatId(chatListId.getId());
                List<Long> memberIds = Arrays.stream(groupChat.getMemberIds().split(","))
                        .map(Long::valueOf)
                        .toList();
                GroupChatVO groupChatVO = GroupChatVO.builder()
                        .groupId(groupChat.getGroupId())
                        .groupName(groupChat.getGroupName())
                        .ownerId(groupChat.getOwnerId())
                        .memberIds(memberIds)
                        // 先传默认头像,测试用
                        .avatarUrl("https://i.pravatar.cc/150?img=1")
                        // .avatarUrl(groupChat.getAvatarUrl())
                        .build();
                result.add(groupChatVO);
            } else {
                ChatFriend chatFriend = userMapper.getChatFriendByChatId(chatListId.getId());
                // 先传默认头像,测试用
                chatFriend.setAvatarurl("https://i.pravatar.cc/150?img=1");
                result.add(chatFriend);
            }
        }
        return result;
    }

    /**
     * 创建群聊
     */
    @Override
    public GroupChatVO createGroupChat(CreateGroupChatDTO createGroupChatDTO) {
        List<Long> groupChatMembers = createGroupChatDTO.getSelectedFriends();
        List<String> groupNameList = new ArrayList<>();
        gwl.entity.User owner = userMapper.getByUserId(BaseContext.getCurrentId());
        groupNameList.add(owner.getUsername());
        for (Long memberId : groupChatMembers) {
            gwl.entity.User groupMember = userMapper.getByUserId(memberId);
            groupNameList.add(groupMember.getUsername());
        }
        String groupName = String.join(",", groupNameList);
        String groupChatMembersStr = groupChatMembers.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        GroupChat groupchat = GroupChat.builder()
                .memberIds(groupChatMembersStr)
                .ownerId(BaseContext.getCurrentId())
                .groupName(groupName)
                .avatarUrl("https://i.pravatar.cc/150?img=1")
                .build();
        groupChatMembers.add(BaseContext.getCurrentId());
        userMapper.createGroupChat(groupchat);
        GroupChatVO groupChatVO = GroupChatVO.builder()
                .groupId(groupchat.getGroupId())
                .memberIds(groupChatMembers)
                .ownerId(groupchat.getOwnerId())
                .groupName(groupchat.getGroupName())
                .avatarUrl(groupchat.getAvatarUrl())
                .build();
        userMapper.insertGroupChatMember(groupChatVO);
        Long groupId = groupchat.getGroupId();

        // 向被拉到群里的用户发message,更新聊天列表(群名称)
        try {
            for (Long id : groupChatMembers) {
                if (id == BaseContext.getCurrentId()) {
                    continue; // 不发给自己
                }
                Channel toChannel = DispatcherHandler.userMap.get(id);
                WebCommand webCommand = WebCommand.builder()
                        .fromUser(BaseContext.getCurrentId())
                        .toUser(id)
                        .type("joinGroupChat_" + groupId + "_" + groupName)
                        .build();
                String sendMessage = CommonUtil.mapper.writeValueAsString(webCommand);
                toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
            }
        } catch (Exception e) {
            log.error("Exception occurred", e);
        }
        log.info("用户" + BaseContext.getCurrentId() + "创建群聊");
        return groupChatVO;
    }

    /**
     * 获取群信息
     */
    @Override
    public GroupChat getGroupChat(Long groupId) {
        GroupChat groupChat = userMapper.getGroupChat(groupId);
        // for (Long id : groupChat.getMemberIds()) {
        // System.out.println("群成员id为: " + id);
        // }
        return groupChat;
    }

    /**
     * 获取群消息
     */
    @Override
    public List<GroupMessagesVO> getGroupMessages(Long groupId) {
        List<GroupMessagesVO> groupMessagesVOs = userMapper.getGroupMessages(groupId);
        return groupMessagesVOs;
    }

    @Override
    public void saveGroupMessage(Message message) {
        userMapper.saveGroupMessage(message);
    }

    /**
     * 上传头像
     * 
     * @param file
     * @return
     * @throws java.io.IOException 
     */
    @Override
    public Boolean uploadAvatar(MultipartFile file) throws java.io.IOException {

        // 创建 S3 客户端
        try (
                S3Client s3 = S3Client.builder()
                        .region(software.amazon.awssdk.regions.Region.of(region))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                        .build()) {

            // 上传路径，例如：avatars/时间戳_文件名
            String key = "avatars/" + BaseContext.getCurrentId();

            // 构建上传请求
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketOrAccessPoint) // 可以是 arn:aws:s3:... 或 bucket 名
                    .key(key)
                    .contentType(file.getContentType())
                    .acl("public-read") // 让头像可直接访问（可选）
                    .build();

            // 上传文件
            s3.putObject(request, RequestBody.fromBytes(file.getBytes()));
            // 拼接公开URL
            String url = "https://gwltest-01.s3.ap-northeast-1.amazonaws.com/" + key;
            System.out.println("✅ 上传成功: " + key);
            UserInfoDTO userInfoDTO = UserInfoDTO.builder()
            .id(BaseContext.getCurrentId())
            .avatarurl(url)
            .username(userMapper.getByUserId(BaseContext.getCurrentId()).getUsername())
            .sex(userMapper.getByUserId(BaseContext.getCurrentId()).getSex())
            .build();
            Boolean result = updateUserInfo(userInfoDTO);
            return result;

        } catch (S3Exception | IOException e) {
            System.err.println("❌ 上传失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
