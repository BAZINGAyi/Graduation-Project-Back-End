package com.potflesh.wenda.service;

import com.potflesh.wenda.dao.CommentDAO;
import com.potflesh.wenda.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Created by bazinga on 2017/4/15.
 */
@Service
public class CommentService {
    @Autowired
    CommentDAO commentDAO;

    @Autowired
    SensitiveService sensitiveService;

    // 得到问题的评论 entityId就是问题的Id entityType就是问题的评论类型
    // 得到评论的评论 entityId就是评论的Id entityType就是评论的评论类型
    public List<Comment> getCommentsByEntity(int entityId,
                                             int entityType){
        return commentDAO.selectCommentByEntity(entityId,entityType);
    }

    public int addComment(Comment comment){
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDAO.addComment(comment);
    }

    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }

    public int getCommentInCommentCount(int entityId) {
        return commentDAO.getCommentInCommentCount(entityId);
    }

    public Comment getCommentById(int id) {
        return commentDAO.getCommentById(id);
    }

    public int getCommentCount(int entityId, int entityType){
        return commentDAO.getCommentCount(entityId,entityType);
    }

    public void deleteComment(int entityId,int entityType){
        commentDAO.updateStatus(entityId,entityType,1);
    }
}
