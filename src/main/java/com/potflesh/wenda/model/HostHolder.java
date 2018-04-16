package com.potflesh.wenda.model;

import org.springframework.stereotype.Component;

/**
 * Created by bazinga on 2017/4/12.
 */
@Component
public class HostHolder {
    // 为每个访问的用户设置一个线程，每个线程对应一个关联的 user 变量
    private static ThreadLocal<User> users = new ThreadLocal<>();

    public static User getUsers() {
        return users.get();
    }

    public static void setUsers(User user) {
        users.set(user);
    }

    public void clear(){
        users.remove();
    }


}
