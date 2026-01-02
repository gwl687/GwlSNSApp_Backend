package gwl.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import gwl.pojo.entity.User;

@Mapper
public interface FriendMapper {
    /**
     * 获取好友列表
     * 
     * @param id
     * @return
     */
    @Select("""
            SELECT u.*
            FROM test_user u
            JOIN friend_relation fr ON u.id = fr.friend_id
            WHERE fr.user_id = #{id}
            """)
    List<User> getFriendListByUserId(Long id);

    /**
     * 获取正在申请成为我好友的用户
     * 
     * @param myId
     * @return
     */
    @Select("select tu.* from test_user tu join friend_relation fr on tu.id = fr.user_id where fr.status = 2 and fr.friend_id = #{myId}")
    List<User> getRequestFriends(Long myId);

    /**
     * 回复好友申请
     * 
     * @param myId
     * @param friendId
     * @param res
     */
    void friendRequestResponse(Long myId, Long friendId, Integer res);

    /**
     * 添加朋友到聊天列表
     * 
     * @param friendId
     */
    @Insert("""
            insert ignore into chatlist(user_id, friend_id, isgroup)
            values
            (#{myId}, #{friendId}, 0),
            (#{friendId}, #{myId}, 0)
            """)
    void addToChatList(Long myId, Long friendId);
}
