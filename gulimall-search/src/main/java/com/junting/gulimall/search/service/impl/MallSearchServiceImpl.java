package com.junting.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.junting.common.to.es.SkuEsModel;
import com.junting.gulimall.search.config.GulimallEsConfig;
import com.junting.gulimall.search.constant.EsConstant;
import com.junting.gulimall.search.service.MallSearchService;
import com.junting.gulimall.search.vo.SearchParam;
import com.junting.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.management.remote.rmi._RMIConnection_Stub;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mini_zeng
 * @create 2022-01-11 10:53
 */

@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResult search(SearchParam param) {
        //获取request请求
        SearchRequest request = buildSearchRequest(param);
        SearchResult result = null;

        try {
            //执行ES的search方法返回结果
            SearchResponse response = restHighLevelClient.search(request, GulimallEsConfig.COMMON_OPTIONS);
            //处理返回结果封装并返回页面
            result = buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @return org.elasticsearch.action.search.SearchRequest
     * @Description 返回构造好的SearchRequest对象
     * @author mini_zeng
     * @Date 2022/1/11
     * @Param 全文检索：skuTitle-》keyword
     * 排序：saleCount（销量）、hotScore（热度分）、skuPrice（价格）
     * 过滤：hasStock、skuPrice区间、brandId、catalog3Id、attrs
     * 聚合：attrs
     **/
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /**
         * 模糊匹配，过滤（按照属性、分类、品牌、价格区间，库存）
         */
        //1.构建bool-query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1 must-查询模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2 filter-catalogId
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        //1.2.2 按照品牌ID查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //nested检索方法
        //1.2.3 按照所有指定的属性进行查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : param.getAttrs()) {
                //attrs=1_5寸:8寸&attrs=2_16g:8g
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                String[] split = attr.split("_");
                String attrId = split[0];
                //检索的属性值
                String[] attrValue = split[1].split(":");
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                //每一个必须都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        //1.2.4 按照是否有库存查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        //1.2.5 按照价格区间查询
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder rangeBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                rangeBuilder.from(s[0]).to(s[1]);
            } else {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeBuilder.to(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeBuilder.from(s[0]);
                }
            }
            boolQuery.filter(rangeBuilder);
        }
        //封装查询条件
        sourceBuilder.query(boolQuery);

        /**
         * 排序、分页、高亮
         */
        //2.1 排序
        if (!StringUtils.isEmpty(param.getSort())){
            //sort=hotScore_asc/desc
            String sort = param.getSort();
            String[] split = sort.split("_");
            String name = split[0];
            SortOrder order = split[1].equalsIgnoreCase("asc")? SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(name,order);
        }
        //2.2 分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //3.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        /**
         * 聚合分析
         */
        // 品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);
        //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);
        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出当前attrId对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //聚合分析出当前attrId对应所有可能的属性值attrValue
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue"));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("构建出的DSL语句为" + s);

        SearchRequest request = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return request;
    }

    /**
     * @return com.junting.gulimall.search.vo.SearchResult
     * @Description 将ES返回的response结果处理返回SearchResult返回页面
     * @author mini_zeng
     * @Date 2022/1/11
     * @Param response
     **/
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {
        SearchResult result = new SearchResult();
        //1、返回所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> list = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length>0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString,SkuEsModel.class);
                //高亮标题处理
                if (!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                list.add(esModel);
            }
        }
        result.setProducts(list);

        //2、当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            //得到属性ID
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            //得到属性名
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);
            //得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        //3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brands = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> brandAggBuckets = brand_agg.getBuckets();
        for (Terms.Bucket bucket : brandAggBuckets) {
            //获取品牌信息
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //获取品牌的ID
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);
            //获取品牌名字
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            //获取品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brands.add(brandVo);
        }
        result.setBrands(brands);

        //4、当前所有商品涉及到的所有分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //获取品牌ID
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //得到品牌名字
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //5、分页信息-页码
        result.setPageNum(param.getPageNum());
        //5、分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //5、分页信息-总页码
        int totalPages = total%EsConstant.PRODUCT_PAGESIZE == 0 ? (int)total/EsConstant.PRODUCT_PAGESIZE : (int)total/EsConstant.PRODUCT_PAGESIZE+1;
        result.setTotalPages(totalPages);
        //遍历导航页
        List<Integer> navs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++){
            navs.add(i);
        }
        result.setPageNavs(navs);

        //面包屑导航结果封装
        if (param.getAttrs() !=null && param.getAttrs().size()>0) {
            List<SearchResult.NavVo> navVos = result.getNavs();
            List<Long> attrIds = result.getAttrIds();
            navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                String navVoId = s[0];
                String navVoValue = s[1];
                navVo.setNavValue(navVoValue);
                for (SearchResult.AttrVo attrVo : attrVos) {
                    if (attrVo.getAttrId() == Long.parseLong(navVoId)) {
                        navVo.setNavName(attrVo.getAttrName());
                        attrIds.add(attrVo.getAttrId());
                        break;
                    }
                }
                System.out.println(attr);
                String link = replaceQueryString(param, attr, "attrs");
                navVo.setLink(link);
                return navVo;
            }).collect(Collectors.toList());
            result.setAttrIds(attrIds);
            result.setNavs(navVos);
        }

        //品牌相关
        if (param.getBrandId() != null && param.getBrandId().size() >0){
            List<SearchResult.NavVo> navVos = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //远程查询所有品牌
            StringBuffer buffer = new StringBuffer();
            String replace = "";
            String link = "";
            for (SearchResult.BrandVo brandVo : brands) {
                buffer.append(brandVo.getBrandName()+";");
                replace = brandVo.getBrandId()+"";
                link = replaceQueryString(param, replace, "brandId");
            }
            navVo.setNavValue(buffer.toString());
            navVo.setLink(link);
            navVos.add(navVo);
            result.setNavs(navVos);
        }

        return result;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20");
            encode = encode.replace("%28","(");//浏览器和java对+号的差异化处理
            encode = encode.replace("%29",")");//浏览器和java对+号的差异化处理
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = param.get_queryString();
        String replace = null;
        String source = null;
        if (!url.contains("&")){
            replace = url.replace(key+"=" + encode, "");
            source = "http://search.gulimall.com/list.html";
        }else{
            replace = url.replace("&"+key+"=" + encode, "").replace(key+"=" + encode, "");
            source = "http://search.gulimall.com/list.html?";
        }
        String link = source + replace;
        return link;
    }
}
