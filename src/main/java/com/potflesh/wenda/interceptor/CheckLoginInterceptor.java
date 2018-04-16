package com.potflesh.wenda.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by bazinga on 2017/4/12.
 */
@Component
public class CheckLoginInterceptor implements HandlerInterceptor {

    // 所有http请求开始之前，false 代表失败
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        String illehalUri = httpServletRequest.getQueryString();

        // 如果输入外部的非法链接，则跳回首页

        if (illehalUri != null)

        if(illehalUri.contains("http"))

            httpServletResponse.sendRedirect("/reglogin/");

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
