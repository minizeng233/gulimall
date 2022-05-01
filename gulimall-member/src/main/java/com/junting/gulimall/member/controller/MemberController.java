package com.junting.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.junting.common.exception.BizCodeEnum;
import com.junting.gulimall.member.exception.PhoneExistException;
import com.junting.gulimall.member.exception.UserNameExistException;
import com.junting.gulimall.member.vo.MemberLoginVo;
import com.junting.gulimall.member.vo.SocialUser;
import com.junting.gulimall.member.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.junting.gulimall.member.entity.MemberEntity;
import com.junting.gulimall.member.service.MemberService;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.R;



/**
 * 会员
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 10:08:53
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/oauth2/login")
    public R login(@RequestBody SocialUser socialUser){

        MemberEntity memberEntity = memberService.login(socialUser);
        if(memberEntity != null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnum.SOCIALUSER_LOGIN_ERROR.getCode(), BizCodeEnum.SOCIALUSER_LOGIN_ERROR.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){

        MemberEntity memberEntity = memberService.login(vo);
        if(memberEntity != null){
            return R.ok().setData(memberEntity);
        }else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getCode(), BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    ////@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/register")
    public R register(@RequestBody UserRegisterVo userRegisterVo){

        try {
            memberService.register(userRegisterVo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXISTS_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXISTS_EXCEPTION.getMsg());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXISTS_EXCEPTION.getCode(), BizCodeEnum.USER_EXISTS_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    ////@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    ////@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    ////@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    ////@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
