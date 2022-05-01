package com.junting.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.junting.gulimall.product.service.CategoryBrandRelationService;
import com.junting.gulimall.product.vo.Catelog2Vo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.junting.common.utils.PageUtils;
import com.junting.common.utils.Query;

import com.junting.gulimall.product.dao.CategoryDao;
import com.junting.gulimall.product.entity.CategoryEntity;
import com.junting.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Cacheable(value = {"category"},key = "#root.methodName",sync = true)
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
//         2 组装成父子的树形结构
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() != null && categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return level1Menus;
    }

    @Cacheable(value = {"category"},key = "#root.methodName")
    @CacheEvict(value = {"category"},allEntries = true)
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1 检查当前的菜单是否被别的地方所引用
        //
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        paths = findParentPath(catelogId, paths);
        Collections.reverse(paths);
        return paths.toArray(new Long[paths.size()]);
    }

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Transactional
    @Override
    public void updateByReation(CategoryEntity category) {
        this.updateById(category);
        String categoryName = category.getName();
        Long catelogId = category.getCatId();
        if (!StringUtils.isEmpty(categoryName)) {
            categoryBrandRelationService.updateByCategoryName(catelogId, categoryName);
        }
    }

    @Cacheable(value = {"category"},key = "#root.methodName",sync = true)
    @Override
    public List<CategoryEntity> getLevel1() {
        System.out.println("Level1方法调用了");
        return this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    //TODO 大并发产生io.netty.util.internal.OutOfDirectMemoryError对外溢出异常
//    产生原因：
//1)、springboot2.0以后默认使用lettuce作为操作redis的客户端，它使用netty进行网络通信
//2)、lettuce的bug导致netty堆外内存溢出。netty如果没有指定堆外内存，默认使
//    用Xms的值，可以使用-Dio.netty.maxDirectMemory进行设置
//    解决方案：由于是lettuce的bug造成，不要直接使用-Dio.netty.maxDirectMemory
//    去调大虚拟机堆外内存，治标不治本。
//    1)、升级lettuce客户端。但是没有解决的
//    2)、切换使用jedis

    public Map<String, List<Catelog2Vo>> getCatelogJson1() {
        //1.看缓存中有没有
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String catalogJson = ops.get("catalogJson");
        //2.没有先从数据库获取再放到缓存
        if (StringUtils.isEmpty(catalogJson)) {
            System.out.println("缓存没有命中，去抢lock");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatalogJsonDbWithRedissonLock();
            String s = JSON.toJSONString(catelogJsonFromDb);
            ops.set("catalogJson", s);
            return catelogJsonFromDb;
        }
        System.out.println("缓存命中了，直接返回");
        //3.缓存中的获取也要将JSON字符串转换为对象，JSON字符串各平台各语言通用的
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return stringListMap;
    }

    @Cacheable(value = "category" , key = "#root.methodName" ,sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        //查询所有CategoryEntity
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //查询一级分类
        List<CategoryEntity> level1 = getChildrenCategory(categoryEntities, 0L);

        Map<String, List<Catelog2Vo>> collect = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //所有二级分类
            List<CategoryEntity> level2 = getChildrenCategory(categoryEntities, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (level2 != null) {
                catelog2Vos = level2.stream().map(L2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), L2.getCatId().toString(), L2.getName(), null);
                    List<CategoryEntity> catelogLevel3 = getChildrenCategory(categoryEntities, L2.getCatId());
                    if (catelogLevel3 != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = catelogLevel3.stream().map(L3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(L3.getCatId().toString(), L3.getName(), L2.getCatId().toString());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return collect;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonDbWithRedissonLock() {
        RLock lock = redisson.getLock("CatalogJson-Lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> categoriesDb = null;
        try {
            categoriesDb = getCatelogJsonFromDb();
        } finally {
            lock.unlock();
            return categoriesDb;
        }
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonDbWithRedisLock() {
        //加锁和解锁要保证原子性
        String uuid = UUID.randomUUID().toString();
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        Boolean lock = ops.setIfAbsent("lock", uuid, 500, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("抢到了lock");
            String lockValue = ops.get("lock");
            Map<String, List<Catelog2Vo>> categoriesDb = null;
            try {
                categoriesDb = getCatelogJsonFromDb();
            } finally {
                // get和delete原子操作
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                redisTemplate.execute(
                        new DefaultRedisScript<Long>(script, Long.class), // 脚本和返回类型
                        Arrays.asList("lock"), // 参数
                        lockValue); // 参数值，锁的值
                return categoriesDb;
            }
        } else {
            System.out.println("没有抢到lock");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 睡眠0.1s后，重新调用 //自旋
            return getCatalogJsonDbWithRedisLock();
        }
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {
        //1.看缓存中有没有，进来也要先找一次redis，不然会执行两次
        //查到后要在这里先return进redis中再结束方法
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String catalogJson = ops.get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            //如果不为空，直接从redis返回
            Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return stringListMap;
        }
        System.out.println("数据库被调用");
        //查询所有CategoryEntity
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //查询一级分类
        List<CategoryEntity> level1 = getChildrenCategory(categoryEntities, 0L);

        Map<String, List<Catelog2Vo>> collect = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //所有二级分类
            List<CategoryEntity> level2 = getChildrenCategory(categoryEntities, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (level2 != null) {
                catelog2Vos = level2.stream().map(L2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), L2.getCatId().toString(), L2.getName(), null);
                    List<CategoryEntity> catelogLevel3 = getChildrenCategory(categoryEntities, L2.getCatId());
                    if (catelogLevel3 != null) {
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = catelogLevel3.stream().map(L3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(L3.getCatId().toString(), L3.getName(), L2.getCatId().toString());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        String s = JSON.toJSONString(collect);
        ops.set("catalogJson", s);
        return collect;
    }

    private List<CategoryEntity> getChildrenCategory(List<CategoryEntity> list, Long catId) {
//        List<CategoryEntity> catelogLevel3 = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", l2.getCatId()));
//        return catelogLevel3;
        List<CategoryEntity> collect = list.stream().filter(item -> item.getParentCid() == catId)
                .collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != null && categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() != null && categoryEntity.getParentCid() == root.getCatId()
        ).map(categoryEntity -> {
            // 1 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 2 菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }
}