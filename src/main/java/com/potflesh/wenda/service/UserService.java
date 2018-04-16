package com.potflesh.wenda.service;
import com.potflesh.wenda.dao.LoginTicketDAO;
import com.potflesh.wenda.dao.UserDAO;
import com.potflesh.wenda.model.LoginTicket;
import com.potflesh.wenda.model.User;
import com.potflesh.wenda.utils.WendaUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by bazinga on 2017/4/10.
 */
@Service
public class UserService {
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private LoginTicketDAO loginTicketDAO;

    public User getUser(int id){

        return userDAO.selectById(id);
    }

    public User selectByName(String name) {
        return userDAO.selectByname(name);
    }

    public Map<String,String> register(String name , String password){

        Map<String,String> map = new HashedMap();

        if(StringUtils.isBlank(name)){
            map.put("msg","用户名不能为空");
            return map;
        }

        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        User user = userDAO.selectByname(name);
        if(user != null){
            map.put("msg","用户名存在");
            return map;
        }

        user = new User();
        user.setName(name);
        user.setSalt(UUID.randomUUID().toString().substring(0,5));
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png",
                new Random().nextInt(1000)));
        user.setPassword(WendaUtil.MD5(password+user.getSalt()));
        userDAO.addUser(user);

        // 第一次登录就下发 ticket
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);

        return map;
    }


    public Map<String,String> login(String name , String password){

        Map<String,String> map = new HashedMap();

        if(StringUtils.isBlank(name)){
            map.put("msg","用户名不能为空");
            return map;
        }

        if(StringUtils.isBlank(password)){
            map.put("msg","密码不能为空");
            return map;
        }

        User user = userDAO.selectByname(name);
        if(user == null){
            map.put("msg","用户不存在");
            return map;
        }

        if (!WendaUtil.MD5( password + user.getSalt() ).equals( user.getPassword() ) ) {
            map.put("msg", "密码不正确");
            return map;
        }

        // 登录成功记录一个 ticket
       String ticket =  addLoginTicket(user.getId());
        // 传给浏览器
        map.put("ticket",ticket);
        return map;
    }
//增加一个 Ticket
    public String addLoginTicket(int userId){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(userId);
        Date now = new Date();
        now.setTime(3600*24*100+now.getTime());
        loginTicket.setExpired(now);
        loginTicket.setStatus(0);
        loginTicket.setTicket(UUID.randomUUID().toString().replaceAll("-",""));
        loginTicketDAO.addTicket(loginTicket);
        return loginTicket.getTicket();
    }

    public void logout(String ticket){
        loginTicketDAO.updateStatus(ticket,1);
    }
}
