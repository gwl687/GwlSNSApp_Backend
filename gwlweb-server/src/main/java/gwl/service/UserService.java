package gwl.service;

import java.util.List;

import gwl.entity.GroupChat;
import gwl.entity.User;
import gwl.pojo.CommonPojo.Message;
import gwl.pojo.DTO.AddFriendToChatListDTO;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.pojo.DTO.GetGroupChatDTO;
import gwl.pojo.DTO.GroupmessageDTO;
import gwl.pojo.DTO.UpdateUserInfoDTO;
import gwl.pojo.DTO.UserLoginDTO;
import gwl.pojo.VO.GroupChatVO;
import gwl.pojo.VO.GroupMessagesVO;

public interface UserService {
  /**
   * 用户登录
   * 
   * @param userLoginDTO
   * @return
   */
  User userLogin(UserLoginDTO userLoginDTO);

  /**
   * 更新用户信息
   * @return
   */
  Boolean updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO);

  /**
   * 获取好友列表
   * @return
   */
  List<User> getFriendList();

  /**
   * 添加朋友或群到聊天列表
   */
  void addFriendToChatList(AddFriendToChatListDTO addFriendToChatListDTO);

  /**
   * 获取聊天列表
   * @return
   */
  List<?> getChatList(); 

  /**
   * 创建群聊
   * @param createGroupChatDTO
   * @return
   */
  GroupChatVO createGroupChat(CreateGroupChatDTO createGroupChatDTO);
  /**
   * 获取群信息
   * @param groupId
   * @return
   */
  GroupChat getGroupChat(Long GroupId);
  
  /**
   * 获取群消息
   */
  List<GroupMessagesVO> getGroupMessages(Long groupId);

  /**
   * 存储群消息
   */
  void saveGroupMessage(Message msg);
}
