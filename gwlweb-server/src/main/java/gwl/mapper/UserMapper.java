package gwl.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import gwl.entity.GroupChat;
import gwl.entity.User;
import gwl.pojo.CommonPojo.Message;
import gwl.pojo.DTO.AddFriendToChatListDTO;
import gwl.pojo.DTO.CreateGroupChatDTO;
import gwl.pojo.DTO.GroupmessageDTO;
import gwl.pojo.DTO.UserInfoDTO;
import gwl.pojo.VO.GroupChatVO;
import gwl.pojo.VO.GroupMessagesVO;
import gwl.pojo.entity.ChatFriend;
import gwl.pojo.entity.ChatListId;

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
    @Select("select * from test_user  where id = #{id}")
    User getByUserId(Long id);

    @Select("""
            SELECT u.*
            FROM test_user u
            JOIN friend_relation fr ON u.id = fr.friend_id
            WHERE fr.user_id = #{id}
            """)
    List<User> getFriendListByUserId(Long id);

    /**
     * 更新用户信息
     * 
     * @param updateUserInfo
     * @return
     */
    @Update("""
            update test_user
            set userName = #{username},
                sex = #{sex},
                avatarurl = #{avatarurl}
                where id = #{id}
            """)
    public int updateUserInfo(UserInfoDTO userInfo);

    /**
     * 新建群聊
     * 
     * @param groupMembers
     */
    void createGroupChat(GroupChat groupChat);

    /**
     * 插入成员表数据
     * 
     * @param groupChat
     */
    void insertGroupChatMember(GroupChatVO groupChat);

    /**
     * 获取所有群成员id
     * 
     * @param groupId
     */
    @Select("select user_id from group_members where group_id = #{groupId}")
    List<Long> getGroupMemberIds(Long groupId);

    /**
     * 获取群信息
     * 
     * @param groupId
     * @return
     */

    GroupChat getGroupChat(Long groupId);

    /**
     * 添加朋友或群到聊天列表
     */
    @Insert("insert into chatlist (userId, friendId ,isgroup) values (#{id},#{friendId},#{isGroup})")
    void addFriendToChatList(AddFriendToChatListDTO addFriendToChatListDTO);

    /*
     * 添加群成员
     */
    @Insert("insert into group_members (group_id,user_id) values (#{groupId},#{userId})")
    Boolean addGroupMembers(Long groupId, Long userId);

    @Insert("delete from group_members where group_id = #{groupId} and user_id = #{userId}")
    Boolean removeGroupMembers(Long groupId, Long userId);

    /*
     * 更改群名
     */
    @Insert("update chatgroups set name = #{groupName} where id = #{groupId}")
    Boolean updateGroupName(Long groupId, String groupName);

    /**
     * 获取聊天列表的各个id
     * 
     * @param id
     * @return
     */
    @Select("select friend_id As id, isgroup As isGroup from chatlist where user_id = #{id}")
    List<ChatListId> getChatListIdById(Long id);

    /**
     * 获取聊天朋友
     * 
     * @param chatListId
     * @return
     */
    @Select("select id, username, avatarurl from test_user where id=#{id}")
    ChatFriend getChatFriendByChatId(Long id);

    /**
     * 获取聊天群
     * 
     * @param id
     * @return
     */

    GroupChat getChatGroupByChatId(Long id);

    /**
     * 获取群消息
     * 
     * @param id
     * @return
     */
    @Select("select id, group_id, sender_id, content, type from group_messages where group_id=#{groupId}")
    List<GroupMessagesVO> getGroupMessages(Long groupId);

    /**
     * 保存群消息
     * 
     * @param msg
     */
    @Insert("insert into group_messages(group_id, sender_id, content, type) values(#{toUser},#{fromUser},#{content},#{type})")
    void saveGroupMessage(Message message);
}
