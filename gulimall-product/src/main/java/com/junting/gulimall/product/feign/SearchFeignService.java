package com.junting.gulimall.product.feign;

import com.junting.common.to.es.SkuEsModel;
import com.junting.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-09 1:40
 */

@FeignClient("gulimall-search")
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
