package gwl.components.NettyHandlers;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gwl.pojo.CommonPojo.Message;
import gwl.pojo.CommonPojo.WebCommand;
import gwl.util.CommonUtil;
import gwl.util.JwtUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ChannelHandler.Sharable
public class DispatcherHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    public static final ConcurrentHashMap<Long, Channel> userMap = new ConcurrentHashMap<>();
    static final AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete handshake) {
            // 从握手请求里拿 Header
            HttpHeaders headers = handshake.requestHeaders();
            String authHeader = headers.get("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.close();
                return;
            }

            String token = authHeader.substring("Bearer ".length());
            long userId = JwtUtil.parseToken(token); // 你的 JWT 解析工具

            // 绑定 userId
            ctx.channel().attr(USER_ID).set(userId);
            log.info("保存userId: " + userId);
            userMap.put(userId, ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        JsonNode jsonData = CommonUtil.mapper.readTree(text);
        String type = jsonData.get("type").asText();
        log.info("消息类型为" + type);
        switch (type) {
            case "command":
                ctx.fireChannelRead(new WebCommand(jsonData)); // 传给 ChatHandler
                break;
            default:
                ctx.fireChannelRead(new Message(jsonData));
        }
    }
}
