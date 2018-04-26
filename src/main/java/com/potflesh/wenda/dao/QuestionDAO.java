package com.potflesh.wenda.dao;
import com.potflesh.wenda.model.Question;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by bazinga on 2017/4/9.
 */
@Mapper
public interface QuestionDAO {
    String TABLE_NAME = " question ";

    String INSERT_FIELDS = " title, content, created_date, user_id, comment_count, topic_id ";

    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into " ,TABLE_NAME ,  "(",

            INSERT_FIELDS, ") values(#{title},#{content},#{createdDate},#{userId},#{commentCount},#{topicId})"})

    int addQuestion(Question question);


     List<Question> selectLatestQuestions(@Param("userId") int userId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    @Select({"select ",SELECT_FIELDS," from ",TABLE_NAME," where id = #{id}"})
    Question selectById(int qid);

    @Update({"update ", TABLE_NAME, " set comment_count = #{commentCount} where id=#{id}"})
    int updateCommentCount(@Param("id") int id, @Param("commentCount") int commentCount);

    List<Question> selectLastTopicQuestionListByTid(@Param("topicId") int topicId,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit);

    List<Question> selectLastQuestionQuestionListByTitle(@Param("title") String title,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);

    List<Question> getQuestionsByUserIdList(List<Integer> questionIdList, int offset, int limit);
}
