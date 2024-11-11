package com.ai.backend.controller;

import com.ai.backend.entity.LoginTicket;
import com.ai.backend.entity.User;
import com.ai.backend.service.UserService;
import com.ai.backend.util.CookieUtil;
import com.ai.backend.util.EvaluationUtil;
import com.ai.backend.util.HostHolder;
import com.ai.backend.util.UserUtil;
import com.google.code.kaptcha.Producer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements UserUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private HostHolder hostHolder;





    @RequestMapping(path = "/register", method = RequestMethod.POST)
    @ResponseBody
    public String register(@RequestBody Map<String, Object> requestBody){
        User user = new User();
        Map<String, Object> form = (Map<String, Object>) requestBody.get("registerForm");
        user.setUsername((String)form.get("username"));
        user.setPassword((String)form.get("password"));
        user.setEmail((String)form.get("email"));
        //User user = (User)requestBody.get("user");
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            return "success";
        }
        else{

            return (String)map.get("msg");

        }
    }

    @RequestMapping(path = "/activation", method = RequestMethod.GET)
    @ResponseBody
    public String activation(@RequestParam int id, @RequestParam String code){
        int result = userService.activation(id, code);
        if (result == ACTIVATION_SUCCESS){
            return "激活成功！";
        }else if(result == ACTIVATION_REPEAT){
            return "重复激活！";
        }else{
           return "激活失败！";
        }
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    @ResponseBody
    public void getKaptch(HttpServletResponse response , HttpSession session){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        session.setAttribute("kaptcha", text);

        String kaptchaOwner = EvaluationUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath("/api");
        response.addCookie(cookie);





        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }

    }

    @RequestMapping(path="/getTicket", method = RequestMethod.GET)
    @ResponseBody
    public String getTicket(HttpServletRequest request){
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null){
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                return loginTicket.getTicket();
            }
        }
        return "fail";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestBody Map<String, Object> request, HttpSession session, HttpServletResponse response){
        request = (Map<String, Object>) request.get("form");
        String code = (String) request.get("code");
        String kaptcha = (String) session.getAttribute("kaptcha");
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        //boolean rememberme = (boolean) request.get("rememberme");
        System.out.println(kaptcha);
        System.out.println(code);

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){

            return "code";
        }

        Map<String, Object> map = userService.login(username, password, DEFAULT_EXPIRED_SECONDS);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath("/api");
            cookie.setMaxAge(DEFAULT_EXPIRED_SECONDS);
            response.addCookie(cookie);
            return map.get("ticket").toString();
        }else{

            return  (String)map.get("msg");
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    @ResponseBody
    public String logout(@RequestParam String ticket){
        userService.logout(ticket);
        return "success";
    }


}