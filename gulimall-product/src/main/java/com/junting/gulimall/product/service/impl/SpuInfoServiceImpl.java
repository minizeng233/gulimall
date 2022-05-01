package com.junting.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.junting.common.constant.ProductConstant;
import com.junting.common.to.SkuHasStockVo;
import com.junting.common.to.SkuReductionTo;
import com.junting.common.to.SpuBoundTo;
import com.junting.common.to.es.SkuEsModel;
import com.junting.common.utils.R;
import com.junting.gulimall.product.entity.*;
import com.junting.gulimall.product.feign.CouponFeignService;
import com.junting.gulimall.product.feign.SearchFeignService;
import com.junting.gulimall.product.feign.WareFeignService;
import com.junting.gulimall.product.service.*;
import com.junting.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrService attrService;
    @Autowired
    ProductAttrValueService attrValueService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void spuInfoEntity(SpuSaveVo vo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.save(infoEntity);

        //2、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(infoEntity.getId(), images);

        //3、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(infoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
//        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        spuInfoDescService.save(spuInfoDescEntity);

        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
//        attrValueService.saveProductAttr(collect);
        attrValueService.saveBatch(collect);

        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程调用spu积分信息失败");
        }

        //6、保存当前spu对应的所有sku信息；
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
//                skuInfoService.saveSkuInfo(skuInfoEntity);
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image
                skuImagesService.saveBatch(imagesEntities);
                //TODO 没有图片路径的无需保存

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);
                    return attrValueEntity;
                }).collect(Collectors.toList());

                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void spuUp(Long spuId) {

        //TODO 4、根据spu查出当前sku的所有可以被用来检索的规格属性
        //根据pms_product_attr_value查询spuId对应的所有attrValueEntities
        List<ProductAttrValueEntity> attrValueEntities = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        //根据attrId筛选出符合search的attrvalue的ids集合
        List<Long> attr_ids = attrValueEntities.stream().map(attrValueEntitie -> {
            Long attrId = attrValueEntitie.getAttrId();
            AttrEntity attrEntity = attrService.getOne(new QueryWrapper<AttrEntity>().eq("attr_id", attrId));
            return attrEntity;
        }).filter(attrEntity -> {
            return attrEntity.getSearchType() == 1;
        }).map(attrEntity -> {
            return attrEntity.getAttrId();
        }).collect(Collectors.toList());

        //set的contains判断比List快很多，List加索引检索快
        Set<Long> ids = new HashSet<>(attr_ids);

        //重新过滤attrValueEntities得符合ids的attrvalue的集合并为SkuEsModel.Attr attr赋值
        List<SkuEsModel.Attr> attrs = attrValueEntities.stream().filter(attrValueEntitie -> {
            return ids.contains(attrValueEntitie.getAttrId());
        }).map(attrValueEntitie -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            BeanUtils.copyProperties(attrValueEntitie, attr);
            return attr;
        }).collect(Collectors.toList());

        //TODO 1、发送远程调用，库存系统查询是否有库存
        List<Long> skuIds = skuInfoService.getSkusBySpuId(spuId).stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = new HashMap<>();
        try {
            R r = wareFeignService.getSkuHasStock(skuIds);
            List<SkuHasStockVo> data = r.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            stockMap = r.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));

        }catch (Exception e){
            log.error("仓库服务远程调用发生异常，异常原因{}",e);
        }

        //通过spuId查询所有需要上架的sku的信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        //封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModels = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            //es中名字与skuinfoentity中属性名字不匹配的单独赋值
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            //TODO 2、热度评分。0
            skuEsModel.setHotScore(0L);
            //TODO 3、查询品牌和分类的名字信息 ,es中skuinfo没有的属性
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            //设置可搜索属性
            skuEsModel.setAttrs(attrs);

            //设置是否有库存
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            return skuEsModel;
        }).collect(Collectors.toList());


        //TODO 5、将数据发给es进行保存：gulimall-search
        R r1 = searchFeignService.productStatusUp(skuEsModels);
        System.out.println(r1);
        if (r1.getCode()==0){
            this.baseMapper.upSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            log.error("商品远程es保存失败");
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        return getById(skuInfoService.getById(skuId).getSpuId());
    }
}