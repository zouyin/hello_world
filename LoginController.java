package com.qf.shop_sso.controller;

import com.google.gson.Gson;
import com.qf.zag.shop.entity.User;
import com.qf.zag.shop.service.IUserService;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.HttpResource;

import javax.jws.WebParam;
import javax.rmi.CORBA.Util;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/sso")
public class LoginController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/tologin")
    public  String toLogin (){
        return "login";
    }

    @RequestMapping("/login")
    public String login(User user, HttpServletResponse response){
        System.out.println(user.toString());

        user= userService.queryUserByName(user.getUsername(), user.getPassword());
     if(user!=null ){
       String token = UUID.randomUUID().toString();
       /*user存到redis中*/
         redisTemplate.opsForValue().set(token, user);
         redisTemplate.expire(token,30, TimeUnit.DAYS);
         /*user 存到cookie中*/
         Cookie cookie = new Cookie("login_token",token);
         cookie.setMaxAge(60 * 60 * 24 * 30);
         cookie.setPath("/");
         response.addCookie(cookie);

         return "redirect:http://localhost:8081";
        }

        return "redirect:/sso/tologin";
    }


    @RequestMapping("/islogin")
    @ResponseBody
    public String  idlogin(@CookieValue(value = "login_token" ,required = false ) String token, Model model) {
        System.out.println("welcome");
        String userStr=null;
        if(token != null) {
            User user = (User) redisTemplate.opsForValue().get(token);
            System.out.println(user.toString());
           userStr = new Gson().toJson(user);
        }
        return "islogin(" + userStr + ")";

    }

    @RequestMapping("/logout")
    public String loginout(@CookieValue(value = "login_token",required = false) String token, HttpServletResponse response){
        System.out.println("logout");
        if(token !=null) {
            redisTemplate.delete(token);
            Cookie cookie = new Cookie("login_token", null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

        }
        return "redirect:/sso/tologin";



    }



}
