package com.potflesh.wenda.service;
import com.potflesh.wenda.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by bazinga on 2017/4/16.
 */
@Service
public class LikeService {

    @Autowired
    RedisService jedisService;


    public long getLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        return jedisService.scard(likeKey);
    }

    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
       // 如果在喜欢的集合里返回 1
       if (jedisService.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        // 如果在不喜欢的集合里返回 0 都不在返回 -1
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return jedisService.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;
    }

    public long like(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisService.sadd(likeKey, String.valueOf(userId));
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisService.srem(disLikeKey, String.valueOf(userId));

        return jedisService.scard(likeKey);
   }

    public long disLike(int userId, int entityType, int entityId) {
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisService.sadd(disLikeKey, String.valueOf(userId));

        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisService.srem(likeKey, String.valueOf(userId));

        return jedisService.scard(likeKey);
    }

}
