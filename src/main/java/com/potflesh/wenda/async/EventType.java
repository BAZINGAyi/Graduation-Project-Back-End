package com.potflesh.wenda.async;

/**
 * Created by bazinga on 2017/4/17.
 */
// 该类型 用于异步事件处理的判断
public enum EventType {
    LIKE(0),
    // 给发表该评论的用户的粉丝发送新鲜事
    COMMENT_MyFans(1),
    LOGIN(2),
    MAIL(3),
    FOLLOW(4),
    UNFOLLOW(5),
    // 给关注该问题的用户发表评论
    COMMENT_Focus_Question(6),;

    private int value;

    EventType(int value){
        this.value = value;
    }
    public final int getValue(){
        return this.value;
    }
}
