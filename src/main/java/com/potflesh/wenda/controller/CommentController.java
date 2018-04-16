package com.potflesh.wenda.controller;

import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventProducer;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.Comment;
import com.potflesh.wenda.model.EntityType;
import com.potflesh.wenda.model.HostHolder;
import com.potflesh.wenda.service.CommentService;
import com.potflesh.wenda.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.Date;

/**
 * Created by bazinga on 2017/4/15.
 */
@Controller
public class CommentController {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    EventProducer eventProducer;

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
}