package com.potflesh.wenda.controller;

import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.CommentService;
import com.potflesh.wenda.service.FollowService;
import com.potflesh.wenda.service.QuestionService;
import com.potflesh.wenda.service.UserService;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/10.
 */
@Controller
public class HomeController {

    private static final Logger logger= LoggerFactory.getLogger(HomeController.class);

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {

        List<ViewObject> vos =  getQuestions(0,0,10);

        model.addAttribute("vos",vos);

        return "index";
    }

    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId) {
        model.addAttribute("vos", getQuestions(userId, 0, 10));
        User user = userService.getUser(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        if (hostHolder.getUsers() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUsers().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        return "profile";
    }

    private List<ViewObject> getQuestions(int userId,int offset, int limit){

        List<Question> questionList = questionService.getLatestQuestions(userId,offset,limit);

        List<ViewObject> vos = new ArrayList<ViewObject>();

        for (Question question : questionList){

            ViewObject vo = new ViewObject();

            vo.set("question", question);
            vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            vo.set("user", userService.getUser(question.getUserId()));

            vos.add(vo);

        }

        return vos;
    }

    @RequestMapping(path = {"api/getquestions"}, method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String getquestions(@RequestParam("offset") int offset) {

        List<Question> questionList = questionService.getLatestQuestions(0,offset,10);

        List< Map<String,Object> > vos = new ArrayList< Map<String,Object>>();

        for (Question question : questionList){
            Map vo = new HashedMap();
            vo.put("question", question);
            vo.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            vo.put("user", userService.getUser(question.getUserId()));
            vos.add(vo);
        }

        return WendaUtil.getJSONString(200, vos);
    }

//    public static void main(String[] args){
//        List<Question> questionList = new ArrayList<>();
//
//        Question question = new Question();
//
//        question.setTitle("asdas");
//        question.setContent("asdasdsda");
//        question.setCommentCount(5);
//
//        questionList.add(question);
//
//        Question question1 = new Question();
//        question1.setTitle("asgdfgddasdas");
//        question1.setContent("dfgasdasdsda");
//        question1.setCommentCount(5);
//        questionList.add(question1);
//
//        List<ViewObject> vos = new ArrayList<ViewObject>();
//
//        for (Question question3 : questionList){
//
//            ViewObject vo = new ViewObject();
//
//            vo.set("question", question3);
//
//            User user = new User();
//            user.setName("zhang");
//            user.setHeadUrl("asd7777");
//            vo.set("user", user);
//
//            vos.add(vo);
//
//        }
//        User user = new User();
//        user.setName("zhang");
//        user.setHeadUrl("asd7777");
//
//        String var = JSON.toJSONString(questionList);
//        System.out.println(var);
//
//        Map<String,Object> s = new HashedMap();
//        s.put("123",questionList);
//        s.put("1223",user);
//        System.out.println(JSON.toJSONString(s));
//
//        List< Map<String,Object>> list = new ArrayList<>();
//        list.add(s);
//        list.add(s);
//
//        System.out.println(JSON.toJSONString(list));
//
//
//    }
}
