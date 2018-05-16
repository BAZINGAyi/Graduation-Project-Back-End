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

    /**
     * 该接口主要功能是按照 conservation_id(也就是thread)返回message列表，并同时返回某个thead的全部消息
     * 由于功能的实现，先实现前端，之后按照前端的逻辑实现后端的逻辑，所以可能较为晦涩难懂
     * @return
     */
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

            Map<String, Object> firstThreadMaps = new HashedMap();
            int count = 0;
            String firstThreadConversationId = "";
            int firstThreadId = 0;
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Message message : conversationList) {

                Map<String, Object> threadMaps = new HashedMap();
                threadMaps.put("id", message.getConversationId());
                // 如果是登录用户自己发送的消息，则thread应该保存to_id的信息
                if (localUserId == message.getFromId()) {
                    threadMaps.put("name", userService.getUser(message.getToId()).getName());
                    threadMaps.put("avatarSrc", userService.getUser(message.getToId()).getHeadUrl());
                } else if (localUserId == message.getToId()){
                //  如果是登录用户是接收消息的对象，则thread应该保存from_id的信息
                    threadMaps.put("name", userService.getUser(message.getFromId()).getName());
                    threadMaps.put("avatarSrc", userService.getUser(message.getFromId()).getHeadUrl());
                }
                Map<String, Object> lastMessageMap = new HashedMap();
                lastMessageMap.put("author", userService.getUser(message.getFromId()));
                lastMessageMap.put("sentAt", message.getCreatedDate());
                lastMessageMap.put("text", message.getContent());
                lastMessageMap.put("thread", threadMaps);
                lastMessageMap.put("id", message.getId());
                threadMaps.put("lastMessage", lastMessageMap);

                Map<String, Object> maps = new HashedMap();
                maps.put("author", userService.getUser(message.getFromId()));
                maps.put("sentAt", message.getCreatedDate());
                maps.put("text", message.getContent());
                maps.put("thread", threadMaps);
                maps.put("id", message.getId());

                resultList.add(maps);

                // 取第一条 message 中的 thread，用于显示和他的全部的聊天内容
                if (count == 0) {
                    firstThreadMaps.put("id", message.getConversationId());
                    // 如果是登录用户自己发送的消息，则thread应该保存to_id的信息
                    if (localUserId == message.getFromId()) {
                        firstThreadMaps.put("name", userService.getUser(message.getToId()).getName());
                        firstThreadMaps.put("avatarSrc", userService.getUser(message.getToId()).getHeadUrl());
                    } else if (localUserId == message.getToId()){
                        //  如果是登录用户是接收消息的对象，则thread应该保存from_id的信息
                        firstThreadMaps.put("name", userService.getUser(message.getFromId()).getName());
                        firstThreadMaps.put("avatarSrc", userService.getUser(message.getFromId()).getHeadUrl());
                    }
                    firstThreadConversationId = message.getConversationId();
                    System.out.println("convsercaton:" + message.getConversationId());
                    System.out.println("convsercaton:" + message.getId());
                    System.out.println("convsercaton:" + message.getContent());
                    firstThreadId = message.getId();
                    System.out.println("Id:" + firstThreadId);
                    count++;
                }
            }


            // 添加和第一个 thread 的会话的所有message
            List<Message> firstThreadMessages= new ArrayList<>();
            firstThreadMessages = messageService.getConversationDetail(firstThreadConversationId, 0 , 10);
            if (firstThreadMessages != null && firstThreadMessages.size() != 0) {
                messageList.put("firstThreadMessages", generateFrontEndMessageLogicStructure(firstThreadMessages, localUserId, firstThreadId));
            }

            messageList.put("msg", "请求成功");
            messageList.put("messageList", resultList);
            messageList.put("currentThread", firstThreadMaps);
            messageList.put("currentUser", userService.getUser(localUserId));

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

    List<Map<String, Object>> generateFrontEndMessageLogicStructure( List<Message> messages, int localUserId, int firstThreadId) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Message message : messages) {
            System.out.println("nu" + firstThreadId);
            System.out.println("跳过第一个" + message.getId());
            if (firstThreadId == message.getId()) {
                continue;
            }

            Map<String, Object> threadMaps = new HashedMap();
            threadMaps.put("id", message.getConversationId());
            // 如果是登录用户自己发送的消息，则thread应该保存to_id的信息
            if (localUserId == message.getFromId()) {
                threadMaps.put("name", userService.getUser(message.getToId()).getName());
                threadMaps.put("avatarSrc", userService.getUser(message.getToId()).getHeadUrl());
            } else if (localUserId == message.getToId()){
                //  如果是登录用户是接收消息的对象，则thread应该保存from_id的信息
                threadMaps.put("name", userService.getUser(message.getFromId()).getName());
                threadMaps.put("avatarSrc", userService.getUser(message.getFromId()).getHeadUrl());
            }
            Map<String, Object> lastMessageMap = new HashedMap();
            lastMessageMap.put("author", userService.getUser(message.getFromId()));
            lastMessageMap.put("sentAt", message.getCreatedDate());
            lastMessageMap.put("text", message.getContent());
            lastMessageMap.put("thread", threadMaps);
            lastMessageMap.put("id", message.getId());
            threadMaps.put("lastMessage", lastMessageMap);

            Map<String, Object> maps = new HashedMap();
            maps.put("author", userService.getUser(message.getFromId()));
            maps.put("sentAt", message.getCreatedDate());
            maps.put("text", message.getContent());
            maps.put("thread", threadMaps);
            maps.put("id", message.getId());

            resultList.add(maps);
        }
        return resultList;
    }

    @RequestMapping(path = {"api/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessageAPI(@RequestBody Map<String, Object> reqMap) {

        Map<String, Object> messageResultMap = new HashedMap();
        String toName =  reqMap.get("toName").toString().trim();
        String content =  reqMap.get("content").toString().trim();

        try {
            if (hostHolder.getUsers() == null) {
                messageResultMap.put("MSG", "请重新登录");
                return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, messageResultMap);
            }

            User user = userService.selectByName(toName);
            if (user == null) {
                return WendaUtil.getJsonString(HttpStatusCode.REQUEST_PARAMARY_ERROR, "用户不存在");
            }

            int fromId = hostHolder.getUsers().getId();
            int toId = user.getId();

            Message msg = new Message();
            msg.setContent(content);
            msg.setFromId(hostHolder.getUsers().getId());
            msg.setToId(user.getId());
            msg.setCreatedDate(new Date());
            msg.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) : String.format("%d_%d", toId, fromId));
            messageService.addMessage(msg);

            return WendaUtil.getJsonString(HttpStatusCode.SUCCESS_STATUS, "请求成功");
        } catch (Exception e) {
            logger.error("发送消息失败" + e.getMessage());
            return WendaUtil.getJsonString(1, "发送消息失败");
        }
    }

    @RequestMapping(path = {"api/msg/ConversationList"}, method = {RequestMethod.GET})
    @ResponseBody
    public String getConversationListByIdAPI(@Param("conversationId") String conversationId) {

        Map<String, Object> messageList = new HashMap<>();
        try {

            if (hostHolder.getUsers() == null) {
                messageList.put("msg", "请登录");
                return WendaUtil.getJSONString(HttpStatusCode.Unauthorized, messageList);
            }

            int localUserId = hostHolder.getUsers().getId();

            // 将 message 遍历按照组分类，形成新的 conversation
            List<Message> conversationList = messageService.getNotIncludeMaxDateMessageList(conversationId, 0, 100);

            if (conversationList == null || conversationList.size() == 0) {
                messageList.put("msg", "返回的数字为空");
                return WendaUtil.getJSONString(HttpStatusCode.NO_CONTENT, messageList);
            }

            // -1 表示不跳过第一个
            int firstThreadId = -1;
            messageList.put("messageList", generateFrontEndMessageLogicStructure(conversationList, localUserId, firstThreadId));
            messageList.put("msg", "请求成功");

            return WendaUtil.getJSONString(HttpStatusCode.SUCCESS_STATUS, messageList);

        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }

        messageList.put("msg", "服务器出错");
        return WendaUtil.getJSONString(HttpStatusCode.SERVIC_ERROR, messageList);
    }

}