package com.zyl.mypro.config;

import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import com.zyl.mypro.aop.SqlStatementInterceptor;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.zyl.mypro.service.CostService;

@Configuration
@MapperScan(value = "com.zyl.mypro.mapper")
public class MyBatisConfig {

    @Autowired
    private SqlStatementInterceptor sqlStatementInterceptor;

    @Value("${spring.datasource.driver-class-name}")
    private String jdbcDriver;
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.username}")
    private String jdbcUser;
    @Value("${spring.datasource.password}")
    private String jdbcPassword;


    @Value("${dataBaseProviderIdKey:MySQL}")
    private String dataBaseProviderIdKey;
    @Value("${dataBaseProviderIdValue:mysql}")
    private String dataBaseProviderIdValue;

    @Autowired(required = false)
    @Lazy
	CostService costService;

    /**
     * 使用单纯application-jdbc.yaml时候，需要打开。
     * 使用使用单纯application.yaml(shardingsphere)时候，不用配置这个数据源
     */

//    @Bean
//    public DataSource createDataSource() throws PropertyVetoException {
//    	DruidDataSource dataSource = new DruidDataSource();
//        dataSource.setDriverClassName(jdbcDriver);
//        dataSource.setUrl(jdbcUrl);
//        dataSource.setUsername(jdbcUser);
//        dataSource.setPassword(jdbcPassword);
//        return dataSource;
//    }
    @Bean
    public DataSourceTransactionManager jdbcTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();

        sessionFactory.setDataSource(dataSource);

        //默认mysql，如果切换到其它数据库，需要单独自行配置
        {
            Properties properties = new Properties();

            properties.put(dataBaseProviderIdKey, dataBaseProviderIdValue);
            DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();

            databaseIdProvider.setProperties(properties);

            sessionFactory.setDatabaseIdProvider(databaseIdProvider);
        }


        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mybatis/*Mapper.xml"));

//        SqlStatementInterceptor interceptor = new SqlStatementInterceptor();

        sessionFactory.setPlugins(sqlStatementInterceptor);
        //灰度拦截器企业用户
//		CanaryInterceptor mybatisInterceptor = new CanaryInterceptor(new CanaryEntCodeHook() {
//			@Override
//			public Set<String> getEntCodeSet() {
//				return getCanaryEntCodes();
//			}
//		});
		
//        sessionFactory.setPlugins(mybatisInterceptor, pageHelper);
        
        SqlSessionFactory sqlSessionFactory = sessionFactory.getObject();
        //是否金丝雀（灰度）环境
//        boolean canaryEnv = true;
        //初始化
//        CanaryJobMetaHook.initCanaryMapper(sqlSessionFactory, canaryEnv);
        
        return sqlSessionFactory;
    }
    
    // 模拟远程调用，获取灰度企业用户
    public Set<String> getCanaryEntCodes() {
    	Set<String> list = new HashSet<>();
    	list.add("13854321278");
    	list.add("13854321279");
    	return list;
    }
    
}
