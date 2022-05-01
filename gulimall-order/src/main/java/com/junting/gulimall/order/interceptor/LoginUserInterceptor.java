package com.junting.gulimall.order.interceptor;

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
        // 这个请求直接放行
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match1 = antPathMatcher.match("/order/order/status/**", uri);
        boolean match2 = antPathMatcher.match("/payed/notify", uri);
//        boolean match = new AntPathMatcher().match("/order/order/status/**", uri);
        if(match1 || match2){
            return true;
        }
        // 获取loginUser对应的用户value，没有也不去登录了。登录逻辑放到别的代码里，需要登录时再重定向
        HttpSession session = request.getSession();
        MemberRespVo user = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (user == null){
            // 没登陆就去登录
            session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }else{
            threadLocal.set(user);

            return true;
        }
    }
}
