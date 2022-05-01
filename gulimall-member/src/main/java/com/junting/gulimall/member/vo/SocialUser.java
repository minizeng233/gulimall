package com.junting.gulimall.member.vo;

import lombok.Data;

/**
 * @author mini_zeng
 * @create 2022-01-14 14:40
 */
@Data
public class SocialUser {

    private String accessToken;

    private String remindIn;

    private Long expiresIn;

    private String uid;

    private String isrealname;
}