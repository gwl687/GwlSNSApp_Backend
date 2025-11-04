package gwl.components.NettyHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gwl.context.BaseContext;
import gwl.mapper.UserMapper;
import gwl.pojo.CommonPojo.Message;
import gwl.service.MessageService;
import gwl.service.UserService;
import gwl.util.CommonUtil;
import gwl.util.JwtUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelHandler;

@Component
@Slf4j
@ChannelHandler.Sharable
public class ChatHandler extends SimpleChannelInboundHandler<Message> {
    // @Autowired
    // private MessageService messageService; // Spring 管理的业务逻辑 Bean
    AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        long toUser = msg.getToUser();
        long fromUser = ctx.channel().attr(DispatcherHandler.USER_ID).get();
        String content = msg.getContent();
        String type = msg.getType();
        msg.setFromUser(fromUser);

        String sendMessage = CommonUtil.mapper.writeValueAsString(msg);
        System.out.println("sendMessage=" + sendMessage);

        if (type == "private") { // 单对单消息
            log.info("收到消息: " + msg.getContent());
            Channel toChannel = DispatcherHandler.userMap.get(toUser);
            if (toChannel != null && toChannel.isActive()) {
                toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
                log.info("用户" + msg.getFromUser() + "说: " + content);
            } else {
                ctx.channel().writeAndFlush(new TextWebSocketFrame("用户 " + toUser + " 不在线"));
                log.info("用户" + toUser + "不在线,发送消息到服务器后端数据库暂存");
            }
        } else { // 群聊消息
            log.info("收到群聊消息: " + msg.getContent());
            List<Long> memberIds = userMapper.getGroupMemberIds(toUser);
            boolean isSaved = false;
            for (Long memberId : memberIds) {
                if (memberId != fromUser) {
                    Channel toChannel = DispatcherHandler.userMap.get(memberId);
                    // 给群里在线用户发消息
                    if (toChannel != null && toChannel.isActive()) {
                        toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
                        log.info("用户" + msg.getFromUser() + "说: " + content);
                    } else if (isSaved == false) { // 给不在线用户
                        userService.saveGroupMessage(msg);
                        isSaved = true;
                    }
                }
                // log.info("用户" + toUser + "不在线,发送消息到服务器后端数据库暂存");
            }
        }
    }
}
