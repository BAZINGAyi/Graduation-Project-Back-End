package com.potflesh.wenda.controller;
import com.alibaba.fastjson.JSON;
import com.potflesh.wenda.model.EntityType;
import com.potflesh.wenda.model.Feed;
import com.potflesh.wenda.model.HostHolder;
import com.potflesh.wenda.service.FeedService;
import com.potflesh.wenda.service.FollowService;
import com.potflesh.wenda.service.RedisService;
import com.potflesh.wenda.utils.HttpStatusCode;
import com.potflesh.wenda.utils.WendaUtil;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.potflesh.wenda.utils.RedisKeyUtil;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 2017/4/26.
 */
@Controller
public class FeedController {

    @Autowired
    FeedService feedService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    FollowService followService;

    @Autowired
    RedisService redisService;

    // 拉的模式
    @RequestMapping(path = {"/pullfeeds"},method = {RequestMethod.GET})
    public String getPullFeeds(Model model){
        int localUserId = hostHolder.getUsers() == null ? 0 : hostHolder.getUsers().getId();
        List<Integer> followees = new ArrayList<>();
        // 未登录
        if (localUserId == 0){

        }else {
            // 查找我关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER,
                    Integer.MAX_VALUE);
            // 加上查找我关注的问题
        }

        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE,followees,10);

        model.addAttribute("feeds",feeds);

        return "feeds";
    }

    @RequestMapping(path = {"api/pullUserFeeds"},method = {RequestMethod.GET})
    @ResponseBody
    public String getUserPullFeeds(){

        int localUserId = hostHolder.getUsers() == null ? 0 : hostHolder.getUsers().getId();
        List<Integer> followees = new ArrayList<>();
        Map<String, Object> maps = new HashedMap();

        if (localUserId != 0) {
            // 查找我关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER,
                    Integer.MAX_VALUE);
            // 这里查找的 feed 流的原理是 用户1关注对应的用户2后，被关注的用户2的动态都会被推送到用户1的feed流里，这里查出的 feed 流的 user_id 都是
            // 用户2
            // 查找我关注的问题 写法不对如果，想要增加关注问题的动态 可以把 feed 中的 user_id 改成 entity_id 再增加 entity_type 进行区分
//            followees.addAll(followService.getFollowees(localUserId, EntityType.ENTITY_QUESTION,
//                    Integer.MAX_VALUE));
            // 这里同样支持给关注某个问题的用户发消息提醒
        }

        // 未登录则获取最近所有用户的 feed
        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE,followees,10);

        List<Map<String, Object>> feedsListMap = new ArrayList<>();
        // 将序列化的feed信息换成 json 格式
        for (int i = 0; i<feeds.size(); i++) {
            Map<String,Object> feedMap = new HashedMap();
            feedMap.put("createdDate",feeds.get(i).getCreatedDate());
            feedMap.put("id",feeds.get(i).getId());
            feedMap.put("type",feeds.get(i).getType());
            feedMap.put("userId",feeds.get(i).getUserId());
            Map<String,Object> feedContentMap = new HashedMap();
            feedContentMap.put("userId",feeds.get(i).get("userId"));
            feedContentMap.put("userName",feeds.get(i).get("userName"));
            feedContentMap.put("userHead",feeds.get(i).get("userId"));
            feedContentMap.put("questionTitle",feeds.get(i).get("questionTitle"));
            feedContentMap.put("questionId",feeds.get(i).get("questionId"));
            feedMap.put("data", feedContentMap);
            feedsListMap.add(feedMap);
        }
        maps.put("feeds",feedsListMap);

        if (feeds != null && feeds.size() != 0) {
            maps.put("msg", "请求成功");
            return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, maps);
        } else {
            maps.put("msg", "没有数据");
            return WendaUtil.getJSONString(HttpStatusCode.NO_CONTENT, maps);
        }
    }



    // 推的模式
    @RequestMapping(path = {"/pushfeeds"},method = {RequestMethod.GET})
    public String getPushFeeds(Model model){
        int localUserId = hostHolder.getUsers() == null ? 0 : hostHolder.getUsers().getId();
        List<String> feedIds = redisService.lrange(RedisKeyUtil.getTimelineKey(localUserId),
                0,10);
        List<Feed> feeds = new ArrayList<>();
        // 将发生的事件取出来
        for (String feedId:feedIds){
            Feed feed = feedService.getById(Integer.valueOf(feedId));
            if(feed == null)
                continue;
            feeds.add(feed);
        }

        model.addAttribute("feeds",feeds);
        return "feeds";
    }
}
