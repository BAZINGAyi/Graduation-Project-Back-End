package com.potflesh.wenda.controller;
import com.potflesh.wenda.model.EntityType;
import com.potflesh.wenda.model.Feed;
import com.potflesh.wenda.model.HostHolder;
import com.potflesh.wenda.service.FeedService;
import com.potflesh.wenda.service.FollowService;
import com.potflesh.wenda.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.potflesh.wenda.utils.RedisKeyUtil;

import java.util.ArrayList;
import java.util.List;

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
