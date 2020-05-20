package com.lming.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.lming.entity.Response20;
import com.lming.entity.SetDataBean;

/**
 * 输入百度网盘的资源地址，获取网盘的真实下载地址。
 * 
 * @author gaoqiang
 * 
 */
public class GetBaiduCloudRealURL {
	// 定义一些全局变量
	private static String url = "https://pan.baidu.com/s/1XB7O9dbYXeWjWQ1k76bx8w";// 资源地址
	private static String cookie = null;
	private static final String getvcodeURL = "https://pan.baidu.com/api/getvcode?prod=pan";// 请求vcode地址，不变
	private static boolean isDownload = true;// 标记是否需要下载文件，false就只获取地址
	private static String server_filename = null;
	private static String size = null;

	// 通过浏览器抓包分析，获取百度网盘共享文件的真实下载地址
	public static void main(String[] args) {
//		if (args.length > 0) {
//			System.out.println("输入的原始地址：" + args[0]);
//			url = args[0];
//		}

		// 第一次获取cookie（）
		Map<String, String> map1 = HttpUtils.get(url, cookie);
		System.out.println("服务器返回的cookie：\n" + map1.get("cookie") + "\n");
		cookie = "PANWEB=1;" + map1.get("cookie").split(";")[0];// 抓包看到携带了PANWEB1，不设置也没问题
		Map<String, String> params = getBodyParams(map1.get("body"));
		server_filename = params.get("server_filename");
		size = params.get("size");

		// 拼接post的url地址
		String post_url = getPostUrl(params);

		// 拼接post携带的参数
		Map<String, String> data = getPostData(params);

		// 发送post请求
		String responseJson = HttpUtils.post(post_url, data, cookie);
		System.out.println(responseJson + "\n");

		Gson gson = new Gson();
		Response20 response20 = gson.fromJson(responseJson, Response20.class);
		String errorCode = response20.getErrno();

		int count = 0;
		while (!errorCode.equals("0") && count < 5) {
			count++;
			if (errorCode.equals("-20")) {
				// 下载超过3次，需要验证码,获取vcode和img地址
				Map<String, String> generateValidateCode = generateValidateCode(count);

				data.put("vcode_input", generateValidateCode.get("vcode_input"));
				data.put("vcode_str", generateValidateCode.get("vcode_str"));
				String responseJsonCode = HttpUtils.post(post_url, data, cookie);
				System.out.println(responseJsonCode + "\n");

				errorCode = gson.fromJson(responseJsonCode, Response20.class).getErrno();
				if (errorCode.equals("0")) {
					responseJson = responseJsonCode;
				}
			}
		}

		if (errorCode.equals("0")) {// 成功返回真实的url
			String realURL = parseRealDownloadURL(responseJson);
			System.out.println("成功！真实的下载链接为：" + realURL);
			if (isDownload) {
				System.out.println("正在下载文件...");
				HttpUtils.download(realURL, cookie, server_filename, size);// 进行下载
			} else {
				System.out.println("配置不下载");
			}
		} else {
			System.out.println("尝试了" + count + "次，地址获取失败");
		}

	}

	/**
	 * POST请求的url地址
	 */
	public static String getPostUrl(Map<String, String> params) {
		/**
		 * post请求（抓包可看到） 抓包看到logid，实际测试logid不携带也可以正常抓取 https://pan.baidu.com/api/
		 * sharedownload?sign=2d970c761500085deb09d423d549dea2f4ef28da &timestamp=
		 * 1515400310&bdstoken=null&channel=chunlei&clienttype=0&web=1
		 * &app_id=250528&logid=MTUxNTQwMDQ1NTc1MTAuOTYwMTE4NzIyMzcxMzYwNQ==
		 */
		StringBuffer sb1 = new StringBuffer();
		sb1.append("https://pan.baidu.com/api/sharedownload?");
		sb1.append("sign=" + params.get("sign"));
		sb1.append("&timestamp=" + params.get("timestamp"));
		sb1.append("&bdstoken=" + params.get("bdstoken"));
		sb1.append("&channel=chunlei");
		sb1.append("&clienttype=0");
		sb1.append("&web=1");
		sb1.append("&app_id=" + params.get("app_id"));
		String post_url = sb1.toString();
		System.out.println("POST请求的网址：" + post_url);

		return post_url;
	}

	/**
	 * 获取POST请求携带的参数
	 */
	public static Map<String, String> getPostData(Map<String, String> params) {
		// POST携带的参数(抓包可看到)
		Map<String, String> data = new HashMap<String, String>();
		data.put("encrypt", "0");
		data.put("product", "share");
		data.put("uk", params.get("uk"));
		data.put("primaryid", params.get("primaryid"));
		// 添加了[]，解码就是%5B %5D
		data.put("fid_list", "%5B" + params.get("fid_list") + "%5D");
		data.put("path_list", "");// 可以不写

		return data;
	}

	/**
	 * 从post返回数据解析出dlink字段，真实的下载地址，这个地址有效期8h
	 */
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

	/**
	 * 获取并输入验证码
	 * 
	 * @return map{vcode_str:请求的vcode值, vcode_input:输入的验证码}
	 */
	public static Map<String, String> generateValidateCode(int count) {
		// 下载超过3次，需要验证码,获取vcode和img地址
		Map<String, String> vcodeResponse = HttpUtils.get(getvcodeURL, cookie);
		String res = vcodeResponse.get("body");
		System.out.println("获取vcode：" + vcodeResponse.get("body"));

		Gson gsonVcode = new Gson();
		Response20 responseVcode = gsonVcode.fromJson(res, Response20.class);
		String vcode = responseVcode.getVcode();
		String imgURL = responseVcode.getImg();
		System.out.println("vcode值：" + vcode);
		System.out.println("验证码地址：" + imgURL);

		// 请求验证码
		HttpUtils.saveImage(imgURL, cookie);

		System.out.print("查看图片，输入验证码(第" + count + "次尝试/共5次):");
		InputStreamReader is_reader = new InputStreamReader(System.in);
		Map<String, String> map = new HashMap<String, String>();
		try {
			String result = new BufferedReader(is_reader).readLine();
			System.out.println("输入的验证码为：" + result + "\n");
			map.put("vcode_str", vcode);
			map.put("vcode_input", result);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	/**
	 * 正则匹配出json字符串，将json字符串转化为java对象。
	 */
	public static Map<String, String> getBodyParams(String body) {
		Map<String, String> map = new HashMap<String, String>();

		String setData = "";
		Pattern pattern_setData = Pattern.compile("setData.*?;");
		Matcher matcher_setData = pattern_setData.matcher(body);
		if (matcher_setData.find()) {
			String tmp = matcher_setData.group(0);
			setData = tmp.substring(8, tmp.length() - 2);
			// System.out.println("setData:" + setData + "\n");

			Gson gson = new Gson();
			SetDataBean bean = gson.fromJson(setData, SetDataBean.class);

			System.out.println("sign--" + bean.getSign());
			System.out.println("token--" + bean.getBdstoken());
			System.out.println("timestamp--" + bean.getTimestamp());
			System.out.println("uk--" + bean.getUk());
			System.out.println("shareid--" + bean.getShareid());
			System.out.println("fs_id--" + bean.getFile_list().getList()[0].getFs_id());
			System.out.println("file_name--" + bean.getFile_list().getList()[0].getServer_filename());
			System.out.println("size--" + bean.getFile_list().getList()[0].getSize());
			System.out.println("app_id--" + bean.getFile_list().getList()[0].getApp_id() + "\n");

			map.put("sign", bean.getSign());
			map.put("timestamp", bean.getTimestamp());
			map.put("bdstoken", bean.getBdstoken());
			map.put("app_id", bean.getFile_list().getList()[0].getApp_id());
			map.put("uk", bean.getUk());
			map.put("shareid", bean.getShareid());
			map.put("primaryid", bean.getShareid());
			map.put("fs_id", bean.getFile_list().getList()[0].getFs_id());
			map.put("fid_list", bean.getFile_list().getList()[0].getFs_id());
			map.put("server_filename", bean.getFile_list().getList()[0].getServer_filename());
			map.put("size", bean.getFile_list().getList()[0].getSize());

		}

		return map;
	}

}