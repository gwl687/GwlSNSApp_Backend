package gwl.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import gwl.mapper.FriendMapper;
import gwl.service.PrivateChatService;

public class PrivateChatServiceImpl implements PrivateChatService {
    @Autowired
    FriendMapper friendMapper;

    
}
