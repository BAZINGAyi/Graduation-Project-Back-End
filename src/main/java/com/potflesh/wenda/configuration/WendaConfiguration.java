package com.potflesh.wenda.configuration;

import com.potflesh.wenda.interceptor.CheckLoginInterceptor;
import com.potflesh.wenda.interceptor.LoginRequirementInterceptor;
import com.potflesh.wenda.interceptor.PassportInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by bazinga on 2017/4/12.
 */
@Component
public class WendaConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    PassportInterceptor passportInterceptor;

    @Autowired
    LoginRequirementInterceptor loginRequirementInterceptor;

    @Autowired
    CheckLoginInterceptor checkLoginInterceptor;

    // 在系统初始化的时候加入自己的拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注意先后顺序  顺序1
        registry.addInterceptor(passportInterceptor);
        // 当和用户有关的页面走下面拦截器 顺序2
        registry.addInterceptor(loginRequirementInterceptor)
        .addPathPatterns("/user/*").addPathPatterns("/msg/*");
        registry.addInterceptor(checkLoginInterceptor);
        super.addInterceptors(registry);
    }
}
