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
    RedisService redisService;

    public long getLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        return redisService.scard(likeKey);
    }

    public long getDisLikeCount(int entityType, int entityId) {
        String dislikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return redisService.scard(dislikeKey);
    }

    public int getLikeStatus(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
       // 如果在喜欢的集合里返回 1
       if (redisService.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        // 如果在不喜欢的集合里返回 -1 都不在返回 0
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return redisService.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;
    }

    public long like(int userId, int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        redisService.sadd(likeKey, String.valueOf(userId));
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        redisService.srem(disLikeKey, String.valueOf(userId));

        return redisService.scard(likeKey);
   }

    public long disLike(int userId, int entityType, int entityId) {
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        redisService.sadd(disLikeKey, String.valueOf(userId));

        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        redisService.srem(likeKey, String.valueOf(userId));

        return redisService.scard(likeKey);
    }

}
