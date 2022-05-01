package com.junting.gulimall.auth.vo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

/**
 * @author mini_zeng
 * @create 2022-01-13 16:08
 */
@Data
@Slf4j
public class UserRegisterVo {
    // JSR303校验
    @Length(min = 6,max = 20,message = "用户名长度必须在6-20之间")
    private String userName;

    @Length(min = 6,max = 20,message = "密码长度必须在6-20之间")
    private String password;

    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
