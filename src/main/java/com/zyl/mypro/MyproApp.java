package com.zyl.mypro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MyproApp extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(MyproApp.class, args);
		//增加输出窗口打印成功信息 方便用户看到启动成功后的标志
		System.out.println("helloworld application success...");

	}
	/**
	 * 命名空间限制
	 */
//	@Bean
//	public ServletRegistrationBean webDispatcher() {
//		DispatcherServlet dispatchServlet = new DispatcherServlet();
//		XmlWebApplicationContext appContext = new XmlWebApplicationContext();
//	    appContext.setConfigLocation("classpath:spring.mvc-web.xml");
//	    dispatchServlet.setApplicationContext(appContext);
//
//	    ServletRegistrationBean srb = new ServletRegistrationBean<>(dispatchServlet, "/web/*");
//	    srb.setName("web-dispatcher");
//	    return srb;
//	}
}
