package gwl.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import gwl.entity.User;

@Mapper
public interface UserMapper {
    /**
     * 用户登录
     * 
     * @param username
     * @return
     */

    @Select("select * from user  where username = #{username}")
    User getByUsername(String username);

    /**
     * 插入新用户数据
     * 
     * @param user
     */
    void insert(User user);

    /**
     * 根据邮箱获取用户信息
     * 
     * @param emailaddress
     * @return
     */
    @Select("select * from test_user where emailaddress = #{emailaddress}")
    User getByUserEmail(String emailaddress);

    /**
     * 根据id获取用户信息
     * 
     * @param id
     * @return
     */
    @Select("select * from user  where id = #{id}")
    User getByUserId(Long id);

    @Select("""
            SELECT u.*
            FROM test_user u
            JOIN friend_relation fr ON u.id = fr.friend_id
            WHERE fr.user_id = #{id}
            """)
    List<User> getFriendListByUserId(Long id);
}
