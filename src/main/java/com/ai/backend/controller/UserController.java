package com.ai.backend.controller;

import com.ai.backend.entity.User;
import com.ai.backend.service.UserService;
import com.ai.backend.util.UserUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements UserUtil {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);





    @Autowired
    private UserService userService;


    @RequestMapping(path = "/profile", method = RequestMethod.GET)
    @ResponseBody
    public User getUserById(@RequestParam int userId){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }


        return user;
    }

    @RequestMapping(path = "/profile", method = RequestMethod.POST)
    @ResponseBody
    public boolean updateUser(@RequestBody Map<String, Object> request){



        return true;
    }


}