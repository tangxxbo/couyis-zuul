package com.couyis.zuul.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.couyis.zuul.pojo.AccessLog;

@FeignClient(value = "couyis-platform",fallback =AccessLogService.class )
public interface IAccessLogService {
	@RequestMapping(value = "/frame/accessLog/add", method = RequestMethod.POST)
	public void add(@RequestBody AccessLog accessLog);

}
