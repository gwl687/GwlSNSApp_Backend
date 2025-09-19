package gwl.service;

import java.util.List;

import gwl.entity.User;
import gwl.pojo.DTO.UserLoginDTO;

public interface UserService {
  /**
   * 用户登录
   * 
   * @param userLoginDTO
   * @return
   */
  User userLogin(UserLoginDTO userLoginDTO);

  /**
   * 获取好友列表
   * @return
   */
  List<User> getFriendList();
}
