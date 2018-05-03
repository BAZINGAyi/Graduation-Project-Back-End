package com.potflesh.wenda.model;

/**
 * 以后的项目可能会扩展，评论不是只有问题的评论，还有可能是给问题的评论再去评论，也有可能给用户做评论，所以这里就用评论的类型加上评论
 * 内容的 id 做区分
 *
 * 这里的 type 类型表示每一个 event 触发的事件类型，也就是当前发生的事件是对谁的，
 * 比如对评论的评论和对评论点赞或者点踩都是对评论的操作，
 * event 的类型都是 ENTITY_COMMENT
 */
public class EntityType {
    public static int ENTITY_QUESTION = 1;
    public static int ENTITY_COMMENT = 2;
    public static int ENTITY_USER = 3;
    // 可能会加入一个用户的关注的问题
}
