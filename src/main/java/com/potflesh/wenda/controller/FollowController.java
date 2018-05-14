package com.potflesh.wenda.controller;

import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventProducer;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.CommentService;
import com.potflesh.wenda.service.FollowService;
import com.potflesh.wenda.service.QuestionService;
import com.potflesh.wenda.service.UserService;
import com.potflesh.wenda.utils.HttpStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.potflesh.wenda.utils.WendaUtil;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
public class FollowController {
    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        boolean ret = followService.follow(hostHolder.getUsers().getId(), userId,EntityType.ENTITY_USER);

        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId));

        // 返回关注的人数
        return WendaUtil.getJsonString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUsers().getId(), EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        boolean ret = followService.unfollow(hostHolder.getUsers().getId(), EntityType.ENTITY_USER, userId);

        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId));

        // 返回关注的人数
        return WendaUtil.getJsonString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUsers().getId(), EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        Question q = questionService.selectById(questionId);
        if (q == null) {
            return WendaUtil.getJsonString(1, "问题不存在");
        }

        boolean ret = followService.follow(hostHolder.getUsers().getId(), questionId,EntityType.ENTITY_QUESTION);

        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

        Map<String, Object> info = new HashMap<>();
        info.put("headUrl", hostHolder.getUsers().getHeadUrl());
        info.put("name", hostHolder.getUsers().getName());
        info.put("id", hostHolder.getUsers().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        Question q = questionService.selectById(questionId);
        if (q == null) {
            return WendaUtil.getJsonString(1, "问题不存在");
        }

        boolean ret = followService.unfollow(hostHolder.getUsers().getId(), EntityType.ENTITY_QUESTION, questionId);

        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

        Map<String, Object> info = new HashMap<>();
        info.put("id", hostHolder.getUsers().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId) {
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 10);
        if (hostHolder.getUsers() != null) {
            model.addAttribute("followers", getUsersInfo(hostHolder.getUsers().getId(), followerIds));
        } else {
            model.addAttribute("followers", getUsersInfo(0, followerIds));
        }
        model.addAttribute("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followers";
    }

    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId) {
        List<Integer> followeeIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 10);

        if (hostHolder.getUsers() != null) {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUsers().getId(), followeeIds));
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
        }
        model.addAttribute("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        model.addAttribute("curUser", userService.getUser(userId));
        return "followees";
    }

    private List<ViewObject> getUsersInfo(int localUserId, List<Integer> userIds) {
        List<ViewObject> userInfos = new ArrayList<ViewObject>();
        for (Integer uid : userIds) {
            User user = userService.getUser(uid);
            if (user == null) {
                continue;
            }
            ViewObject vo = new ViewObject();
            vo.set("user", user);
            vo.set("commentCount", commentService.getUserCommentCount(uid));
            vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, uid));
            vo.set("followeeCount", followService.getFolloweeCount(uid, EntityType.ENTITY_USER));
            if (localUserId != 0) {
                vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, uid));
            } else {
                vo.set("followed", false);
            }
            userInfos.add(vo);
        }
        return userInfos;
    }

    /////////////////////////////////////////////////////////////////////////////// api interface ///////////////////////////////////////////////////////////////////////////////////////////////

    @RequestMapping(path = {"api/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestionAPI(@RequestBody Map<String, Object> reqMap) {

        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999, "请登录");
        }

        int questionId = Integer.valueOf(reqMap.get("questionId").toString());
        Question q = questionService.selectById(questionId);
        if (q == null) {
            return WendaUtil.getJsonString(1, "问题不存在");
        }

        boolean ret = followService.follow(hostHolder.getUsers().getId(), questionId, EntityType.ENTITY_QUESTION);

        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

        // 返回跟随后的信息，用于更新页面
        Map<String, Object> info = new HashMap<>();
        info.put("headUrl", hostHolder.getUsers().getHeadUrl());
        info.put("name", hostHolder.getUsers().getName());
        info.put("id", hostHolder.getUsers().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(ret ? 200 : 1, info);
    }

    @RequestMapping(path = {"api/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestioAPI(@RequestBody Map<String, Object> reqMap) {
        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(999);
        }

        int questionId = Integer.valueOf(reqMap.get("questionId").toString());
        Question q = questionService.selectById(questionId);
        if (q == null) {
            return WendaUtil.getJsonString(1, "问题不存在");
        }

        boolean ret = followService.unfollow(hostHolder.getUsers().getId(), EntityType.ENTITY_QUESTION, questionId);

        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(q.getUserId()));

        Map<String, Object> info = new HashMap<>();
        info.put("id", hostHolder.getUsers().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));
        return WendaUtil.getJSONString(ret ? 200 : 1, info);
    }

    @RequestMapping(path = {"api/followUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followUserAPI(@RequestBody Map<String, Object> reqMap) {

        if (hostHolder.getUsers() == null) {
            return WendaUtil.getJsonString(HttpStatusCode.Unauthorized, "请登录");
        }

        int userId = Integer.valueOf(reqMap.get("userId").toString());
        User u = userService.getUser(userId);
        if (u == null) {
            return WendaUtil.getJsonString(HttpStatusCode.REQUEST_PARAMARY_ERROR, "用户不存在");
        }

        boolean ret = followService.follow(hostHolder.getUsers().getId(), userId, EntityType.ENTITY_USER);

        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUsers().getId()).setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(u.getId()));

        // 返回跟随后的信息，用于更新页面
        Map<String, Object> info = new HashMap<>();
        info.put("followeeUserCount", followService.getFolloweeCount(EntityType.ENTITY_USER, userId));
        return WendaUtil.getJSONString(ret ? HttpStatusCode.SUCCESS_STATUS : HttpStatusCode.SERVIC_ERROR, info);
    }

}
