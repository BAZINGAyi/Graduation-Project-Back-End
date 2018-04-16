package com.potflesh.wenda.dao;

import com.potflesh.wenda.model.Topic;
import com.potflesh.wenda.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by bazinga on 2017/4/9.
 */
@Mapper
public interface TopicDAO {

    String TABLE_NAME = " topic ";

    String INSERT_FIELDS = " topic_name ";

    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into " ,TABLE_NAME ,  "(",

    INSERT_FIELDS, ") values(#{topic_name})"})

    int addUser(User user);

    @Select({"select ",SELECT_FIELDS," from ", TABLE_NAME,
    " where id = #{id}"})
    User selectById(int id);

    @Select({"select ",SELECT_FIELDS," from ", TABLE_NAME})
    List<Topic> select();


    @Update({"update ",TABLE_NAME," set password = #{password} where id = #{id}"})
    void updatePassword(User user);

    @Delete({"delete from ", TABLE_NAME," where id = #{id}"})
    void deleteById(int id);

    @Select({"select",SELECT_FIELDS," from", TABLE_NAME,
    " where name = #{name}"})
    User selectByname(String name);
}
