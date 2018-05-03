package com.potflesh.wenda.controller;

import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventProducer;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.*;
import com.potflesh.wenda.utils.RedisKeyUtil;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.potflesh.wenda.utils.WendaUtil;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/15.
 */
@Controller
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    FeedService feedService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    RedisService redisService;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    private static final Logger logger= LoggerFactory.getLogger(CommentController.class);


    @RequestMapping(path = {"/addComment"},method ={RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content){

        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCreatedDate(new Date());
            if (hostHolder != null) {
                comment.setUserId(hostHolder.getUsers().getId());
            } else {
                comment.setUserId(WendaUtil.Anonymous_USERID);
            }
            comment.setEntityId(questionId);
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            commentService.addComment(comment);
            // 这块应该使用数据库的事务操作
            int count = commentService.getCommentCount(comment.getEntityId(),comment.getEntityType());
            questionService.updateCommentCount(questionId,count);

            // 给发表该评论的用户的粉丝发送新鲜事
            eventProducer.fireEvent(new EventModel(EventType.COMMENT_MyFans)
                    .setActorId(comment.getUserId())
                    .setEntityId(questionId));

            // 给关注该问题的用户发表评论
            eventProducer.fireEvent(new EventModel(EventType.COMMENT_Focus_Question)
                    .setActorId(comment.getUserId())
                    .setEntityId(questionId));

        }catch (Exception e){
            logger.error("增加评论失败" + e.getMessage());
        }

        return "redirect:/question/" + questionId;
    }

    /**
     * 对评论内容进行评论
     * @param commentId
     * @param content
     * @return
     */
    @RequestMapping(path = {"/api/addCommentToComment"},method ={RequestMethod.POST})
    public String addCommentToComment(@RequestParam("commentId") int commentId,
                                      @RequestParam("content") String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCreatedDate(new Date());

            if (hostHolder != null) {
                comment.setUserId(hostHolder.getUsers().getId());
            } else {
                comment.setUserId(WendaUtil.Anonymous_USERID);
            }

            comment.setEntityId(commentId);
            comment.setEntityType(EntityType.ENTITY_COMMENT);
            commentService.addComment(comment);

        }catch (Exception e){
            logger.error("增加评论失败" + e.getMessage());
        }

        return "";
    }

    /**
     * 得到当前登陆用户评论的问题列表
     */
    @RequestMapping(path = {"/api/queryUserCommentQuestionList"},method ={RequestMethod.GET})
    @ResponseBody
    String getUserCommentQuestion(@RequestParam("offset") int offset){
        int localUserId = hostHolder.getUsers() == null ? 0 : hostHolder.getUsers().getId();
        System.out.println(localUserId + "用户已经登录");
        // 首先取出所有用户的评论
        List<Comment> comments = new ArrayList<>();
        if (localUserId != 0) {
            comments = commentService.getCommentsByUserid(localUserId);
        }
        // 在这些评论中筛选出对问题的评论
        List<Integer> questionIdList = new ArrayList<>();
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getEntityType() == EntityType.ENTITY_QUESTION) {
                questionIdList.add(comments.get(i).getEntityId());
            }
        }
        // 取出这些问题
        List<Map<String,Object>> vos = new ArrayList< Map<String,Object>>();
        if (questionIdList.size() != 0) {
            List<Question> questionList = questionService.getQuestionsByUserIdList(questionIdList, offset);
            for (Question question : questionList){
                Map vo = new HashedMap();
                vo.put("question", question);
                vo.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
                vo.put("user", userService.getUser(question.getUserId()));
                vos.add(vo);
            }
        }
        return WendaUtil.getJSONString(200, vos);
    }
}
