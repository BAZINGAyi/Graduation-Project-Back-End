package com.potflesh.wenda.controller;

import com.potflesh.wenda.model.*;
import com.potflesh.wenda.service.MessageService;
import com.potflesh.wenda.service.UserService;
import com.potflesh.wenda.utils.HttpStatusCode;
import org.apache.commons.collections.map.HashedMap;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.potflesh.wenda.utils.WendaUtil;

import javax.servlet.http.HttpSession;
import java.util.*;

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

    /////////////////////////////////////////////////////////////////////////////////// api interface ////////////////////////////////////////////////////////
    @RequestMapping(path = {"api/msg/list"}, method = {RequestMethod.GET})
    @ResponseBody
    public String getConversationList() {

        Map<String, Object> messageList = new HashMap<>();

        try {

            if (hostHolder.getUsers() == null) {
                messageList.put("msg", "请登录");
                return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, messageList);
            }

            int localUserId = hostHolder.getUsers().getId();

            // 取出和用户所有有关的 message
//            List<Message> userMessages = messageService.getMessagesByUserId(localUserId, 0 ,100);

            // 将 message 遍历按照组分类，形成新的 conversation
            List<Message> conversationList = messageService.getConversationList(localUserId, 0, 100);

            if (conversationList == null || conversationList.size() == 0) {
                messageList.put("msg", "返回的数字为空");
                return WendaUtil.getJSONString(HttpStatusCode.NO_CONTENT, messageList);
            }

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Message message : conversationList) {

                Map<String, Object> threadMaps = new HashedMap();
                threadMaps.put("id", message.getConversationId());
                if (localUserId == message.getFromId()) {
                    threadMaps.put("name", userService.getUser(message.getToId()).getName());
                    threadMaps.put("avatarSrc", userService.getUser(message.getToId()).getHeadUrl());
                } else if (localUserId == message.getToId()){
                    threadMaps.put("name", userService.getUser(message.getFromId()).getName());
                    threadMaps.put("avatarSrc", userService.getUser(message.getFromId()).getHeadUrl());
                }
                Map<String, Object> maps1 = new HashedMap();
                maps1.put("user", userService.getUser(message.getFromId()));
                maps1.put("sentAt", message.getCreatedDate());
                maps1.put("text", message.getContent());
                maps1.put("thread", threadMaps);
                threadMaps.put("lastMessage", maps1);

                Map<String, Object> maps = new HashedMap();
                maps.put("user", userService.getUser(message.getFromId()));
                maps.put("sentAt", message.getCreatedDate());
                maps.put("text", message.getContent());
                maps.put("thread", threadMaps);

                resultList.add(maps);
            }

            messageList.put("msg", "请求成功");
            messageList.put("messageList", resultList);

            return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, messageList);


            // 将 message 分成分组，形成多个 conversation。

            // 然后根据 conversation 获取个人的通话面板

            // 将形成的 conversation 放入返回的 message 里


        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }

        messageList.put("msg", "请求成功");
        return WendaUtil.getJSONString(HttpStatusCode.SERVIC_ERROR, messageList);
    }

}