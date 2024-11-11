package com.ai.backend.interceptor;

import com.ai.backend.entity.LoginTicket;
import com.ai.backend.entity.User;
import com.ai.backend.service.UserService;
import com.ai.backend.util.CookieUtil;
import com.ai.backend.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.OutputStream;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null){
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user == null ){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 设置错误消息
            response.setContentType("application/json;charset=UTF-8");
            try (OutputStream os = response.getOutputStream()) {
                String json = "{\"error\":\"Unauthorized\"}";
                os.write(json.getBytes("UTF-8"));
                os.flush();
            }
            // 终止进一步的处理
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}