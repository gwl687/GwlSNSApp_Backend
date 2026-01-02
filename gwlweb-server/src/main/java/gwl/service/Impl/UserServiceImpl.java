package gwl.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.FirebaseMessagingException;
import gwl.constant.MessageConstant;
import gwl.constant.StatusConstant;
import gwl.context.BaseContext;
import gwl.exception.BaseException;
import gwl.mapper.UserMapper;
import gwl.pojo.dto.AddFriendToChatListDTO;
import gwl.pojo.dto.CreateGroupChatDTO;
import gwl.pojo.dto.UserInfoDTO;
import gwl.pojo.dto.UserLoginDTO;
import gwl.pojo.entity.ChatFriend;
import gwl.pojo.entity.ChatListId;
import gwl.pojo.entity.GroupChat;
import gwl.pojo.entity.Message;
import gwl.pojo.entity.User;
import gwl.pojo.vo.GroupChatVO;
import gwl.pojo.vo.GroupMessagesVO;
import gwl.pojo.vo.SearchForUserVO;
import gwl.service.CommonService;
import gwl.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedis;

    @Autowired
    private CommonService commonService;

    @Autowired
    private StringRedisTemplate redis;

    @Override
    public User userLogin(UserLoginDTO userLoginDTO) {
        String emailaddress = userLoginDTO.getEmailaddress();
        String password = userLoginDTO.getPassword();
        User user = userMapper.getByUserEmail(emailaddress);
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 密码比对
        if (!password.equals(user.getPassword())) {
            // 密码错误
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }
        // device token
        stringRedis.opsForValue().set("push_token:" + user.getId(), userLoginDTO.getPushToken());
        return user;
    }

    /**
     * 获取用户信息
     * 
     * @return
     */
    public User getUserInfo() {
        return userMapper.getByUserId(BaseContext.getCurrentId());
    }

     /**
     * 根据id获取用户信息
     * 
     * @return
     */
    @Override
    public User getUserInfoById(Long userId) {
         return userMapper.getByUserId(userId);
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
     * 添加朋友或群到聊天列表
     */
    @Override
    public void addFriendToChatList(AddFriendToChatListDTO addFriendToChatListDTO) {
        userMapper.addFriendToChatList(addFriendToChatListDTO);
    }

    /**
     * 获取聊天列表
     */
    @Override
    public List<?> getChatList() {
        // 获取好友的，和群的，然后放入同一个list返回
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
                        .avatarUrl("https://i.pravatar.cc/150?img=3")
                        // .avatarUrl(groupChat.getAvatarUrl())
                        .build();
                result.add(groupChatVO);
            } else { // 是好友
                ChatFriend chatFriend = userMapper.getChatFriendByChatId(chatListId.getId());
                // 先传默认头像,测试用
                // chatFriend.setAvatarurl("https://i.pravatar.cc/150?img=3");
                result.add(chatFriend);
            }
        }
        return result;
    }

    /**
     * 创建群聊
     * 
     * @throws IOException
     * @throws FirebaseMessagingException
     */
    @Override
    public GroupChatVO createGroupChat(CreateGroupChatDTO createGroupChatDTO) {
        List<Long> groupChatMembers = createGroupChatDTO.getSelectedFriends();
        List<String> groupNameList = new ArrayList<>();
        gwl.pojo.entity.User owner = userMapper.getByUserId(BaseContext.getCurrentId());
        groupNameList.add(owner.getUsername());
        for (Long memberId : groupChatMembers) {
            gwl.pojo.entity.User groupMember = userMapper.getByUserId(memberId);
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
        // 添加每个群成员的聊天列表
        for (Long userId : groupChatMembers) {
            AddFriendToChatListDTO addFriendToChatListDTO = AddFriendToChatListDTO.builder()
                    .userId(userId)
                    .friendId(groupchat.getGroupId())
                    .isGroup(true).build();
            userMapper.addFriendToChatList(addFriendToChatListDTO);
        }

        // android推送
        for (Long id : groupChatMembers) {
            commonService.sendPush(id, "chatgroup invite",
                    owner.getUsername() + "invite you to join a chat group", "joingroup", false);
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

    /**
     * 发送群消息
     */
    @Override
    public void saveGroupMessage(Message message) {
        userMapper.saveGroupMessage(message);
    }

    /**
     * 根据关键词查找用户
     * 
     * @param keyword
     * @return
     */
    @Override
    public List<SearchForUserVO> searchForUsers(String keyword) {
        return userMapper.searchForUsers(keyword, BaseContext.getCurrentId());
    }

}
