package com.ai.backend.config;

import com.ai.backend.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;



    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        registry.addInterceptor(loginTicketInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/login", "/api/logout", "/api/register", "/api/kaptcha", "/api/getTicket")
                .excludePathPatterns("/**/*.css", "/**/*.png", "/**/*.js", "/**/*.jpg", "/**/*.jpeg");


    }
}
