package gwl.infrastructure.NettyHandler;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gwl.infrastructure.Manager.ChannelManager;
import gwl.mapper.ChatMessageMapper;
import gwl.mapper.UserMapper;
import gwl.pojo.entity.Message;
import gwl.pojo.entity.User;
import gwl.service.CommonService;
import gwl.service.UserService;
import gwl.util.CommonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandler;

@Component
@Slf4j
@ChannelHandler.Sharable
public class ChatHandler extends SimpleChannelInboundHandler<Message> {
    AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private CommonService commonService;

    /**
     * 发送聊天消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        long toUser = msg.getToUser();
        long fromUser = ctx.channel().attr(ChannelManager.USER_ID).get();
        String content = msg.getContent();
        String type = msg.getType();
        msg.setFromUser(fromUser);
        String sendMessage = CommonUtil.mapper.writeValueAsString(msg);
        System.out.println("sendMessage=" + sendMessage);

        if ("private".equals(type)) { // 单对单消息
            log.info("收到私聊消息: " + msg.getContent());
            // 存数据库
            chatMessageMapper.sendPrivateMessage(fromUser, toUser, content);
            chatMessageMapper.updateLastMessageTime(fromUser, toUser);
            // 对方在线的话，长连接发消息并推送
            Channel toChannel = ChannelManager.userChannelMap.get(toUser);
            if (toChannel != null && toChannel.isActive()) {
                // FCMpush
                User user = userService.getUserInfoById(fromUser);
                commonService.sendPush(toUser, user.getUsername(), content, "privatemessage", false);
                // websocket
                toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
                log.info("用户" + msg.getFromUser() + "说: " + content);
            }
        } else if ("group".equals(type)) { // 群聊消息
            log.info("收到群聊消息: " + msg.getContent());
            userService.saveGroupMessage(msg);
            List<Long> memberIds = userMapper.getGroupMemberIds(toUser);
            boolean isSaved = false;
            for (Long memberId : memberIds) {
                if (memberId != fromUser) {
                    Channel toChannel = ChannelManager.userChannelMap.get(memberId);
                    // 给群里在线用户发消息
                    if (toChannel != null && toChannel.isActive()) {
                        toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
                        log.info("用户" + msg.getFromUser() + "说: " + content);
                    }
                }
                // log.info("用户" + toUser + "不在线,发送消息到服务器后端数据库暂存");
            }
        } else { // command
            log.info("收到command " + msg.getContent());
            Channel toChannel = ChannelManager.userChannelMap.get(toUser);
            if (toChannel != null && toChannel.isActive()) {
                toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
            }
        }
    }
}
