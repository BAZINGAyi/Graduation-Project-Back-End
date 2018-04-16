package com.potflesh.wenda.controller;

import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.MessageService;
import com.potflesh.wenda.service.UserService;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bazinga on 2017/4/15.
 */
@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                             @RequestParam("content") String content) {

        try {
            if (hostHolder.getUsers() == null) {
                return WendaUtil.getJsonString(999, "未登录");
            }
            User user = userService.selectByName(toName);
            if (user == null) {
                return WendaUtil.getJsonString(1, "用户不存在");
            }

            Message msg = new Message();
            msg.setContent(content);
            msg.setFromId(hostHolder.getUsers().getId());
            msg.setToId(user.getId());
            msg.setCreatedDate(new Date());
            //msg.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
            messageService.addMessage(msg);
            return WendaUtil.getJsonString(0);
        } catch (Exception e) {
            logger.error("发送消息失败" + e.getMessage());
            return WendaUtil.getJsonString(1, "发送消息失败");
        }

    }

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String conversationList(Model model) {
        try {
            if(hostHolder.getUsers() == null)
                return "redirect:/reglogin";
            int localUserId = hostHolder.getUsers().getId();
            List<ViewObject> conversations = new ArrayList<ViewObject>();
            List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message", msg);
                // 有我发给别人的消息和别人发给我的消息，但业务的要求是显示来自别人的消息
                int targetId = msg.getFromId() == localUserId ? msg.getToId() : msg.getFromId();
                User user = userService.getUser(targetId);
                vo.set("user", user);
                vo.set("unread", messageService.getConvesationUnreadCount(localUserId, msg.getConversationId()));
                conversations.add(vo);
            }
            model.addAttribute("conversations", conversations);
        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letter";
    }

    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model, @Param("conversationId") String conversationId) {
        try {
            List<Message> conversationList = messageService.getConversationDetail(conversationId, 0, 10);
            messageService.updateConversationReadCount(conversationId); // 更新已经阅读数量
            List<ViewObject> messages = new ArrayList<>();
            int userLocalId  = 0;
            if(hostHolder.getUsers()!=null)
                userLocalId = hostHolder.getUsers().getId();

            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message", msg);
                User user = userService.getUser(msg.getFromId());
                if (user == null) {
                    continue;
                }
                if(msg.getFromId() != userLocalId){
                    vo.set("fromOther",1);
                    vo.set("otherUsername",user.getName());
                }
                else
                    vo.set("fromOther",0);

                vo.set("headUrl", user.getHeadUrl());
                vo.set("userId", user.getId());
                messages.add(vo);
            }
            model.addAttribute("messages", messages);
        } catch (Exception e) {
            logger.error("获取详情消息失败" + e.getMessage());
        }

        return "letterDetail";
    }
}