package com.zyl.mypro.controller;

import com.zyl.mypro.bean.Cost;
import com.zyl.mypro.bean.User;
import com.zyl.mypro.mapper.UserMapper;
import com.zyl.mypro.service.CostService;
import com.zyl.mypro.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@RestController
public class IndexController {
	private static final Logger log = LoggerFactory.getLogger(IndexController.class);
	int count = 0;
	@Autowired(required = false)
	CostService costService;

	@Autowired
	UserMapper userMapper;
	
	//模拟beta用户
	private final Set<String> betaSet = new HashSet<>();
	{
		betaSet.add("zyl");
		betaSet.add("wqc");
		betaSet.add("zz");
	}
    @GetMapping("/beta")
    @ResponseBody
	public List<String> beta() {
		List<String> list = new ArrayList<>();
		
		list.add("A");
		if((count++) % 2==0) {
			list.add("B");
		}
		
		return list;
	}
    @GetMapping("/hello/{name}")
    @ResponseBody
    public HelloVO say(HttpServletRequest request,
    		@PathVariable("name") String name){
    	String beta = request.getHeader("beta");
    	HelloVO vo = new HelloVO();
    	vo.setName(name);
    	vo.setMessage("Hello " + name);
    	vo.setBeta(beta);
    	return vo;
    }

	@GetMapping("/shardingsphereTest")
	@ResponseBody
	public List<Cost> shardingsphereTest(HttpServletRequest request){
		{
			//测试插入带人工生成id插入，没有问题
			Cost cost = new Cost();
			cost.setId(1);//模拟人工生成id
			cost.setEntCode("zyl");
			cost.setMoney(800);
//			costService.insert(cost);
		}

		//不分表
		User user = userMapper.selectByPrimaryKey(1);
		log.info("------------------" + user);
		//分表但不带分表列条件
		Cost cost = costService.select(4);
		log.info("------------------" + cost);
		//分表带分表列条件
		String entCode = request.getParameter("entCode");

		// 模拟自定义复杂TenementComplexShardingAlgorithm分表策略，2022年之前的查询走 _before_2022后缀表
		Calendar c = Calendar.getInstance();
		c.set(2021, 10, 10);
		Date date = c.getTime();
		List<Cost> costList = costService.listByEntCode(entCode, date);

		if(costList.isEmpty()) {
			log.debug("查询Cost结果为空");
		}
		return costList;
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
			log.error("", e);
		}
    	return vo;
    }

    /**
     * 测试浏览器redirect
     */
    @GetMapping("/redirect")
    public void redirect(HttpServletResponse response){
    	try {
			response.sendRedirect("https://baidu.com/");
		} catch (IOException e) {
			log.error("", e);
		}
    }

    @RequestMapping("/getAllUrl")                                   
    @ResponseBody
    public Set<String> getAllUrl(HttpServletRequest request) {
        Set<String> result = new HashSet<>();
        WebApplicationContext wc = (WebApplicationContext) request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        RequestMappingHandlerMapping bean = wc.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = bean.getHandlerMethods();
        for (RequestMappingInfo rmi : handlerMethods.keySet()) {
            PatternsRequestCondition pc = rmi.getPatternsCondition();
			if(pc != null) {
				Set<String> pSet = pc.getPatterns();
				result.addAll(pSet);
			}
        }
        return result;
    }
}
