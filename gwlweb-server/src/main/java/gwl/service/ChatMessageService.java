package gwl.service;


import java.util.List;

import gwl.pojo.dto.SendPrivateMessageDTO;
import gwl.pojo.vo.PrivateMessageVO;

public interface ChatMessageService {
    /**
     * 获取聊天列表里的全部私聊消息
     * @return
     */
    List<PrivateMessageVO> getPrivateMessages();

   
}
