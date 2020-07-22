package com.couyis.zuul.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.couyis.zuul.redis.RedisUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class LoginFilter extends ZuulFilter {
	@Autowired
	private RedisUtil redisUtil;
//	@Autowired
//	private ResourceService resourceService;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private List<Object> paths;

	public LoginFilter() {
		super();
		paths = new ArrayList<Object>();
		paths.add("/platform/login");
		paths.add("/api/");
	}

	@Override
	public boolean shouldFilter() {
		// TODO Auto-generated method stub
		//List<String> pubResources = resourceService.findByAttr(ResourceAttr.PUBLIC);
		return !checkResource(paths);
	}

	private Boolean checkResource(List<Object> resources) {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletRequest request = requestContext.getRequest();
		String uri = request.getRequestURI();
		for (Object string : resources) {
			if (uri.indexOf(string.toString()) != -1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		logger.info("send  {} request to {} ", request.getMethod(), request.getRequestURL().toString());
		String accessToken = request.getHeader("accessToken");
		
		if (StringUtils.isEmpty(accessToken)) {
			accessToken = request.getParameter("accessToken");
		}
		
		logger.info("access token ok");
		
		long time = redisUtil.getExpire(accessToken);
		if (StringUtils.isEmpty(accessToken)||time < 0) {
			ctx.setSendZuulResponse(false);
			ctx.setResponseStatusCode(401);
			ctx.setResponseBody("会话超时");
			return null;
		}
		Map<Object, Object> map = redisUtil.hmget(accessToken);
		String resourcesToken = map.get("resourcesToken").toString();	
		String accountToken = map.get("accountToken").toString();	
		String authorityToken = map.get("authorityToken").toString();	
		List<Object> resources = redisUtil.lGet(resourcesToken, 0, -1);		
		String account = redisUtil.get(accountToken).toString();
		
		if(checkResource(resources)) {
			ctx.addZuulRequestHeader("X-ACCESS-TOKEN", accessToken);
			ctx.addZuulRequestHeader("X-ACCOUNT", account);
			redisUtil.expire(accessToken, 1800);
			redisUtil.expire(resourcesToken, 1800);
			redisUtil.expire(accountToken, 1800);
			redisUtil.expire(authorityToken, 1800);
			return null;
		}
		ctx.setSendZuulResponse(false);
		ctx.setResponseStatusCode(403);
		ctx.setResponseBody("资源未授权");
		return null;
	}

	@Override
	public String filterType() {
		// TODO Auto-generated method stub
		return "pre";
	}

	@Override
	public int filterOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}
