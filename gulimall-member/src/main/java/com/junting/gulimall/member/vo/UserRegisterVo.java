package com.junting.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

/**
 * @author mini_zeng
 * @create 2022-01-13 18:58
 */
@Data
public class UserRegisterVo {

    private String userName;

    private String password;

    private String phone;
}
