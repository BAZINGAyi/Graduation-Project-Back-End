package com.potflesh.wenda.dao;

import com.potflesh.wenda.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by bazinga on 2017/4/9.
 */
@Mapper
public interface UserDAO {

    String TABLE_NAME = " user ";

    String INSERT_FIELDS = " name, password, salt, head_url, mail, user_describe ";

    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into " ,TABLE_NAME ,  "(",

    INSERT_FIELDS, ") values(#{name},#{password},#{salt},#{headUrl})"})

    int addUser(User user);

    @Select({"select ",SELECT_FIELDS," from ", TABLE_NAME,
    " where id = #{id}"})
    User selectById(int id);

    @Update({"update ",TABLE_NAME," set password = #{password} where id = #{id}"})
    void updatePassword(User user);

    @Delete({"delete from ", TABLE_NAME," where id = #{id}"})
    void deleteById(int id);

    @Select({"select",SELECT_FIELDS," from", TABLE_NAME,
    " where name = #{name}"})
    User selectByname(String name);

    List<User> selectUserListByName(@Param("name") String name,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);
}
