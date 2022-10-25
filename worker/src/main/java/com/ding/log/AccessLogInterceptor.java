package com.ding.log;

import com.sun.istack.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yanou
 * @date 2022年10月25日 7:47 下午
 */
@Component
@Slf4j
public class AccessLogInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Log.REST.info("prepare to execute handler {}, request params {} ", handler.getClass().getName(), request.getParameterMap().toString());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        Log.REST.info("prepare to post handler {}, request params {} ", handler.getClass().getName(), request.getParameterMap().toString());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        Log.REST.info("finish execute handler {}, request params {} ", handler.getClass().getName(), request.getParameterMap().toString());
    }
}