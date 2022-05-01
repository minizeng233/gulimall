package com.junting.gulimall.search.service;

import com.junting.gulimall.search.vo.SearchParam;
import com.junting.gulimall.search.vo.SearchResult;

/**
 * @author mini_zeng
 * @create 2022-01-11 10:52
 */

public interface MallSearchService {
    /**
        *@Description
        *@author mini_zeng
        *@Date 2022/1/11
        *@Param param   检索的所有参数
        *@return java.lang.Object 检索返回的结果
        **/
    SearchResult search(SearchParam param);
}
