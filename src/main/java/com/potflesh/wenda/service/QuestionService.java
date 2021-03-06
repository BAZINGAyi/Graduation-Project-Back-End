package com.potflesh.wenda.service;
import com.potflesh.wenda.dao.QuestionDAO;
import com.potflesh.wenda.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.Set;

/**
 * Created by bazinga on 2017/4/10.
 */
@Service
public class QuestionService {
    @Autowired
    QuestionDAO questionDAO;

    @Autowired
    SensitiveService sensitiveService;


    public List<Question> getLatestQuestions(int userId, int offset, int limit ){

        return questionDAO.selectLatestQuestions(userId,offset,limit);
    }

    public int addQuestion(Question question){
        //敏感词过滤
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setMarkdownContent(sensitiveService.filter(question.getMarkdownContent()));
        return questionDAO.addQuestion(question) > 0 ? question.getId():0;
    }

    public int deleteQuestion(int id) {
        return questionDAO.deleteQuestion(id);
    }

    public int updateQuestion(Question question) {
        //敏感词过滤
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setMarkdownContent(sensitiveService.filter(question.getMarkdownContent()));
        return questionDAO.UpdateQuestion(question);
    }

    public Question selectById(int qid){
        return questionDAO.selectById(qid);
    }

    public int updateCommentCount(int id, int count) {
        return questionDAO.updateCommentCount(id, count);
    }

    public List<Question> getLastTopicQuestionList(int tId, int offset) {
        return questionDAO.selectLastTopicQuestionListByTid(tId, offset, 10);
    }

    public List<Question> getLastSearchQuestionList(String searchContent, int offset) {
        return questionDAO.selectLastQuestionQuestionListByTitle(searchContent, offset, 10);
    }

    public List<Question> getQuestionsByQuestionIdList(Set<Integer> questionIdList, int offset) {
        return questionDAO.getQuestionsByQuestionIdList(questionIdList, offset, 10);
    }

    public List<Question> getQuestionsByQuestionIdList(List<Integer> questionIdList, int offset) {
        return questionDAO.getQuestionsByQuestionIdList(questionIdList, offset, 10);
    }
}
