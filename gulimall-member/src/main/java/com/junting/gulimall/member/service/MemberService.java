package com.junting.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.member.entity.MemberEntity;
import com.junting.gulimall.member.exception.PhoneExistException;
import com.junting.gulimall.member.exception.UserNameExistException;
import com.junting.gulimall.member.vo.MemberLoginVo;
import com.junting.gulimall.member.vo.SocialUser;
import com.junting.gulimall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 10:08:53
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo userRegisterVo);
    void checkPhone(String phone) throws PhoneExistException;

    void checkUserName(String username) throws UserNameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser);
}

