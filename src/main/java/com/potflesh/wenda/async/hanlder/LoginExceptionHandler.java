package com.potflesh.wenda.async.hanlder;

import com.potflesh.wenda.async.EventHandler;
import com.potflesh.wenda.async.EventModel;
import com.potflesh.wenda.async.EventType;
import com.potflesh.wenda.utils.MailSenderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 2016/7/30.
 */
@Component
public class LoginExceptionHandler implements EventHandler {
    @Autowired
    MailSenderUtil mailSenderUtil;

    @Override
    public void doHandle(EventModel model) {
        // xxxx判断发现这个用户登陆异常
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", model.getExt("username"));
        mailSenderUtil.sendWithHTMLTemplate(
                        model.getExt("email"),
                        "您今日登陆新媒体聚合平台",
                        "mails/login_exception.html"
                        , map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
