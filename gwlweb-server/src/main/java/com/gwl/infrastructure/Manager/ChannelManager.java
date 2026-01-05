package com.gwl.infrastructure.Manager;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.gwl.pojo.entity.WebCommand;
import com.gwl.util.CommonUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChannelManager {
    public final static ConcurrentMap<Long, Channel> userChannelMap = new ConcurrentHashMap<>();
    public final static AttributeKey<Long> USER_ID = AttributeKey.valueOf("userId");

    public void add(Long userId, Channel channel) {
        userChannelMap.put(userId, channel);
    }

    public Channel get(Long userId) {
        return userChannelMap.get(userId);
    }

    public void remove(Long userId) {
        userChannelMap.remove(userId);
    }

    public void sendCommand(Long userId, List<Long> ids, String command) {
        try {
            for (Long id : ids) {
                Channel toChannel = userChannelMap.get(id);
                WebCommand webCommand = WebCommand.builder()
                        .fromUser(userId)
                        .toUser(id)
                        .type(command)
                        .build();
                String sendCommand;
                sendCommand = CommonUtil.mapper.writeValueAsString(webCommand);
                if (toChannel != null && toChannel.isActive()) {
                    toChannel.writeAndFlush(new TextWebSocketFrame(sendCommand));
                    log.info(command);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
