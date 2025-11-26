package gwl.components;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import gwl.components.NettyHandlers.DispatcherHandler;
import gwl.context.BaseContext;
import gwl.pojo.CommonPojo.WebCommand;
import gwl.util.CommonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.apachecommons.CommonsLog;

@Component
public class ChannelManager {
    private final ConcurrentMap<Long, Channel> userChannelMap = new ConcurrentHashMap<>();

    public void add(Long userId, Channel channel) {
        userChannelMap.put(userId, channel);
    }

    public Channel get(Long userId) {
        return userChannelMap.get(userId);
    }

    public void remove(Long userId) {
        userChannelMap.remove(userId);
    }

    public void sendCommand(List<Long> ids, String command) {
        try {
            for (Long id : ids) {
                Channel toChannel = DispatcherHandler.userMap.get(id);
                WebCommand webCommand = WebCommand.builder()
                        .fromUser(BaseContext.getCurrentId())
                        .toUser(id)
                        .type(command)
                        .build();
                String sendMessage;
                sendMessage = CommonUtil.mapper.writeValueAsString(webCommand);
                if (toChannel != null && toChannel.isActive()) {
                    toChannel.writeAndFlush(new TextWebSocketFrame(sendMessage));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}
