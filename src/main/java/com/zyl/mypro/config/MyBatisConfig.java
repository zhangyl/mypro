package com.zyl.mypro.config;

import java.beans.PropertyVetoException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

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
import com.github.pagehelper.PageHelper;
import com.maycur.mybatis.plugin.CanaryEntCodeHook;
import com.maycur.mybatis.plugin.CanaryInterceptor;
import com.maycur.mybatis.plugin.CanaryJobMetaHook;
import com.zyl.mypro.service.CostService;

@Configuration
@MapperScan(value = "com.zyl.mypro.mapper")
public class MyBatisConfig {
    @Value("${spring.datasource.driver-class-name}")
    private String jdbcDriver;
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.username}")
    private String jdbcUser;
    @Value("${spring.datasource.password}")
    private String jdbcPassword;
    
    @Autowired(required = false)
    @Lazy
	CostService costService;
 
    @Bean
    public DataSource createDataSource() throws PropertyVetoException {
    	DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(jdbcDriver);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUser);
        dataSource.setPassword(jdbcPassword);
        return dataSource;
    }
    @Bean
    public DataSourceTransactionManager jdbcTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:mybatis/*Mapper.xml"));
        
        var pageHelper = new PageHelper();
        var props = new Properties();
        props.setProperty("dialect", "mysql");
        props.setProperty("pageSizeZero", "true");
        pageHelper.setProperties(props);
        
        //灰度拦截器企业用户
		CanaryInterceptor mybatisInterceptor = new CanaryInterceptor(new CanaryEntCodeHook() {
			@Override
			public Set<String> getEntCodeSet() {
				return getCanaryEntCodes();
			}
		});
		
        sessionFactory.setPlugins(mybatisInterceptor, pageHelper);
        
        SqlSessionFactory sqlSessionFactory = sessionFactory.getObject();
        //是否金丝雀（灰度）环境
        boolean canaryEnv = true;
        //初始化
        CanaryJobMetaHook.initCanaryMapper(sqlSessionFactory, canaryEnv);
        
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
