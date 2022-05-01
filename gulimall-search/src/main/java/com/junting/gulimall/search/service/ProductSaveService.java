package com.junting.gulimall.search.service;

import com.junting.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-08 23:28
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
