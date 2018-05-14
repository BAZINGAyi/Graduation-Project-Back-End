package com.potflesh.wenda.service;
import com.potflesh.wenda.dao.MessageDAO;
import com.potflesh.wenda.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by bazinga on 2017/4/15.
 */
@Service
public class MessageService {
    @Autowired
    MessageDAO messageDAO;

    @Autowired
    SensitiveService sensitiveService;

    public int addMessage(Message message){
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDAO.addMessage(message);
    }

    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return messageDAO.getConversationDetail(conversationId, offset, limit);
    }

    public List<Message> getConversationList(int userId, int offset, int limit) {
        return messageDAO.getConversationList(userId, offset, limit);
    }

    public int getConvesationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConvesationUnreadCount(userId, conversationId);
    }

    public int updateConversationReadCount(String conversationId){
        return messageDAO.setConversationReadCount(conversationId);
    }

    public List<Message> getMessagesByUserId(int userId, int offset, int limit) {
        return messageDAO.getMessageByUserId(userId, offset, limit);
    }
}
