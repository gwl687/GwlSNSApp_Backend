package gwl.service.Impl.group;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gwl.components.ChannelManager;
import gwl.components.NettyHandlers.DispatcherHandler;
import gwl.context.BaseContext;
import gwl.entity.GroupChat;
import gwl.mapper.UserMapper;
import gwl.pojo.CommonPojo.WebCommand;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.service.group.GroupService;
import gwl.util.CommonUtil;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.VideoGrant;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupServiceImpl implements GroupService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    ChannelManager channelManager;

    private static final String API_KEY = "devkey";
    private static final String API_SECRET = "secret";
    private static final String LIVEKIT_URL = "ws://3.112.54.245:7880";

    /*
     * 添加群成员
     */
    @Override
    public Boolean addGroupMembers(Long groupId, CreateGroupChatDTO createGroupChatDTO) {
        GroupChat groupChat = userMapper.getGroupChat(groupId);
        String groupName = groupChat.getGroupName();
        try {
            for (Long userId : createGroupChatDTO.getSelectedFriends()) {
                userMapper.addGroupMembers(groupId, userId);
                String friendName = userMapper.getByUserId(userId).getUsername();
                groupName += ',' + friendName;
            }
            userMapper.updateGroupName(groupId, groupName);
            String memberIdsString = groupChat.getMemberIds();
            List<Long> memberIds = Arrays.stream(memberIdsString.split(",")).map(Long::parseLong).toList();
            // 发送更新群名的消息
            channelManager.sendCommand(BaseContext.getCurrentId(), memberIds, "groupMessageChange");
            log.info("发送群信息更改的消息(添加群成员)");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     * 移除群成员
     */
    @Override
    public Boolean removeGroupMembers(Long groupId, CreateGroupChatDTO createGroupChatDTO) {
        String groupName = "";
        try {
            for (Long userId : createGroupChatDTO.getSelectedFriends()) {
                userMapper.removeGroupMembers(groupId, userId);
            }
            GroupChat groupChat = userMapper.getGroupChat(groupId);
            String memberIdsString = groupChat.getMemberIds();
            List<Long> memberIds = Arrays.stream(memberIdsString
                    .split(","))
                    .map(Long::parseLong)
                    .toList();
            for (Long id : memberIds) {
                if (id == groupChat.getOwnerId()) {
                    groupName += userMapper.getByUserId(id).getUsername();
                } else {
                    groupName += "," + userMapper.getByUserId(id).getUsername();
                }
            }
            userMapper.updateGroupName(groupId, groupName);
            // 发送更新群名的消息
            channelManager.sendCommand(BaseContext.getCurrentId(),memberIds, "groupMessageChange");
            log.info("发送群信息更改的消息(移除群成员)");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * 创建视频聊天token
     */
    @Override
    public String createLivekitToken(String roomName) {
        AccessToken token = new AccessToken(API_KEY, API_SECRET);
        token.setIdentity(BaseContext.getCurrentId().toString());
        token.addGrants(new RoomJoin(true),new RoomName(roomName));
        return token.toJwt();
    }
}
