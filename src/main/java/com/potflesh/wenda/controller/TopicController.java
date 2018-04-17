package com.potflesh.wenda.controller;

import com.potflesh.wenda.model.Topic;
import com.potflesh.wenda.service.TopicService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.potflesh.wenda.utils.WendaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 11/04/2018.
 */
@Controller
public class TopicController {

    @Autowired
    TopicService topicService;
    @RequestMapping(path = {"api/getTopicList"}, method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String getAllTopicList(@RequestParam("offset") int offset) {

        List<Topic> topicList = topicService.SelectToplicList();
        List<Map<String,Object>> vos = new ArrayList< Map<String,Object>>();
        for (Topic topic : topicList){
            Map vo = new HashedMap();
            vo.put("topic", topic);
            vos.add(vo);
        }

        return WendaUtil.getJSONString(200, vos);
    }
}
