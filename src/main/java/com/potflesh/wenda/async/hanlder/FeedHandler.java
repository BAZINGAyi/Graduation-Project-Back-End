package com.potflesh.wenda.async.hanlder;

import com.alibaba.fastjson.JSONObject;
import com.potflesh.wenda.async.EventHandler;
import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.*;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.potflesh.wenda.utils.RedisKeyUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FeedService feedService;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    FollowService followService;

    @Autowired
    RedisService redisService;

    private String buildFeedData(EventModel eventModel){
        Map<String,String> map = new HashedMap();
        User actor = userService.getUser(eventModel.getActorId());
        if(actor == null)
            return null;
        map.put("userId",String.valueOf(actor.getId()));
        map.put("userHead",actor.getHeadUrl());
        map.put("userName",actor.getName());

        if(eventModel.getType() == EventType.COMMENT_MyFans||
                (eventModel.getType() == EventType.COMMENT_Focus_Question) ||
                (eventModel.getEntityType() == EntityType.ENTITY_QUESTION
                    && eventModel.getType() == EventType.FOLLOW)){
            Question question = questionService.selectById(eventModel.getEntityId());
            if (question == null)
                return null;
            map.put("questionId",String.valueOf(question.getId()));
            map.put("questionTitle",question.getTitle());
            return JSONObject.toJSONString(map);
        }
        return null;
    }

    @Override
    public void doHandle(EventModel model) {
        Feed feed = new Feed();
        feed.setCreatedDate(new Date());
        feed.setUserId(model.getActorId());
        feed.setType(model.getType().getValue());
        feed.setData(buildFeedData(model));
        if(feed.getData() == null)
            return;
        feedService.addFeed(feed);

        // 上面是拉的过程

        // 下面是推的过程
        // 给发生该事件的所有粉丝推
//        List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER,model.getActorId(),
//                Integer.MAX_VALUE);
//        // 0 代表系统，当未登录查看的系统的队列
//        followers.add(0);
//        for (int follower : followers){
//            // 在触发事件的用户的所有粉丝中，加入触发的事件
//            String timeLineKey = RedisKeyUtil.getTimelineKey(follower);
//            redisService.lpush(timeLineKey,String.valueOf(feed.getId()));
//        }
        checkEventType(model,feed.getId());


    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[]{EventType.FOLLOW,
                EventType.COMMENT_MyFans,EventType.COMMENT_Focus_Question});
    }


    public Map checkEventType(EventModel eventModel,int feedId){

        EventType e = eventModel.getType();
        switch (e) {
            case FOLLOW: {
                if (eventModel.getEntityType() == EntityType.ENTITY_QUESTION) {
                    List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, eventModel.getActorId(),
                            Integer.MAX_VALUE);

                    // 0 代表系统，当未登录查看的系统的队列
                    followers.add(0);
                    for (int follower : followers) {
                        // 在触发事件的用户的所有粉丝中，加入触发的事件
                        String timeLineKey = RedisKeyUtil.getTimelineKey(follower);
                        redisService.lpush(timeLineKey, String.valueOf(feedId));
                    }
                }
            }break;

            case COMMENT_MyFans: {
                List<Integer> followers = followService.getFollowers(EntityType.ENTITY_USER, eventModel.getActorId(),
                        Integer.MAX_VALUE);
                // 0 代表系统，当未登录查看的系统的队列
                followers.add(0);
                for (int follower : followers) {
                    // 在触发事件的用户的所有粉丝中，加入触发的事件
                    String timeLineKey = RedisKeyUtil.getTimelineKey(follower);
                    redisService.lpush(timeLineKey, String.valueOf(feedId));
                }
            }
            break;

            case COMMENT_Focus_Question: {
                // 给关注该问题的用户发送消息
                List<Integer> followers = followService.getFollowers(EntityType.ENTITY_QUESTION,
                        eventModel.getEntityId(), Integer.MAX_VALUE);
                // 0 代表系统，当未登录查看的系统的队列
                followers.add(0);
                for (int follower : followers) {
                    String timeLineKey = RedisKeyUtil.getTimelineKey(follower);
                    redisService.lpush(timeLineKey, String.valueOf(feedId));
                }
            }
            break;
        }
        return null;
    }


}

