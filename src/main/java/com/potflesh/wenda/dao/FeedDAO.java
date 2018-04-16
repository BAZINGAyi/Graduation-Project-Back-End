package com.potflesh.wenda.dao;
import com.potflesh.wenda.model.Feed;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by bazinga on 2017/4/26.
 */
@Mapper
public interface FeedDAO {

    String TABLE_NAME = " feed ";

    String INSERT_FIELDS = " user_id, data, created_date, type ";

    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into " ,TABLE_NAME ,  "(",

            INSERT_FIELDS, ") values(#{userId},#{data},#{createdDate},#{type})"})

    int addFeed(Feed feed);

    // 拉模式获取新鲜事 区分是否登录的用户
    // 未登录拉的所有人／登录拉的是关注的人



    /**
     *
     * @param maxId
     * @param userIds 关注的用户
     * @param count 分页
     * @return
     */
    List<Feed> selectUserFeeds(@Param("maxId") int maxId,
                               @Param("userIds") List<Integer> userIds,
                               @Param("count") int count
    );

    /**
     * 查询一个用户的所有feeds
     * @param userId
     * @return
     */
    @Select({"select ", SELECT_FIELDS," from ",TABLE_NAME," where user_id = #{userId}"})

    List<Feed> selectSingleUserFeeds(@Param("userId") int userId
    );

    /**
     * 拉模式获取新鲜事
     * @param Id
     * @return
     */
    @Select({"select ", SELECT_FIELDS," from ",TABLE_NAME," where id = #{id}"})
    Feed getFeedById(int Id);
}
