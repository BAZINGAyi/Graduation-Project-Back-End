package com.potflesh.wenda.controller;

import com.potflesh.wenda.model.EntityType;
import com.potflesh.wenda.model.Question;
import com.potflesh.wenda.service.FollowService;
import com.potflesh.wenda.service.QuestionService;
import com.potflesh.wenda.service.UserService;
import com.potflesh.wenda.utils.WendaUtil;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bazinga on 17/04/2018.
 */
@Controller
public class SearchController {

    @Autowired
    QuestionService questionService;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @RequestMapping(value = "api/getSearchQuestionList")
    @ResponseBody
    String getSearchQuestionList(@RequestParam("searchContent")String searchContent,
                                @RequestParam("offset")int offset) {

        // 存放每个 question 和 对应 question 的话题类型
        List<Question> questionTopicList = questionService.getLastSearchQuestionList(searchContent, offset);
        List<Map<String,Object>> vos = new ArrayList< Map<String,Object>>();
        for (Question question : questionTopicList){
            Map questionMap = new HashedMap();
            questionMap.put("question", question);
            questionMap.put("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            questionMap.put("user", userService.getUser(question.getUserId()));
            vos.add(questionMap);
        }

        return WendaUtil.getJSONString(1, vos);
    }
}
