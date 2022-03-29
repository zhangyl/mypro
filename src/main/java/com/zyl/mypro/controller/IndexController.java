package com.zyl.mypro.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
public class IndexController {
	//模拟beta用户
	private Set<String> betaSet = new HashSet<>();
	{
		betaSet.add("zyl");
		betaSet.add("wqc");
		betaSet.add("zz");
	}
    @GetMapping("/hello/{name}")
    @ResponseBody
    public HelloVO say(HttpServletRequest request, @PathVariable("name") String name){
    	
    	String beta = request.getHeader("beta");
    	HelloVO vo = new HelloVO();
    	vo.setName(name);
    	vo.setMessage("Hello " + name);
    	vo.setBeta(beta);
    	return vo;
    }
    @GetMapping("/login/{name}")
    @ResponseBody
    public HelloVO login(HttpServletRequest request, @PathVariable("name") String name){

    	HelloVO vo = new HelloVO();
    	vo.setName(name);
    	vo.setMessage("Hello " + name);
    	//如果是zyl
    	if(name != null && betaSet.contains(name)) {
    		vo.setBeta("b");
    	} else {
    		vo.setBeta("a");
    	}
    	
    	return vo;
    }

    /**
     * 测试浏览器redirect
     * @param response
     */
    @GetMapping("/redirect")
    public void redirect(HttpServletResponse response){
    	try {
			response.sendRedirect("https://baidu.com/");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * 
     * @param request
     * @return
     */
    @RequestMapping("/getAllUrl")                                   
    @ResponseBody
    public Set<String> getAllUrl(HttpServletRequest request) {
        Set<String> result = new HashSet<String>();
        WebApplicationContext wc = (WebApplicationContext) request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        RequestMappingHandlerMapping bean = wc.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = bean.getHandlerMethods();
        for (RequestMappingInfo rmi : handlerMethods.keySet()) {
            PatternsRequestCondition pc = rmi.getPatternsCondition();
            Set<String> pSet = pc.getPatterns();
            result.addAll(pSet);
        }
        return result;
    }
}
