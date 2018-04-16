package com.potflesh.wenda.async.hanlder;

import com.potflesh.wenda.async.EventHandler;
import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.model.Message;
import com.potflesh.wenda.model.User;
import com.potflesh.wenda.service.MessageService;
import com.potflesh.wenda.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by bazinga on 2017/4/17.
 */
@Component
public class LikeHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        // user4 为默认的系统用户
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        message.setContent("用户" + user.getName()
                + "赞了你的评论,http://127.0.0.1:8080/question/" + model.getExt("questionId"));

        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LIKE);
    }
}
