package gwl.service.Impl;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.AccountNotFoundException;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gwl.constant.MessageConstant;
import gwl.constant.StatusConstant;
import gwl.context.BaseContext;
import gwl.mapper.UserMapper;
import gwl.pojo.DTO.UserLoginDTO;
import gwl.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public gwl.entity.User userLogin(UserLoginDTO userLoginDTO) {
        String emailaddress = userLoginDTO.getEmailaddress();
        String password = userLoginDTO.getPassword();
        gwl.entity.User user = userMapper.getByUserEmail(emailaddress);
        if (user == null) {
            throw new RuntimeException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 密码比对
        if (!password.equals(user.getPassword())) {
            // 密码错误
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }
        if (user.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new RuntimeException(MessageConstant.ACCOUNT_LOCKED);
        }
        return user;
    }

    @Override
    public List<gwl.entity.User> getFriendList() {
        return userMapper.getFriendListByUserId(BaseContext.getCurrentId());
    }

}
