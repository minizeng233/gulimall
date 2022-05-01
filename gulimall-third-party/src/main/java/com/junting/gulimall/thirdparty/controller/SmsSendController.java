package com.junting.gulimall.thirdparty.controller;

import com.junting.common.exception.BizCodeEnum;
import com.junting.common.utils.R;
import com.junting.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author mini_zeng
 * @create 2022-01-13 15:08
 */
@Controller
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    private SmsComponent smsComponent;

    /*** 提供给别的服务进行调用的
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
        if(!"fail".equals(smsComponent.sendSmsCode(phone, code).split("_")[0])){
            return R.ok();
        }
//        return R.error(BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getMsg());
        return null;
    }

}
