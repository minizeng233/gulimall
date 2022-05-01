package com.junting.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.junting.common.constant.AuthServerConstant;
import com.junting.common.exception.BizCodeEnum;
import com.junting.common.utils.R;
import com.junting.common.vo.MemberRespVo;
import com.junting.gulimall.auth.feign.MemberFeignService;
import com.junting.gulimall.auth.feign.ThirdPartFeignService;
import com.junting.gulimall.auth.vo.UserLoginVo;
import com.junting.gulimall.auth.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mini_zeng
 * @create 2022-01-13 13:39
 */
@Slf4j
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){
        //  TODO 接口防刷(冷却时长递增)，redis缓存 sms:code:电话号
        //先从redis中查询是否60s内发送
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if(null != redisCode && redisCode.length() > 0){
            String[] s = redisCode.split("_");
            long CuuTime = Long.parseLong(s[1]);
            long curTime = System.currentTimeMillis();
            if ((curTime - CuuTime )< 60000){
                //验证码发送频繁错误
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //发送给用户的
        String code = UUID.randomUUID().toString().substring(0, 6);
        //保存进数据库的
        String redis_code = code + "_" + System.currentTimeMillis();
        //将验证码缓存进redis
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,redis_code,10L, TimeUnit.MINUTES);
        Map<String, String> codeNum = null;
        try {// 调用第三方短信服务
//            return thirdPartFeignService.sendCode(phone, code);
            codeNum = send(code);
        } catch (Exception e) {
            log.warn("远程调用不知名错误 [无需解决]");
        }
        return R.ok().put("codeNum",codeNum);
    }

    private Map<String, String> send(String code){
        //在验证码框内直接返回数据
        HashMap<String, String> map = new HashMap<>();
        //code为上步获取的UUID验证码，发给用户
        map.put("codeNum",code);

        return map;
    }

    /**
     * TODO 重定向携带数据,利用session原理 将数据放在sessoin中 取一次之后删掉
     *
     * TODO 1. 分布式下的session问题
     * 校验
     * RedirectAttributes redirectAttributes ： 模拟重定向带上数据
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo,
                           BindingResult result,
                            RedirectAttributes redirectAttributes){
        if (result.hasErrors()){
            //TODO 提交空值时会报错，因为不能确定getDefaultMessage的类型？？？
            // 将错误属性与错误信息一一封装
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, fieldError -> {
                        String defaultMessage = fieldError.getDefaultMessage();
                        return defaultMessage;
                    }));
            // addFlashAttribute 这个数据只取一次
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 开始注册 调用远程服务
        // 1.校验验证码
        String code = userRegisterVo.getCode();
        String redis_code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
        if(!StringUtils.isEmpty(redis_code)){
            // 验证码通过
            if(code.equals(redis_code.split("_")[0])){
                // 删除验证码 先不删除，保存着用
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());
                // 调用远程服务进行注册
                R r = memberFeignService.register(userRegisterVo);
                if(r.getCode() == 0){
                    // 注册成功，去登录
                    return "redirect:http://auth.gulimall.com/login.html";
                }else{
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    // 数据只需要取一次
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else{
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                // addFlashAttribute 这个数据只取一次
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            // addFlashAttribute 这个数据只取一次
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    //需要判断是否已经登录
    @GetMapping({"/login.html","/","/index","/index.html"}) // auth
    public String loginPage(HttpSession session){
        // 从会话从获取loginUser
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);// "loginUser";
        System.out.println("attribute:"+attribute);
        if(attribute == null){
            return "login";
        }
        System.out.println("已登陆过，重定向到首页");
        return "redirect:http://gulimall.com";
    }


    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, // from表单里带过来的
                        RedirectAttributes redirectAttributes,
                        HttpSession session){
         //远程登录
        R r = memberFeignService.login(userLoginVo);
        if(r.getCode() == 0){
            // 登录成功
            MemberRespVo respVo = r.getData("data", new TypeReference<MemberRespVo>() {});
            // 放入session
            session.setAttribute(AuthServerConstant.LOGIN_USER, respVo);//loginUser
            log.info("\n欢迎 [" + respVo.getUsername() + "] 登录");
            return "redirect:http://gulimall.com";
        }else {
            HashMap<String, String> error = new HashMap<>();
            // 获取错误信息
            error.put("msg", r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
