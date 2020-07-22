package com.couyis.zuul.filter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.couyis.zuul.pojo.AccessLog;
import com.couyis.zuul.redis.RedisUtil;
import com.couyis.zuul.service.IAccessLogService;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class AccessLogFilter extends ZuulFilter {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private IAccessLogService accessLogService;
	@Autowired
	private RedisUtil redisUtil;

	@Override
	public boolean shouldFilter() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		// TODO Auto-generated method stub
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		logger.info("send  {} request to {} ", request.getMethod(), request.getRequestURL().toString());
		AccessLog accessLog = new AccessLog();
		accessLog.setCreateTime(new Date());

		String accessToken = request.getHeader("accessToken");
		if (StringUtils.isEmpty(accessToken)) {
			accessToken = request.getParameter("accessToken");
		}
		if (!StringUtils.isEmpty(accessToken)) {
			Map<Object, Object> map = redisUtil.hmget(accessToken);
			if(map.size()>0) {
				String accountToken = map.get("accountToken").toString();
				String account = redisUtil.get(accountToken).toString();
				accessLog.setCreateUser(account);
			}			
		}

		accessLog.setIpAddress(getIpAddress(request));
		accessLog.setGetPara(request.getQueryString());
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
			StringBuffer sb = new StringBuffer();
			String temp;
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			br.close();
			String params = sb.toString();
			accessLog.setPostPara(params);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		accessLog.setUrl(request.getRequestURL().toString());
		accessLogService.add(accessLog);
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
		return 0;
	}

	private String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}