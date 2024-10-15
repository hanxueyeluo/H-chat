package com.easychat.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {

    //校验是否登录
    boolean checkLogin() default true;
    //是否为管理员
    boolean checkAdmin() default false;
}
