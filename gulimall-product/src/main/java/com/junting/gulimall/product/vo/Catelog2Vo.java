package com.junting.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mini_zeng
 * @create 2022-01-09 16:40
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {
    private String catelog1Id;
    private String id;
    private String name;
    private List<Catelog3Vo> catalog3List;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo{
        private String id;
        private String name;
        private String catelog2Id;
    }
}
