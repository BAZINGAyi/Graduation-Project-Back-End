package com.potflesh.wenda.controller;

import com.alibaba.fastjson.JSONObject;
import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.*;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/13.
 */
@Controller
public class QuestionController {

    private static final Logger logger= LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;


    @Autowired
    FollowService followService;

    public static String getJsonString(int code){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        return jsonObject.toJSONString();
    }

    @RequestMapping(value = "/question/add",method={RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content")String content){

        try{

            Question question = new Question();

            question.setTitle(title);
            question.setContent(content);
            question.setCreatedDate(new Date());
            if(hostHolder.getUsers() != null)
            question.setUserId(hostHolder.getUsers().getId());
            else{
                //999 返回到登录页面
                return WendaUtil.getJsonString(999);
               // question.setUserId(WendaUtil.Anonymous_USERID);
            }

            if(questionService.addQuestion(question) > 0){
                // 成功返回 0
                return WendaUtil.getJsonString(0);
            }

        }catch (Exception e){
            logger.error("增加题目失败" + e.getMessage());
        }
        // 失败返回 1
        return WendaUtil.getJsonString(1);
    }

    @RequestMapping(value = "question/{qid}")
    public String questionDetail(Model model ,
                                 @PathVariable("qid")int qid){
        Question question = questionService.selectById(qid);
        model.addAttribute("question",question);
        model.addAttribute("user",userService.getUser(question.getUserId()));
        List<Comment> commentList = commentService.getCommentsByEntity(question.getId(), EntityType.ENTITY_QUESTION);
        List<ViewObject> comments = new ArrayList<>();
        for (Comment comment:commentList){
            ViewObject vo = new ViewObject();
            vo.set("comment",comment);
            if (hostHolder.getUsers() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUsers().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));
            vo.set("user",userService.getUser(comment.getUserId()));
            comments.add(vo);
        }
        model.addAttribute("comments",comments);

        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (hostHolder.getUsers() != null) {
            model.addAttribute("followed", followService.isFollower(hostHolder.getUsers().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }
        return "detail";
    }

    @RequestMapping(value = "api/question")
    @ResponseBody
    public String getQuestionDetail(Model model ,
                                 @RequestParam("qid")int qid){

        // 定义一个 Json 序列化的对象，用于返回 question 页面需要的所有数据
        Map<String, Object> questionJson = new HashedMap();

        // 获取问题的详细信息
        Question question = questionService.selectById(qid);
        questionJson.put("question", question);

        // 获取问题点赞的数量
        questionJson.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));

        // 获取提问题用户的信息
        questionJson.put("user", userService.getUser(question.getUserId()));

        // 获取对问题的评论,注意是对问题的评论数据, type = 1
        List<Comment> questionCommentList = commentService.getCommentsByEntity(question.getId(), EntityType.ENTITY_QUESTION);

        // 定义一个用于保存对每个问题评论的结构信息，包含该发布该评论的用户信息，该条评论信息，该条评论评论的信息，该条评论点赞的数量，登录的用户是否对该问题进行点赞
        List<Map<String, Object>> singleComments = new ArrayList<>();
        for (Comment comment: questionCommentList){

            // 获取针对每个评论的评论，放到每条评论里
            Map<String, Object> commentMap = new HashedMap();
            List<Comment> commentInCommentList = commentService.getCommentsByEntity(comment.getId(), EntityType.ENTITY_COMMENT);
            List<Map<String, Object>> commentSonListMap = new ArrayList<>();
            for (int i = 0 ; i < commentInCommentList.size(); i++) {
                Map<String, Object> commentSon = new HashedMap();
                commentSon.put("comment", commentInCommentList.get(i));
                // 为每个评论添加相应的用户信息
                commentSon.put("user", userService.getUser(commentInCommentList.get(i).getUserId()));
                commentSonListMap.add(commentSon);
            }
            commentMap.put("commentSon", commentSonListMap);

            int commentInCommentCount = commentService.getCommentInCommentCount(comment.getId());
            // 该条问题的一个评论
            commentMap.put("commentParent",comment);
            commentMap.put("commentInCommentCount", commentInCommentCount);

            // 定义一个包含该条评论的有关的所有需要的信息
            Map<String, Object> commentNew = new HashedMap();
            commentNew.put("comment", commentMap);

            // 用户对该问题点赞
            if (hostHolder.getUsers() == null) {
                commentNew.put("liked", 0);
            } else {
                commentNew.put("liked", likeService.getLikeStatus(hostHolder.getUsers().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            // 该评论点赞的数量
            commentNew.put("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));

            // 该条评论是由哪个用户评论的
            commentNew.put("user",userService.getUser(comment.getUserId()));
            singleComments.add(commentNew);
        }
        questionJson.put("comments", singleComments);

        // 获取查看的用户是否已经关注问题
        if (hostHolder.getUsers() != null) {
            questionJson.put("followed", followService.isFollower(hostHolder.getUsers().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            questionJson.put("followed", false);
        }

       return WendaUtil.getJSONString(1,questionJson);
    }

    @RequestMapping(value = "api/getTopicQuestion")
    @ResponseBody
    String getTopicQuestionList(@RequestParam("tId")int tId,
                                @RequestParam("offset")int offset) {

        // 存放每个 question 和 对应 question 的话题类型
        List<Map<String, Object>> topicQuestionListMap = new ArrayList<>();
        List<Question> topicQuestionList = new ArrayList<>();

        topicQuestionList = questionService.getLastTopicQuestionList(tId, offset);
        return "";
    }
}