package gwl.components;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties.Cache.Channel;
import org.springframework.stereotype.Component;

import gwl.service.MessageService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

@Component
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final ConcurrentHashMap<String, io.netty.channel.Channel> userMap = new ConcurrentHashMap<>();

    @Autowired
    private MessageService messageService; // Spring 管理的业务逻辑 Bean

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String text = msg.text();
        String[] parts = text.split(":", 3);
        if (parts.length < 3)
            return;

        String from = parts[0];
        String to = parts[1];
        String content = parts[2];

        userMap.put(from, ctx.channel());

        // 保存聊天记录到数据库/Redis
        messageService.saveMessage(from, to, content);

        io.netty.channel.Channel toChannel = userMap.get(to);
        if (toChannel != null && toChannel.isActive()) {
            toChannel.writeAndFlush(new TextWebSocketFrame(from + " 说: " + content));
        } else {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("用户 " + to + " 不在线"));
        }
    }
}
