package gwl.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gwl.context.BaseContext;
import gwl.mapper.ChatMessageMapper;
import gwl.pojo.dto.SendPrivateMessageDTO;
import gwl.pojo.vo.PrivateMessageVO;
import gwl.service.ChatMessageService;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    @Autowired
    ChatMessageMapper chatMessageMapper;

    /**
     * 获取聊天列表里的全部私聊消息
     * @return
     */
    @Override
    public List<PrivateMessageVO> getPrivateMessages() {
        return chatMessageMapper.getPrivateMessages(BaseContext.getCurrentId());
    }

}
