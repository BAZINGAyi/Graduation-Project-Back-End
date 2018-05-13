package com.potflesh.wenda.controller;

import com.alibaba.fastjson.JSONObject;
import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.*;
import com.potflesh.wenda.utils.HttpStatusCode;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.*;

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

    ////////////////////////////////////////////////////////////////////////////////////////// API INTERFACE ////////////////////////////////////////////////////////////////

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

        // 定义一个用于保存对每个问题评论的结构信息，包含该发布该评论的用户信息，该条评论信息，该条评论评论的信息，该条评论点赞的数量，登录的用户是否对该问题进行点赞q
        List<Map<String, Object>> singleComments = new ArrayList<>();
        for (Comment commentParent: questionCommentList){

            // 获取针对每个评论的评论，放到每条评论里
            Map<String, Object> commentMap = new HashedMap();
            List<Comment> commentSonList = commentService.getCommentsByEntity(commentParent.getId(), EntityType.ENTITY_COMMENT);
            List<Map<String, Object>> commentSonListMap = new ArrayList<>();
            for (Comment commentSon: commentSonList) {
                Map<String, Object> commentSonMap = new HashedMap();
                commentSonMap.put("comment", commentSon);

                // 为每个评论添加相应的用户信息
                commentSonMap.put("user", userService.getUser(commentSon.getUserId()));

                // 标识这条评论是否是由登录用户评论的
                if (hostHolder.getUsers() == null) {
                    commentSonMap.put("sonCommentIsUser", false);
                } else {
                    if (commentSon.getUserId() == hostHolder.getUsers().getId()) {
                        commentSonMap.put("sonCommentIsUser", true);
                    } else {
                        commentSonMap.put("sonCommentIsUser", false);
                    }
                }

                commentSonListMap.add(commentSonMap);
            }
            commentMap.put("commentSon", commentSonListMap);

            int commentInCommentCount = commentService.getCommentInCommentCount(commentParent.getId());
            // 该条问题的一个评论
            commentMap.put("commentParent", commentParent);
            commentMap.put("commentInCommentCount", commentInCommentCount);

            // 定义一个包含该条评论的有关的所有需要的信息
            Map<String, Object> commentNew = new HashedMap();
            commentNew.put("comment", commentMap);

            // 用户是否对该评论点赞或点踩
            if (hostHolder.getUsers() == null) {
                commentNew.put("liked", 0);
            } else {
                commentNew.put("liked", likeService.getLikeStatus(hostHolder.getUsers().getId(), EntityType.ENTITY_COMMENT, commentParent.getId()));
            }

            // 标识这条评论是否是由登录用户评论的
            if (hostHolder.getUsers() == null) {
                commentNew.put("parentCommentIsUser", false);
            } else {
                if (commentParent.getUserId() == hostHolder.getUsers().getId()) {
                    commentNew.put("parentCommentIsUser", true);
                } else {
                    commentNew.put("parentCommentIsUser", false);
                }
            }

            // 该评论点赞的数量
            commentNew.put("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, commentParent.getId()));
            commentNew.put("dislikeCount", likeService.getDisLikeCount(EntityType.ENTITY_COMMENT, commentParent.getId()));

            // 该条评论是由哪个用户评论的
            commentNew.put("user",userService.getUser(commentParent.getUserId()));
            singleComments.add(commentNew);
        }
        questionJson.put("comments", singleComments);

        // 获取查看的用户是否已经关注问题
        if (hostHolder.getUsers() != null) {
            questionJson.put("followed", followService.isFollower(hostHolder.getUsers().getId(), EntityType.ENTITY_QUESTION, qid));
            return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, questionJson);
        } else {
            questionJson.put("followed", false);
            return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, questionJson);
        }

    }

    @RequestMapping(value = "api/getTopicQuestion")
    @ResponseBody
    String getTopicQuestionList(@RequestParam("tId")int tId,
                                @RequestParam("offset")int offset) {

        // 存放每个 question 和 对应 question 的话题类型
        List<Question> questionTopicList = questionService.getLastTopicQuestionList(tId, offset);
        List< Map<String, Object> > vos = new ArrayList< Map<String,Object>>();
        for (Question question : questionTopicList){
            Map questionMap = new HashedMap();
            questionMap.put("question", question);
            questionMap.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            questionMap.put("user", userService.getUser(question.getUserId()));
            vos.add(questionMap);
        }

        return WendaUtil.getJSONString(1, vos);
    }

    /**
     * 得到当前登陆用户发布的问题列表
     */
    @RequestMapping(path = {"/api/LoginUserQuestionList"},method ={RequestMethod.GET})
    @ResponseBody
    String getLoginUserQuestionList(@RequestParam("offset") int offset){
        int localUserId = hostHolder.getUsers() == null ? 0 : hostHolder.getUsers().getId();
        List< Map<String,Object> > vos = new ArrayList< Map<String,Object>>();
        // 用户已经登录
        if (localUserId != 0) {
            List<Question> questionList = new ArrayList<>();
            questionList = questionService.getLatestQuestions(localUserId, offset, 10);
            for (Question question : questionList){
                Map vo = new HashedMap();

                vo.put("question", question);
                vo.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
                vo.put("user", userService.getUser(question.getUserId()));

                vos.add(vo);
            }

            Map<String, Object> questionMap = new HashedMap();
            questionMap.put("questionList", vos);
            questionMap.put("msg", "请求成功");

            if (questionList.size() == 0) {
                return WendaUtil.getJSONString(HttpStatusCode.NO_CONTENT, questionMap);
            }

            return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, questionMap);
        }
        return WendaUtil.getJsonString(HttpStatusCode.Unauthorized,"您未登录");
    }

    /**
     * 添加问题
     */
    @RequestMapping(value = "api/question/add",method={RequestMethod.POST})
    @ResponseBody
    public String createQuestion(@RequestBody Map<String, Object> reqMap){
        try{

            String title = reqMap.get("title").toString();
            String content = reqMap.get("content").toString();
            String markdownContent = reqMap.get("markdownContent").toString();
            String topicId = reqMap.get("topicId").toString();

            Question question = new Question();
            question.setTitle(title);
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setMarkdownContent(markdownContent);
            question.setTopicId(Integer.valueOf(topicId));
            System.out.println("markdown:" + markdownContent);

            Map<String,Object> map = new HashMap<>();
            if(hostHolder.getUsers() != null)
                question.setUserId(hostHolder.getUsers().getId());
            else{
                map.put("msg","请登录后再发表问题");
                map.put("status","fail");
                //999 返回到登录页面
                return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, map);
                // question.setUserId(WendaUtil.Anonymous_USERID);
            }

            if(questionService.addQuestion(question) > 0){
                // 成功返回 0
                map.put("msg","提问成功");
                map.put("status","success");
                return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, map);
            }

        }catch (Exception e){
            logger.error("增加题目失败" + e.getMessage());
        }
        // 失败返回 1
        return WendaUtil.getJsonString(HttpStatusCode.SERVIC_ERROR, "添加失败");
    }

    @RequestMapping(value = "api/question/delete",method={RequestMethod.POST})
    @ResponseBody
    String deleteQuestion(@RequestBody Map<String, Object> reqMap) {
        try{
            int qid = Integer.valueOf(reqMap.get("qid").toString());
            Map<String,Object> map = new HashMap<>();
            if(hostHolder.getUsers() == null) {
                map.put("msg","请登录后再删除问题");
                map.put("status","fail");
                //999 返回到登录页面
                return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, map);
                // question.setUserId(WendaUtil.Anonymous_USERID);
            }

            if(questionService.deleteQuestion(qid) > 0){
                // 成功返回 0
                map.put("msg","删除成功");
                map.put("status","success");
                return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, map);
            }

        }catch (Exception e){
            logger.error("修复问题失败" + e.getMessage());
        }
        // 失败返回 1
        return WendaUtil.getJsonString(HttpStatusCode.SERVIC_ERROR, "删除问题失败");
    }


    /**
     * 更新问题
     * @return
     */
    @RequestMapping(value = "api/question/update",method={RequestMethod.PUT})
    @ResponseBody
    String updateQuestion(@RequestBody Map<String, Object> reqMap){
        try{

            String title = reqMap.get("title").toString();
            String content = reqMap.get("content").toString();
            String markdownContent = reqMap.get("markdownContent").toString();
            int topicId =  Integer.valueOf(reqMap.get("topicId").toString());
            int qid = Integer.valueOf(reqMap.get("qid").toString());

            Question question = questionService.selectById(qid);
            question.setTitle(title);
            question.setContent(content);
            question.setMarkdownContent(markdownContent);
            question.setTopicId(topicId);

            Map<String,Object> map = new HashMap<>();
            if(hostHolder.getUsers() == null) {
                map.put("msg","请登录后再发表问题");
                map.put("status","fail");
                //999 返回到登录页面
                return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, map);
                // question.setUserId(WendaUtil.Anonymous_USERID);
            }

            if(questionService.updateQuestion(question) > 0){
                // 成功返回 0
                map.put("msg","更新成功");
                map.put("status","success");
                return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, map);
            }

        }catch (Exception e){
            logger.error("修复问题失败" + e.getMessage());
        }
        // 失败返回 1
        return WendaUtil.getJsonString(HttpStatusCode.SERVIC_ERROR, "修改问题失败");
    }
}