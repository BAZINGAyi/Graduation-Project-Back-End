package com.potflesh.wenda.utils;


/**
 * LIKE 和 DISLIKE 使用集合作为底层的数据结构 因为需要保证唯一性
 * 异步事件的处理使用链表作为存储结构，简单的从头插入事件从尾部取出事件，以后可以考虑使用优先队列，可以防止一个用户提交的任务太多
 * FOLLOWER 和 FOLLOWEE 使用有序集合作为底层的数据结构，因为需要按照时间的排序
 */
public class RedisKeyUtil {
    private static String SPLIT          = ":";
    private static String BIZ_LIKE       = "LIKE";
    private static String BIZ_DISLIKE    = "DISLIKE";
    private static String BIZ_EVENTQUEUE = "EVENT_QUEUE";

    // 粉丝
    private static String BIZ_FOLLOWER   = "FOLLOWER";

    //关注对象
    private static String BIZ_FOLLOWEE   = "FOLLOWEE";

    // TIMELINE 每一个人都有自己的模版
    private static String BIZ_TIMELINE   = "TIMELINE";

    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getDisLikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getEventQueueKey() {
        return BIZ_EVENTQUEUE;
    }

    // 某个实体的粉丝key
    public static String getFollowerKey(int entityType, int entityId) {
        return BIZ_FOLLOWER + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    // 每个用户对某类实体的关注key
    public static String getFolloweeKey(int userId, int entityType) {
        return BIZ_FOLLOWEE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(userId);
    }

    public static String getTimelineKey(int userId) {
        return BIZ_TIMELINE + SPLIT + String.valueOf(userId);
    }
}
