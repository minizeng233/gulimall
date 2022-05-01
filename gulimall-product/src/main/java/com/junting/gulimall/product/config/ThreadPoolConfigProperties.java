package com.junting.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author mini_zeng
 * @create 2022-01-13 11:48
 */

@Component
@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize;

    private Integer maxSize;

    private Integer keepAliveTime;
}
