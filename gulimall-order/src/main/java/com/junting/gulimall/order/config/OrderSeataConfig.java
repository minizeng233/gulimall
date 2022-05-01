package com.junting.gulimall.order.config;


/**
 * @author mini_zeng
 * @create 2022-01-18 14:48
 */
//@Configuration
//public class OrderSeataConfig {
//    @Autowired
//    DataSourceProperties dataSourceProperties;
//    // 代理数据源，seata事务使用
//    @Bean
//    public DataSource dataSource(DataSourceProperties dataSourceProperties){
//        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//        if(StringUtils.hasText(dataSourceProperties.getName())){
//            dataSource.setPoolName(dataSourceProperties.getName());
//        }
//        return new DataSourceProxy(dataSource);
//    }
//}
