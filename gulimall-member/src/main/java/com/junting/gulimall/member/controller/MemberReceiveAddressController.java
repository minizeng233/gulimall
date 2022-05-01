package com.junting.gulimall.member.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.junting.gulimall.member.entity.MemberReceiveAddressEntity;
import com.junting.gulimall.member.service.MemberReceiveAddressService;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.R;



/**
 * 会员收货地址
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 10:08:53
 */
@RestController
@RequestMapping("member/memberreceiveaddress")
public class MemberReceiveAddressController {
    @Autowired
    private MemberReceiveAddressService memberReceiveAddressService;

    /**
        *@Description  查询用户地址
        *@author mini_zeng
        *@Date 2022/1/16
        *@Param memberId
        *@return List<MemberAddressVo>
        **/
    @GetMapping("/{memberId}/addresses")
    List<MemberReceiveAddressEntity> getAddress(@PathVariable("memberId") Long memberId){
        return memberReceiveAddressService.getAddress(memberId);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    ////@RequiresPermissions("member:memberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    ////@RequiresPermissions("member:memberreceiveaddress:info")
    public R info(@PathVariable("id") Long id){
		MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);

        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    ////@RequiresPermissions("member:memberreceiveaddress:save")
    public R save(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.save(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    ////@RequiresPermissions("member:memberreceiveaddress:update")
    public R update(@RequestBody MemberReceiveAddressEntity memberReceiveAddress){
		memberReceiveAddressService.updateById(memberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    ////@RequiresPermissions("member:memberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids){
		memberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
