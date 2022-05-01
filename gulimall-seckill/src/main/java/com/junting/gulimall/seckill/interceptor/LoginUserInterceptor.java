package com.junting.gulimall.seckill.interceptor;

import com.junting.common.constant.AuthServerConstant;
import com.junting.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>Title: CartInterceptor</p>
 * Description：在执行目标之前 判断用户是否登录,并封装
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        // 这个请求直接拦截
        //TODO 没有排查出查询不到的原因，先全部放行（user为空）
        //TODO 开启共享session的注解是@EnableRedisHttpSession而不是@EnableRedissonHttpSession!!!!!
        boolean match = new AntPathMatcher().match("/kill", uri);
        if(match){
            HttpSession session = request.getSession();
            MemberRespVo user = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
            if(user != null){
                threadLocal.set(user);
                return true;
            }else{
                 //没登陆就去登录
                session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
                response.sendRedirect("http://auth.gulimall.com/login.html");
                return false;
//                threadLocal.set(user);
//                return true;
            }
        }
        return true;
    }
}
