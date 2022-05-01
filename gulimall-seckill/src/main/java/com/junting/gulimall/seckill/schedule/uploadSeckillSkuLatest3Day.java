package com.junting.gulimall.seckill.schedule;

import com.junting.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author mini_zeng
 * @create 2022-01-20 17:11
 */

@Slf4j
@Service
public class uploadSeckillSkuLatest3Day {
    private final String upload_lock = "seckill:upload:lock";

    @Autowired
    private SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;
    /**
     * 这里应该是幂等的
     *  三秒执行一次：* /3 * * * * ?
     *  8小时执行一次：0 0 0-8 * * ?
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Day(){
        log.info("\n上架秒杀商品的信息");
        // 1.重复上架无需处理 加上分布式锁 状态已经更新 释放锁以后其他人才获取到最新状态
        RLock lock = redissonClient.getLock(upload_lock);// "seckill:upload:lock";
        try {
            lock.lock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillSkuLatest3Day();
        } finally {
            lock.unlock();
        }
    }
}
