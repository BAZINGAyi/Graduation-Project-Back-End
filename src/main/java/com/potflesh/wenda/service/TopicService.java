package com.potflesh.wenda.service;
import com.potflesh.wenda.dao.TopicDAO;
import com.potflesh.wenda.model.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Created by bazinga on 11/04/2018.
 */
@Service
public class TopicService {

    @Autowired
    private TopicDAO topicDAO;

    public List<Topic> SelectToplicList() {
        return topicDAO.select();
    }
}
