package com.ding.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yanou
 * @date 2022年10月25日 7:57 下午
 */
@Component
@EnableWebMvc
@Configuration
public class RestAdapter implements WebMvcConfigurer {
    @Autowired
    public AccessLogInterceptor accessLog() {
        return new AccessLogInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(accessLog());
        registration.addPathPatterns("/api/**");
    }
}
