package com.lming.entity;

import lombok.Data;

/**
 * 服务器返回的json，转java对象 错误码为20 代表需要验证码
 * 
 * @author gaoqiang
 *
 */
@Data
public class Response20 {
	String errno;
	String request_id;
	String server_time;
	String vcode;
	String img;

}
