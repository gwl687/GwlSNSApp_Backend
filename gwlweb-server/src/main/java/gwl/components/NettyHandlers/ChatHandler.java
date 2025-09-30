package gwl.components.NettyHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gwl.pojo.CommonPojo.Message;
import gwl.service.MessageService;
import gwl.util.CommonUtil;
import gwl.util.JwtUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<Message> {
    // @Autowired
    // private MessageService messageService; // Spring 管理的业务逻辑 Bean
    AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        // String text = msg.text();
        // 解析成 Map
        // Map<String, Object> data = mapper.readValue(text, Map.class);
        // String type = msg.get("type");
        long toUser = msg.getToUser();
        long fromUser = ctx.channel().attr(DispatcherHandler.USER_ID).get();
        String content = msg.getContent();
        msg.setFromUser(fromUser);

        // 保存聊天记录到数据库/Redis

        String sendMessage = CommonUtil.mapper.writeValueAsString(msg);
        Channel toChannel = DispatcherHandler.userMap.get(toUser);
        if (toChannel != null && toChannel.isActive()) {
            toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
            log.info("用户" + msg.getFromUser() + "说: " + content);
        } else {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("用户 " + toUser + " 不在线"));
            log.info("用户" + toUser + "不在线");
        }
    }
}
