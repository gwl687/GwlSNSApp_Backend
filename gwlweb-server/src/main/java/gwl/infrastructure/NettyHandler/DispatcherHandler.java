package gwl.infrastructure.NettyHandler;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import gwl.infrastructure.Manager.ChannelManager;
import gwl.pojo.entity.Message;
import gwl.util.CommonUtil;
import gwl.util.JwtUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ChannelHandler.Sharable
public class DispatcherHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

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
            ctx.channel().attr(ChannelManager.USER_ID).set(userId);
            log.info("保存userId: " + userId);
            ChannelManager.userChannelMap.put(userId, ctx.channel());
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws JsonMappingException, JsonProcessingException {
        String text = msg.text();
        JsonNode jsonData = CommonUtil.mapper.readTree(text);
        log.info("收到消息" + msg);
        ctx.fireChannelRead(new Message(jsonData)); // send to ChatHandler
    }
}
