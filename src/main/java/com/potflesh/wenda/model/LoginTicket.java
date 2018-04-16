package com.potflesh.wenda.model;

import java.util.Date;

/**
 * Created by nowcoder on 2016/7/3.
 */

// 建这个表的目的：假如用户登录成功在访问页面，此时在 a 服务器，然后又打开一个页面进入 b 服务器，但是 b 服务器是不知道这个用户是
    // 访问过的 相当于用这张表来代替 session 共享（同一个人访问一个页面）
public class LoginTicket {
    private int id;
    private int userId;
    private Date expired; // 有效期
    private int status;// 0有效，1无效
    private String ticket;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
