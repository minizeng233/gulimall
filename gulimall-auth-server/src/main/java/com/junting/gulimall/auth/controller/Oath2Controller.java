package com.junting.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.junting.common.constant.AuthServerConstant;
import com.junting.common.utils.HttpUtils;
import com.junting.common.utils.R;
import com.junting.common.vo.MemberRespVo;
import com.junting.gulimall.auth.feign.MemberFeignService;
import com.junting.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mini_zeng
 * @create 2022-01-14 13:46
 */
@Controller
@Slf4j
@RequestMapping("/oauth2.0")
public class Oath2Controller {
    @Autowired
    MemberFeignService memberFeignService;
    /**
     * 登录成功回调
     * {
     *     "access_token": "2.00b5w4HGbwxc6B0e3d62c666DlN1DD",
     *     "remind_in": "157679999",
     *     "expires_in": 157679999,
     *     "uid": "5605937365",
     *     "isRealName": "true"
     * }
     * 	汀西氟的我是你	---		2.00b5w4HGbwxc6B0e3d62c666DlN1DD
     */
    @GetMapping("/gitee/success") // Oath2Controller
    public String weiBo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {

        // 根据code换取 Access Token
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "78eaef65c3d4e9cf126bcdb291b3c8d6fd1d01bbbde3992812494a476ccc189a");
        map.put("client_secret", "bb30904c7e2f18eec2ed8c8e29f7070374bd09c8bd1b63972e523337b6be5c75");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/gitee/success");
        map.put("code", code);
        Map<String, String> headers = new HashMap<>();
        // 去获取token
        HttpResponse response = HttpUtils.doPost("https://gitee.com",
                "/oauth/token", "post", headers, null, map);
        if(response.getStatusLine().getStatusCode() == 200){
            // 获取响应体： Access Token
            String json = EntityUtils.toString(response.getEntity());
            //TODO 这时候还没有uid
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
//            Map<String, String> accessMap = new HashMap<>();
//            accessMap.put("access_token",socialUser.getAccessToken());
//            HttpResponse responseWithUid = HttpUtils.doGet("https://gitee.com", "/api/v5/user", "get", null, accessMap);
            // 相当于我们知道了当前是那个用户
            // 1.如果用户是第一次进来 自动注册进来(为当前社交用户生成一个会员信息 以后这个账户就会关联这个账号)
            R login = memberFeignService.login(socialUser);
            if(login.getCode() == 0){
                MemberRespVo respVo = login.getData("data" ,new TypeReference<MemberRespVo>() {});

                log.info("\n欢迎 [" + respVo.getUsername() + "] 使用社交账号登录");
                // 第一次使用session 命令浏览器保存这个用户信息 JESSIONSEID 每次只要访问这个网站就会带上这个cookie
                // 在发卡的时候扩大session作用域 (指定域名为父域名)
                // TODO 1.默认发的当前域的session (需要解决子域session共享问题)

//				session.setAttribute("logUser",respVo);
//				 Cookie cookie = new Cookie("JSESSIONID","123");
//				 cookie.setDomain("gulimall.com");
//				servletResponse.addCookie(cookie);

                // TODO 2.使用JSON序列化后保存到redis  自动完成
                session.setAttribute(AuthServerConstant.LOGIN_USER, respVo);
                // 登录成功 跳回首页
                return "redirect:http://gulimall.com";
            }else{
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else{
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
