package gwl.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.google.firebase.messaging.FirebaseMessagingException;

import gwl.entity.GroupChat;
import gwl.entity.User;
import gwl.pojo.CommonPojo.Message;
import gwl.pojo.DTO.AddFriendToChatListDTO;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.pojo.DTO.GetGroupChatDTO;
import gwl.pojo.DTO.GroupmessageDTO;
import gwl.pojo.DTO.UserInfoDTO;
import gwl.pojo.DTO.UserLoginDTO;
import gwl.pojo.VO.GroupChatVO;
import gwl.pojo.VO.GroupMessagesVO;
import gwl.pojo.VO.UserInfoVO;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

public interface UserService {
  /**
   * 用户登录
   * 
   * @param userLoginDTO
   * @return
   */
  User userLogin(UserLoginDTO userLoginDTO);

  /*
   * 获取用户信息
   */
  User getUserInfo();

  /**
   * 更新用户信息
   * @return
   */
  Boolean updateUserInfo(UserInfoDTO userInfoDTO);

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
