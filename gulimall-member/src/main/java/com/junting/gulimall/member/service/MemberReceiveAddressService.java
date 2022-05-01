package com.junting.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.junting.common.utils.PageUtils;
import com.junting.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 10:08:53
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
        *@Description   查询用户地址
        *@author mini_zeng
        *@Date 2022/1/16
        *@Param memberId
        *@return java.util.List<com.junting.gulimall.member.entity.MemberReceiveAddressEntity>
        **/
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

