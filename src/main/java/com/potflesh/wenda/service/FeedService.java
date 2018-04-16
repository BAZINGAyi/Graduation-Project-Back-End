package com.potflesh.wenda.service;

import com.potflesh.wenda.dao.FeedDAO;
import com.potflesh.wenda.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by bazinga on 2017/4/26.
 */
@Service
public class FeedService {
    @Autowired
    FeedDAO feedDAO;

    // 拉模式
    public List<Feed> getUserFeeds(int maxId, List<Integer> userIds, int count){
        return feedDAO.selectUserFeeds(maxId,userIds,count);
    }

    public boolean addFeed(Feed feed){
        feedDAO.addFeed(feed);
        return feed.getId() > 0;
    }

    // 推模式
    public Feed getById(int id){
        return feedDAO.getFeedById(id);
    }

    public List<Feed> getUserFeeds(int userId){
        // 发生事件的类型
        return feedDAO.selectSingleUserFeeds(userId);
    }

}
