package com.potflesh.wenda.dao;

import com.potflesh.wenda.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by bazinga on 2017/4/9.
 */
@Mapper
public interface CommentDAO {
    String TABLE_NAME = " comment ";

    String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status, markdown_content ";

    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into " ,TABLE_NAME ,  "(",

            INSERT_FIELDS, ") values(#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status},#{markdownContent})"})

    int addComment(Comment comment);

    @Update({"update ", TABLE_NAME, " set markdown_content = #{markdownContent}, content = #{content} where id=#{id}"})
    int updateComment(Comment comment);

    @Update({"update ", TABLE_NAME, " set status = 0 where id=#{commentId}"})
    int deleteComment(int commentId);


    @Select({"select ",SELECT_FIELDS," from ",TABLE_NAME," where entity_id = #{entityId} and entity_type " +
            " = #{entityType} and status = 1 order by created_date desc "})
    List<Comment> selectCommentByEntity(@Param("entityId") int entityId,
                                        @Param("entityType") int entityType);


    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Comment getCommentById(int id);


    @Update({"update ", TABLE_NAME, " set status=#{status} where entity_id=#{entityId} and entity_type=#{entityType}"})
    void updateStatus(@Param("entityId") int entityId, @Param("entityType") int entityType, @Param("status") int status);


    @Select({"select count(id) from ", TABLE_NAME, " where entity_id=#{entityId} and entity_type=#{entityType} and status = 1 "})
    int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);


    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId} and status = 1"})
    int getUserCommentCount(int userId);

    @Select({"select count(id) from", TABLE_NAME, " where entity_id =#{entityId} and entity_type=#{entityType} and status = 1 "})
    int getCommentInCommentCount(@Param("entityId")int entityId, @Param("entityType") int entityType);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where user_id=#{userId}"})
    List<Comment> getCommentsByUserId(int userId);
}
