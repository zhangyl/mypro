package com.zyl.mypro.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.zyl.mypro.bean.Cost;
import com.zyl.mypro.service.CostService;
import com.zyl.mypro.util.JwtUtil;

@RestController
public class IndexController {
	
	@Autowired
	CostService costService;
	
	//模拟beta用户
	private Set<String> betaSet = new HashSet<>();
	{
		betaSet.add("zyl");
		betaSet.add("wqc");
		betaSet.add("zz");
	}
	
	
    @GetMapping("/hello/{name}")
    @ResponseBody
    public HelloVO say(HttpServletRequest request, 
    		HttpServletResponse response, 
    		@PathVariable("name") String name){
    	String beta = request.getHeader("beta");
    	HelloVO vo = new HelloVO();
    	vo.setName(name);
    	vo.setMessage("Hello " + name);
    	vo.setBeta(beta);
    	return vo;
    }
    @GetMapping("/login")
    @ResponseBody
    public HelloVO login(HttpServletRequest request, 
    		HttpServletResponse response){
    	String name = request.getParameter("name");
    	HelloVO vo = new HelloVO();
    	vo.setName(name);
    	vo.setMessage("Hello " + name);
    	
    	Cost cost = costService.select(1);
    	if(cost != null) {
    		vo.setBeta("b");
    	}
        Map<String, Object> claims = new HashMap<>(16);
        claims.put("userId", name);
        claims.put("username", name);
    	if(name != null && betaSet.contains(name)) {
    		claims.put("beta", "b");
    	}
		String token = JwtUtil.generalTocken(claims);
		String pToken = Base64.getUrlEncoder().encodeToString(token.getBytes());
    	try {
			response.sendRedirect("http://zyl.com/a.html?p=" + pToken);
		} catch (IOException e) {
			e.printStackTrace();
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
