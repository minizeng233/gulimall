package com.junting.gulimall.member.dao;

import com.junting.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author junting
 * @email junting@gmail.com
 * @date 2022-01-06 10:08:53
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
