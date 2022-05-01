package com.junting.gulimall.auth.feign;

import com.junting.common.utils.R;
import com.junting.gulimall.auth.vo.SocialUser;
import com.junting.gulimall.auth.vo.UserLoginVo;
import com.junting.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author mini_zeng
 * @create 2022-01-13 20:15
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R login(@RequestBody SocialUser socialUser);

}
