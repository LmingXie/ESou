package com.lming.reptile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.lming.entity.SetDataBean;

public class ReptileMain {
	// 下载链接和提取码
	private static String url = "http://pan.baidu.com/s/1pJ4k7VX";
	private static String pwd = "6ts6";
	// 这几个参数不要动
	private static final String baseUrl = "https://pan.baidu.com/share/verify?surl=";
	private static String params = "";
	// 下载参数，文件名及文件大小
	private static String server_filename = null;
	private static String size = null;
	// 从cookie中获取的重要参数 核心参数
	private static String sekey = "";

	static Map<String, String> getDetailed(String linkUrl,String password) throws IOException{
//		System.out.println("url:"+url+",pwd:"+pwd);
		url = linkUrl;
		if(password != null)
			pwd = password;
		// 获取Cookies
		Map<String, String> cookies = getCookies();
		// 更具请求URL获取页面
		Connection.Response res2 = Jsoup.connect(url)
				.method(Method.POST)
				.cookies(cookies)
				.timeout(60 * 1000)
				.ignoreContentType(true).execute();
//		System.out.println(res2.body());
		// getBodyParams解析body获取POST请求的参数
		Map<String, String> params = getBodyParams(res2.body());
		return params;
	}

	/********************* 如下为解析百度网盘获得真实下载地址参考Demo（如果你想要做下载器的话） ***************************/
	public static void main(String[] args) throws IOException {
		// 获取Cookies
		Map<String, String> cookies = getCookies();
		// 更具请求URL获取页面
		Connection.Response res2 = Jsoup.connect(url).method(Method.POST).cookies(cookies).ignoreContentType(true)
				.execute();

//		System.out.println(res2.body());
		// getBodyParams解析body获取POST请求的参数
		Map<String, String> params = getBodyParams(res2.body());
		// getPostUrl获取POST请求URL
		String post_url = getPostUrl(params);
		// 设置Sekey
		getSekeyBycookies(cookies);
		// 获取data
		Map<String, String> data = getPostData(params);
		// 发送POST请求
		Response res3 = Jsoup.connect(post_url).method(Method.POST).header("Referer", url).cookies(cookies).data(data)
				.ignoreContentType(true).timeout(1000 * 60).execute();
		String URL = parseRealDownloadURL(JSONArray.toJSONString(res3));
		System.out.println(URL);
	}

	public static Map<String, String> getCookies() throws IOException {
		String surl = url.split("/s/1")[1];
		params += "&t=" + System.currentTimeMillis() + "channel=chunlei&web=1&app_id=230528&clienttype=0";

		Connection.Response res = Jsoup.connect(baseUrl + surl + params)
				.header("Referer", "https://pan.baidu.com/share/init?surl=" + surl).data("pwd", pwd).method(Method.POST)
				.timeout(60 * 1000).ignoreContentType(true).execute();
		Map<String, String> cookies = res.cookies();
		return cookies;
	}

	public static String getPostUrl(Map<String, String> params) {
		StringBuffer sb1 = new StringBuffer();
		sb1.append("https://pan.baidu.com/api/sharedownload?");
		sb1.append("sign=" + params.get("sign"));
		sb1.append("&timestamp=" + params.get("timestamp"));
		sb1.append("&channel=chunlei");
		sb1.append("&web=1");
		sb1.append("&app_id=" + params.get("app_id"));
		sb1.append("&clienttype=0");
		String post_url = sb1.toString();
		return post_url;
	}

	public static Map<String, String> getBodyParams(String body) {
		Map<String, String> map = new HashMap<String, String>();

		String setData = "";
		Pattern pattern_setData = Pattern.compile("setData.*?;");
		Matcher matcher_setData = pattern_setData.matcher(body);
		if (matcher_setData.find()) {
			String tmp = matcher_setData.group(0);
			setData = tmp.substring(8, tmp.length() - 2);
			System.out.println(setData); // 查看完整响应json数据
			Gson gson = new Gson();
			SetDataBean bean = gson.fromJson(setData, SetDataBean.class);

//			map.put("sign", bean.getSign());
//			map.put("timestamp", bean.getTimestamp());
//			map.put("bdstoken", bean.getBdstoken());
//			map.put("app_id", bean.getFile_list().getList()[0].getApp_id());
//			map.put("uk", bean.getUk());
			map.put("photo", bean.getPhoto());
			map.put("linkusername", bean.getLinkusername());
//			map.put("shareid", bean.getShareid());
//			map.put("primaryid", bean.getShareid());
//			map.put("fs_id", bean.getFile_list().getList()[0].getFs_id());
//			map.put("fid_list", bean.getFile_list().getList()[0].getFs_id());
			map.put("server_filename", bean.getFile_list().getList()[0].getServer_filename());
			map.put("server_mtime", bean.getFile_list().getList()[0].getServer_mtime());
			map.put("size", bean.getFile_list().getList()[0].getSize());
//			map.put("logid", logid);

		}
//		System.out.println(JSONArray.toJSONString(map));
		return map;
	}

	// 解析cookies获取sekey
	private static void getSekeyBycookies(Map<String, String> cookies) throws UnsupportedEncodingException {
		String bdclnd = cookies.get("BDCLND");
		if (null != bdclnd && !"".equals(bdclnd)) {
			sekey = java.net.URLDecoder.decode(bdclnd, "UTF-8");
		}
	}

	// 获取POST请求需要的data系参数
	public static Map<String, String> getPostData(Map<String, String> params) {
		// POST携带的参数(抓包可看到)
		Map<String, String> data = new HashMap<String, String>();
		data.put("encrypt", "0");
		data.put("product", "share");
		data.put("uk", params.get("uk"));
		data.put("primaryid", params.get("primaryid"));
		// 添加了[]
		data.put("fid_list", "[" + params.get("fid_list") + "]");
		data.put("path_list", "");// 可以不写
		data.put("extra", "{\"sekey\":\"" + sekey + "\"}");
		return data;
	}

	// 获取到真实路径
	public static String parseRealDownloadURL(String responseJson) {
		String realURL = "";
		Pattern pattern = Pattern.compile("\"dlink\":.*?,");
		Matcher matcher = pattern.matcher(responseJson);
		if (matcher.find()) {
			String tmp = matcher.group(0);
			String dlink = tmp.substring(9, tmp.length() - 2);
			realURL = dlink.replaceAll("\\\\", "");
		}
		return realURL;
	}

}
