package gwl.service.group;

import gwl.pojo.DTO.CreateGroupChatDTO;

public interface GroupService {
    /*
     * 添加群成员
     */
    Boolean addGroupMembers(Long groupId, CreateGroupChatDTO createGroupChatDTO);
    /*
     * 移除群成员
     */
    Boolean removeGroupMembers(Long groupId, CreateGroupChatDTO createGroupChatDTO);
    /*
     * 创建视频聊天token
     */
    String createLivekitToken(String roomName);
}