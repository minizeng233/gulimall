package com.junting.gulimall.coupon.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.junting.common.exception.BizCodeEnum;
import com.junting.common.utils.R;
import org.springframework.context.annotation.Configuration;

/**
 * @author mini_zeng
 * @create 2022-01-21 19:23
 */
@Configuration
public class SentinelConfig {

    public SentinelConfig(){
        WebCallbackManager.setUrlBlockHandler((request, response, exception) -> {
            R error = R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(JSON.toJSONString(error));
        });
    }
}
