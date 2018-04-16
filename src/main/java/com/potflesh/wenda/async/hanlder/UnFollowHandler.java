package com.potflesh.wenda.async.hanlder;
import com.potflesh.wenda.async.EventHandler;
import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.EntityType;
import com.potflesh.wenda.model.Feed;
import com.potflesh.wenda.model.Message;
import com.potflesh.wenda.model.User;
import com.potflesh.wenda.service.FeedService;
import com.potflesh.wenda.service.MessageService;
import com.potflesh.wenda.service.RedisService;
import com.potflesh.wenda.service.UserService;
import com.potflesh.wenda.utils.RedisKeyUtil;
import com.potflesh.wenda.utils.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Component
public class UnFollowHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    FeedService feedService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());

        if (model.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setContent("用户" + user.getName()
                    + "取消关注了你的问题,http://127.0.0.1:8080/question/" + model.getEntityId());
        } else if (model.getEntityType() == EntityType.ENTITY_USER) {
            message.setContent("用户" + user.getName()
                    + "取消关注了你,http://127.0.0.1:8080/user/" + model.getActorId());

            // 如果取消关注，则清空 用户关注的人的所有 feed 的Id
            // 得到
            List<Feed> feedsId = feedService.getUserFeeds(model.getEntityId());
            if(feedsId != null){
                for (Feed feed : feedsId){
                    redisService.lremfeed(RedisKeyUtil.getTimelineKey
                                    (model.getActorId()),feed.getId());
                }
            }
        }

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.UNFOLLOW);
    }
}
