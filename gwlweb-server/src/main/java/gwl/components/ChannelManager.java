package gwl.components;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
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
}
