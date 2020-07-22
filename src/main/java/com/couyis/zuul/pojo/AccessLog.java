package com.couyis.zuul.pojo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AccessLog {
	private String id;

	private String url;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private String createUser;

	private String getPara;

	private String postPara;

	private String ipAddress;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getGetPara() {
		return getPara;
	}

	public void setGetPara(String getPara) {
		this.getPara = getPara;
	}

	public String getPostPara() {
		return postPara;
	}

	public void setPostPara(String postPara) {
		this.postPara = postPara;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

}
