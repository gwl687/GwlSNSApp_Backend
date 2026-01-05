package gwl.infrastructure.NettyHandler;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

import gwl.infrastructure.Manager.ChannelManager;
import gwl.pojo.entity.WebCommand;
import gwl.util.CommonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@ChannelHandler.Sharable
public class CommandHandler extends SimpleChannelInboundHandler<WebCommand> {
    AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    /**
     * 发送command命令
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebCommand msg) throws Exception {
        long toUser = msg.getToUser();
        long fromUser = ctx.channel().attr(ChannelManager.USER_ID).get();
        msg.setFromUser(fromUser);
        String sendMessage = CommonUtil.mapper.writeValueAsString(msg);
        Channel toChannel = ChannelManager.userChannelMap.get(toUser);
        if (toChannel != null && toChannel.isActive()) {
            toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
        } 
    }
}
