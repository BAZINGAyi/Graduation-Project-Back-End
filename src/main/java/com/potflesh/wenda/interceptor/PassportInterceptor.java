package com.potflesh.wenda.interceptor;
import com.potflesh.wenda.dao.LoginTicketDAO;
import com.potflesh.wenda.dao.UserDAO;
import com.potflesh.wenda.model.HostHolder;
import com.potflesh.wenda.model.LoginTicket;
import com.potflesh.wenda.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by bazinga on 2017/4/12.
 */
// 拦截器可以用于权限判断
@Component
public class PassportInterceptor implements HandlerInterceptor {
    @Autowired
    LoginTicketDAO loginTicketDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    HostHolder hostHolder;

    // 所有http请求开始之前，false 代表失败
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        String ticket = null;

        if(httpServletRequest.getCookies() != null){
            for(Cookie cookie : httpServletRequest.getCookies()){
                if(cookie.getName().equals("ticket")){
                   ticket = cookie.getValue();
                   break;
                }
            }
        }

        if(ticket!=null){
            LoginTicket loginTicket = loginTicketDAO.selectByTicket(ticket);
            // ticket 过期或者状态是无效的
            if(loginTicket == null || loginTicket.getExpired().before(new Date())
        ||loginTicket.getStatus()!=0){
                return true;
            }
            // ticket 没问题 将 user 的信息保存在 Hostholder，可以随时访问
            User user = userDAO.selectById(loginTicketDAO.selectByTicket(ticket).getUserId());

            hostHolder.setUsers(user);
        }



        return true;
    }
    // 渲染页面之前
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

        if(modelAndView != null){
            // 在所有的 html 模版 和 model 加入这个user变量
            // 可以直接在 html 模版中直接使用
            modelAndView.addObject("user",hostHolder.getUsers());
        }

    }
    // 渲染完成
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        // 用户关闭连接后，清除掉
        hostHolder.clear();
    }
}
