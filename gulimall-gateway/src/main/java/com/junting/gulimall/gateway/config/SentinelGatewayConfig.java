package com.junting.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.junting.common.exception.BizCodeEnum;
import com.junting.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author mini_zeng
 * @create 2022-01-22 11:35
 */
@Configuration
public class SentinelGatewayConfig {

    //TODO 响应式编程学习，天然支持高并发
    //GatewayCallbackManager
    public SentinelGatewayConfig(){
        GatewayCallbackManager.setBlockHandler(new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
                R error = R.error(BizCodeEnum.TO_MANY_REQUEST.getCode(), BizCodeEnum.TO_MANY_REQUEST.getMsg());
                String errJson = JSON.toJSONString(error);
//                Mono<String> aaa = Mono.just("aaa");
                Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(errJson), String.class);
                return body;
            }
        });
    }
}
