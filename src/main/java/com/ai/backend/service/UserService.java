package com.ai.backend.service;

import com.ai.backend.dao.LoginTicketMapper;
import com.ai.backend.dao.UserMapper;
import com.ai.backend.entity.LoginTicket;
import com.ai.backend.entity.User;
import com.ai.backend.util.EvaluationUtil;
import com.ai.backend.util.MailClient;
import com.ai.backend.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements UserUtil {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;




    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${backend.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;



    public User findUserById(int id){
        return userMapper.selectById(id);

    }

    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        if (user == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())){
            map.put("msg", "账号不能为空");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())){
            map.put("msg", "密码不能为空");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())){
            map.put("msg", "邮箱不能为空");
            return map;
        }

        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("msg", "账号已存在");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("msg", "邮箱已存在");
            return map;
        }

        user.setSalt(EvaluationUtil.generateUUID().substring(0, 5));
        user.setPassword(EvaluationUtil.md5(user.getPassword() + user.getSalt()));
        user.setStatus(0);
        user.setActivationCode(EvaluationUtil.generateUUID());
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        String url = String.format("%s%s/activation?id=%d&code=%s", domain, contextPath, user.getId(), user.getActivationCode());
        //String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        mailClient.sendMail(user.getEmail(), "激活账号", url);


        return map;


    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        }
        else{
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)){
            map.put("msg", "noname");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("msg", "nopassword");
            return map;
        }

        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("msg", "none");
            return map;
        }

        if (user.getStatus() == 0){
            map.put("msg", "activation");
            return map;
        }

        password = EvaluationUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("msg", "password");
            return map;
        }

        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(EvaluationUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);



        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateLoginTicket(ticket, 1);

    }

    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);



    }



}
