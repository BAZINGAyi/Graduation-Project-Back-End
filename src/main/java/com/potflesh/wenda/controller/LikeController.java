package com.potflesh.wenda.controller;

import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventProducer;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.Comment;
import com.potflesh.wenda.model.EntityType;
import com.potflesh.wenda.model.HostHolder;
import com.potflesh.wenda.service.CommentService;
import com.potflesh.wenda.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.potflesh.wenda.utils.WendaUtil;

/**
 * Created by bazinga on 2017/4/16.
 */
@Controller

public class LikeController {

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    CommentService commentService;

    @RequestMapping(path = {"/like"}, method = {RequestMethod.POST})
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        Comment comment = commentService.getCommentById(commentId);

        EventModel eventModel = new EventModel(EventType.LIKE);
        eventModel.setActorId(hostHolder.getUsers().getId())
                .setEntityOwnerId(comment.getUserId())
                .setEntityId(commentId)
                .setEntityType(EntityType.ENTITY_COMMENT)
                .setExt("questionId",String.valueOf(comment.getEntityId()));

        eventProducer.fireEvent(eventModel);

        long likeCount = likeService.like(hostHolder.getUsers().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJsonString(0, String.valueOf(likeCount));
    }

    @RequestMapping(path = {"/dislike"}, method = {RequestMethod.POST})
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        long likeCount = likeService.disLike(hostHolder.getUsers().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJsonString(0, String.valueOf(likeCount));
    }

}
