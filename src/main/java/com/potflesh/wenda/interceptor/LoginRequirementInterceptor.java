package com.potflesh.wenda.interceptor;

import com.potflesh.wenda.model.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by bazinga on 2017/4/12.
 */
@Component
public class LoginRequirementInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;

    // 所有http请求开始之前，false 代表失败
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        // 根据拦截器的优先级，当 hostHolder 为空证明用户没有登录，转去登录页面
        // 并且设置当前 Uri 的地址，用于用户登录后返回。
        if(hostHolder.getUsers() == null){
            httpServletResponse.sendRedirect("/reglogin?next="+
            httpServletRequest.getRequestURI());
        }
        return true;
    }
    // handle 处理完，开始处理
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {


    }
    // 渲染完成
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
