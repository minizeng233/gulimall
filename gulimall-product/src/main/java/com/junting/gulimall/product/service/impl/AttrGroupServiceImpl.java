package com.junting.gulimall.product.service.impl;

import com.junting.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.junting.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.junting.gulimall.product.entity.AttrEntity;
import com.junting.gulimall.product.service.AttrAttrgroupRelationService;
import com.junting.gulimall.product.service.AttrService;
import com.junting.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.junting.gulimall.product.vo.SpuItemAttrGroup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.product.dao.AttrGroupDao;
import com.junting.gulimall.product.entity.AttrGroupEntity;
import com.junting.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        // key不为空
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) ->
                    obj.eq("attr_group_id", key).or().like("attr_group_name", key)
            );
        }
        if (catelogId == 0) {
            // Query可以把map封装为IPage
            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params),
                            wrapper);
            return new PageUtils(page);
        } else {
            // 增加id信息
            wrapper.eq("catelog_id", catelogId);

            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params),
                            wrapper);
            return new PageUtils(page);
        }

    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupByCategoryId(Long catelogId) {
        //  获取catelogid下的所有分组
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //  获取分组下的所有属性封装AttrGroupWith
        List<AttrGroupWithAttrsVo> collect = groupEntities.stream().map(group -> {
            List<AttrEntity> attr = attrService.getRelationAttr(group.getAttrGroupId());
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group,attrGroupWithAttrsVo);
            attrGroupWithAttrsVo.setAttrs(attr);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1.出当前Spu对应的所有属性的分组信息 以及当前分组下所有属性对应的值
        // 1.1 查询所有分组
        AttrGroupDao baseMapper = this.getBaseMapper();

        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);

    }
}