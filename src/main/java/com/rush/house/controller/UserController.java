package com.rush.house.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.rush.house.common.constant.Constants;
import com.rush.house.common.result.JSONResult;
import com.rush.house.common.util.SessionUtil;
import com.rush.house.common.util.StrUtils;
import com.rush.house.entity.Contact;
import com.rush.house.entity.User;
import com.rush.house.service.ContactService;
import com.rush.house.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by 17512 on 2018/10/29.
 */
@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private ContactService contactService;

    /**
     * 登录拦截重定向地址
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/needLogin", method = {RequestMethod.GET, RequestMethod.POST})
    public JSONResult needLogin () {
        return new JSONResult(false, "非常抱歉, 您需要登录后才能继续操作!");
    }

    /**
     * 获取用户信息
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/info", method = {RequestMethod.GET, RequestMethod.POST})
    public JSONResult info (HttpServletRequest req) {
        try {
            User user = SessionUtil.getUser(req);
            return new JSONResult(user, "获取用户信息成功!", true);
        } catch (Exception e) {
            log.error("获取用户信息异常", e);
            return new JSONResult(false, "获取用户信息异常:" + e.getMessage());
        }
    }

    /**
     * 用户注册
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/register", method = {RequestMethod.GET, RequestMethod.POST})
    public JSONResult register (@RequestParam(name = "username", required = false) String username,
                                @RequestParam(name = "password", required = false) String password,
                                HttpServletRequest req) {
        try {
            if (StringUtils.isBlank(username)) {
                return new JSONResult(false, "用户名不能为空!");
            }
            if (StringUtils.isBlank(password)) {
                return new JSONResult(false, "密码不能为空!");
            }
            User user = userService.selectByUserName(username);
            if (user != null) {
                return new JSONResult(false, "用户已存在!");
            }
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncode(username, password));
            userService.insert(user);
            return new JSONResult(user, "用户注册成功!", true);
        } catch (Exception e) {
            log.error("用户注册异常", e);
            return new JSONResult(false, "用户注册异常:" + e.getMessage());
        }
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public JSONResult login (@RequestParam(name = "username", required = false) String username,
                             @RequestParam(name = "password", required = false) String password,
                             HttpServletRequest req) {
        try {
            if (StringUtils.isBlank(username)) {
                return new JSONResult(false, "用户名不能为空!");
            }
            if (StringUtils.isBlank(password)) {
                return new JSONResult(false, "密码不能为空!");
            }
            User user = userService.selectByUserName(username);
            if (user == null) {
                return new JSONResult(false, "用户不存在!");
            }
            if  (!user.getPassword().equals(passwordEncode(username, password))) {
                return new JSONResult(false, "密码错误!");
            }
            HttpSession session = req.getSession();
            user.setPassword(null);
            session.setAttribute("user", user);
            return new JSONResult(true, "登录成功!");
        } catch (Exception e) {
            log.error("用户登录异常", e);
            return new JSONResult(false, "用户登录异常:" + e.getMessage());
        }
    }

    /**
     * 退出登录
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/logout", method = RequestMethod.POST)
    public JSONResult login (HttpServletRequest req) {
        try {
            SessionUtil.clearSession(req);
            return new JSONResult(true, "退出登录成功!");
        } catch (Exception e) {
            log.error("退出登录异常", e);
            return new JSONResult(false, "退出登录异常:" + e.getMessage());
        }
    }

    private String passwordEncode (String username, String password) {
        password = StrUtils.md5(username + Constants.MD5_SALT + password);
        return password;
    }
}
