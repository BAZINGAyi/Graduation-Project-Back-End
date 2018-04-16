package com.potflesh.wenda.model;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

/**
 * 新鲜事
 */
public class Feed {
    private int id;
    // 表示是什么类型的新鲜事 如评论的新鲜事
    private int type;
    private int userId;
    private Date createdDate;
    // JSON 显示各种类型的字段
    private String data;
    private JSONObject dataJSON = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        dataJSON = JSONObject.parseObject(data);
    }
    // 用于取出 json 中保留的各项信息

    /**
     * #obj.xxx =》 obj.getXX() obj.get("XXX") obj.isXX();
     * @param key
     * @return
     */
    public String get(String key) {
        return dataJSON == null ? null : dataJSON.getString(key);
    }
}
