package com.potflesh.wenda.dao;

import com.potflesh.wenda.model.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by bazinga on 2017/4/15.
 */
@Mapper
public interface MessageDAO {

    String TABLE_NAME    = " message ";
    String INSERT_FIELDS = " from_id, to_id, content, hasRead, conversation_id, created_date ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ",TABLE_NAME , "(", INSERT_FIELDS,
            ") values (#{fromId},#{toId},#{content},#{hasRead},#{conversationId},#{createdDate})"})
    int addMessage(Message message);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where conversation_id=#{conversationId} order by created_date desc limit #{offset}, #{limit}"})
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                        @Param("offset") int offset, @Param("limit") int limit);

    @Select({"select count(id) from ", TABLE_NAME, " where hasRead=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    int getConvesationUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);


    @Update({"update ", TABLE_NAME, " set hasRead = 1 where conversation_id=#{conversationId}"})
    int setConversationReadCount(@Param("conversationId") String conversationId);

//    @Select({"select ", INSERT_FIELDS, " ,count(id) as id from ( select * from ", TABLE_NAME, " where from_id=#{userId} or to_id=#{userId} order by id desc) tt group by conversation_id  order by created_date desc limit #{offset}, #{limit}"})
//
    @Select({"select * from", TABLE_NAME, "where id in (select max(id) from (select * from ", TABLE_NAME, " where from_id = #{userId} or to_id=#{userId}  order by created_date asc ) tt group by conversation_id) limit #{offset}, #{limit}"})
    List<Message> getConversationList(@Param("userId") int userId,
                                      @Param("offset") int offset, @Param("limit") int limit);
    // 由于 message 是没有用的，所以把统计出的会话的总数赋值给 id 通过 id 反应出会话的总数。

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where from_id=#{userId} or to_id = #{userId} limit #{offset}, #{limit}"})
    List<Message> getMessageByUserId(@Param("userId") int userId,
                                     @Param("offset") int offset, @Param("limit") int limit);
}
