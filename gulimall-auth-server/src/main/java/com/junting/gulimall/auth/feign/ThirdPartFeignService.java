package com.junting.gulimall.auth.feign;

import com.junting.common.utils.R;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mini_zeng
 * @create 2022-01-13 15:18
 */

@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {
    
    /**
        *@Description   发送验证码
        *@author mini_zeng
        *@Date 2022/1/13
        *@Param phone
    code
        *@return com.junting.common.utils.R
        **/
    @GetMapping("/sms/sendcode")
    R sendCode(@RequestParam(value = "phone",required = false) String phone, @RequestParam(value = "code",required = false) String code);
}
