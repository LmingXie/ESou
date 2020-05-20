package com.lming.reptile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.lming.entity.CloudDiskEntity;
import com.lming.esdao.CloudDiskDao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参考文章：https://www.cnblogs.com/LexMoon/p/JavaP.html
 * <p>
 * 爬取资源站“http://bestcbooks.com/”网盘链接<br>
 * 更多资源网站：<br>
 * http://www.pansoso.com/ 猜解http://www.pansoso.com/e/394137中的394137 <br>
 * http://www.pansou.com/ 猜解 http://yun.baidu.com/s/1c21LahU 中的1c21LahU<br>
 * http://www.friok.com/ 猜解 http://www.friok.com/download.php?id=102331 中的102331
 * <br>
 * http://www.wangpanjie.com/5895.html 猜解5895 <br>
 * 此处不再过多举例 百度/Google搜索“网盘资源网站”一大堆 <br>
 * 最为有效的方式是解析大型搜索网站如百度等，不过过程非常复杂.多爬几个网盘网站你的搜索引擎网站就会非常庞大了 <br>
 * </p>
 * 
 * @author Lming
 *
 */
public class CrawlBook {

	public static void main(String[] args) throws ParseException {
		String s = "Android\\u9a71\\u52a8\\u5f00\\u53d1\\u4e0e\\u79fb\\u690d\\u5b9e\\u6218\\u8be6\\u89e3.pdf";
		String[] split = s.split("\\.");
		System.out.println(JSONArray.toJSON(split));
	}

	public static Date stampToDate(String s) {
//	    String res;
//	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long lt = new Long(s);
		Date date = new Date(lt);
//	    res = simpleDateFormat.format(date);
		return date;
	}

	transient static int modCount;

	public static void crawlingBook(CloudDiskDao cloudDiskDao) {
		// 获取分类链接
		ArrayList<String> arrayList;
		try {
			arrayList = getBookClass(getBookUrlCode("http://bestcbooks.com/"));
			for (int i = 0; i < arrayList.size(); i++) {
				// 循环分类链接请求
				String read = getBookUrlCode("http://bestcbooks.com" + arrayList.get(i));
				// 获取所有该分类下所有书籍链接
				ArrayList<String> book = getBook(read);
				for (int j = 0; j < book.size(); j++) {
					if (modCount == 10) // 爬取数据过多，为了测试进行中断爬取
						return;
					String[] bookIn = book.get(j).split("\"");
					String myBook = bookIn[1];
					// 请求书籍详情页获得 网盘链接和密码
					String myBookCode = getBookUrlCode("http://bestcbooks.com" + myBook);
					// System.out.println(myBookCode);
					String url = findURL(myBookCode);
					String password = getPassword(myBookCode);
					
					// 验证连接并进一步获取网盘信息
					Map<String, String> map = null;
					try {
						map = ReptileMain.getDetailed(url, password);
					} catch (SocketException se1) {
						try {
							map = ReptileMain.getDetailed(url, password);
							System.err.println("爬虫重试机制运行：第二次重试.....");
						} catch (SocketException se2) {
							try {
								System.err.println("爬虫重试机制运行：第三次重试.....");
								map = ReptileMain.getDetailed(url, password);
							} catch (SocketException se3) {
								System.out.println("模拟重试机制3次！异常连接：" + url + ",password:" + password);
							}
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
					CloudDiskEntity cde = new CloudDiskEntity();
					cde.setName(map.get("server_filename").split("\\.")[0]);
					cde.setSource("百度云盘");
					cde.setDescribe("这是一段描述"); // 未爬取
					cde.setBrowsetimes(10l);
					cde.setFilesize((double) (Math.round((Float.parseFloat(map.get("size")) / 1024 / 1024) * 10)) / 10);
					cde.setShartime(stampToDate(map.get("server_mtime") + "000"));
					cde.setSharpeople(map.get("linkusername"));
					cde.setCollectiontime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
					cde.setBaiduaddres(url);

					// save到ElasticSearch中
//					CloudDiskEntity save = cloudDiskDao.save(cde);
					System.out.println("ElasticSearch添加成功：" + JSONArray.toJSONString(cde) + "\n");
					modCount++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更具URL获取请求资源
	 * 
	 * @param url 请求地址
	 * @return 返回字符串型 网页源码
	 * @throws IOException
	 */
	public static String getBookUrlCode(String url) throws IOException {
		URL u;
		HttpURLConnection httpURLConnection;
		String ret = "";
		try {
			u = new URL(url);
			httpURLConnection = (HttpURLConnection) u.openConnection();
			if (httpURLConnection.getResponseCode() == 200) {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));

				String read;
				while ((read = bufferedReader.readLine()) != null) {
					ret += read;
					ret += "\r\n";
				}
			}
		} catch (Exception e) {

		}
		return ret;
	}

	/**
	 * 获得所有书籍分类 链接 列表
	 * 
	 * @param read 页面源码
	 * @return
	 */
	public static ArrayList<String> getBookClass(String read) {
		ArrayList<String> arrayList = new ArrayList<String>();
		Document doc = Jsoup.parse(read);
		Elements elements = doc.select("a"); // 查询a标签
		for (Element element : elements) {
			String aurl = element.attr("href"); // 获取href属性值
			// 正则：获取aurl中所有以 /categories开头的href
			String con = "/categories(.*)";
			Pattern ah = Pattern.compile(con);
			Matcher mr = ah.matcher(aurl);
			// 进行正则匹配
			while (mr.find()) {
				if (!arrayList.contains(mr.group())) {
					// 将符合正则的内容添加到列表
					arrayList.add(mr.group());
				}
			}
		}
		return arrayList;
	}

	/**
	 * 正则获取 链接+图片
	 * 
	 * @param read
	 * @return
	 */
	public static ArrayList<String> getBook(String read) {
		ArrayList<String> arrayList = new ArrayList<String>();

		String con = "<a href=(.*)<img src=\"/images/download";
		Pattern ah = Pattern.compile(con); // 创建正则表达式对象
		Matcher mr = ah.matcher(read); // 创建匹配器
		while (mr.find()) {
			if (!arrayList.contains(mr.group())) {
				arrayList.add(mr.group());
			}
		}
		return arrayList;
	}

	/**
	 * 正则获得百度网盘链接
	 * 
	 * @param read 网页源码
	 */
	public static String findURL(String read) {
		String con = "<a href=\"(.*)pan.baidu.com(.*)ref";
		Pattern ah = Pattern.compile(con);
		Matcher mr = ah.matcher(read);
		if (mr.find()) {
			String[] bookPan = mr.group().split("\"");
			String bookM = bookPan[1];
			System.out.print(bookM + "        ");
			return bookM;
		}
		return null;
	}

	/**
	 * 正则获得百度网盘链接
	 * 
	 * @param read 网页源码
	 */
	public static String getPassword(String read) {
		String con = "密码(.*)";
		Pattern ah = Pattern.compile(con);
		Matcher mr = ah.matcher(read);
		if (mr.find()) {
			String pass = mr.group();
			System.out.println(pass);
			return pass.substring(3, 7);
		}
		System.out.println();
		return null;
	}

}