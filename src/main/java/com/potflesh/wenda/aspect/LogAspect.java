package com.potflesh.wenda.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Created by bazinga on 2017/4/7.
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger= LoggerFactory.getLogger(LogAspect.class);
            // 第一个 * 是返回值


    @Before("execution(* com.potflesh.wenda.controller.HomeController.*(..))")
    public void beforeMethod(JoinPoint joinPoint){

        StringBuilder sb = new StringBuilder();

        for (Object arg:joinPoint.getArgs()){
            sb.append("arg"+ arg.toString());
        }
        logger.info("brefore method"+sb.toString());
    }


    @After("execution(* com.potflesh.wenda.controller.HomeController.*(..))")
    public void afterMethod(){
        logger.info("after method");

    }
}
