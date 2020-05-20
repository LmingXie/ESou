package com.lming.reptile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class mythread implements Runnable {
	
	Long count;
	public void run() {
		try {
			// count最大值 1813454114
			for (; this.count < 1813454114; this.count++) {
				PrintWriter printWriter = new PrintWriter(
						new FileWriter(new File("F:/url.txt"), true));
				PrintWriter cachePrintWriter = new PrintWriter(
						new FileWriter(new File("F:/index.txt"), false));
				cachePrintWriter.println(this.count);
				cachePrintWriter.close();
				String url = "http://pan.baidu.com/wap/share/home?uk=" + this.count;
				Document doc = Jsoup.connect(url).timeout(60000).get();
				String title = doc.title();
				System.out.println(title + this.count);
				Element content = doc.body();
				Elements emptytag = content.select(".empty-other");// 看是否有分享 为空则是有分享
				if (emptytag.isEmpty()) {
					System.out.println("有分享");
					Elements dataems = content.select("[data-ac=active]");
					for (Element dataem : dataems) {
						Elements lists = dataem.select(".list-item");
						String sourcename = dataem.attr("data-fn");
						if (sourcename != "") {
							if (!sourcename.matches(
									"^\\w+.[^sS]+$|^\\w+[-]\\w+.[^sS]+$|^\\w+[.?!;]\\w+.[^Ss]+$|\\w+|^.*[!？?！].*$")) {
								System.out.println("不是数字");
								if (sourcename.indexOf("医院") == -1 && sourcename.indexOf("淘宝") == -1
										&& sourcename.indexOf("彩票") == -1 && sourcename.indexOf("福彩") == -1
										&& sourcename.indexOf("牌") == -1 && sourcename.indexOf("双色球") == -1
										&& sourcename.indexOf("创业") == -1 && sourcename.indexOf("咨询") == -1
										&& sourcename.indexOf("赚") == -1 && sourcename.indexOf("网店") == -1
										&& sourcename.indexOf("营销") == -1 && sourcename.indexOf("娱乐") == -1
										&& sourcename.indexOf("cf刷枪") == -1 && sourcename.indexOf("哪里") == -1
										&& sourcename.indexOf("麻将") == -1 && sourcename.indexOf("作弊") == -1
										&& sourcename.indexOf("早泄") == -1 && sourcename.indexOf("人流") == -1
										&& sourcename.indexOf("包皮") == -1 && sourcename.indexOf("痔") == -1
										&& sourcename.indexOf("肾") == -1 && sourcename.indexOf("治疗") == -1
										&& sourcename.indexOf("病") == -1 && sourcename.indexOf("哪家") == -1
										&& sourcename.indexOf("哪个") == -1 && sourcename.indexOf("妇科") == -1
										&& sourcename.indexOf("男科") == -1 && sourcename.indexOf("复件") == -1
										&& sourcename.indexOf("痘") == -1 && sourcename.indexOf("免费") == -1
										&& sourcename.indexOf("qq") == -1 && sourcename.indexOf("QQ") == -1
										&& sourcename.indexOf("减肥") == -1 && sourcename.indexOf("高考志愿") == -1
										&& sourcename.indexOf("瘦身") == -1 && sourcename.indexOf("新建") == -1
										&& sourcename.indexOf("挂") == -1 && sourcename.indexOf("解压") == -1
										&& sourcename.indexOf("肝") == -1 && sourcename.indexOf("炎") == -1
										&& sourcename.indexOf("补丁") == -1 && sourcename.indexOf("疤痕") == -1
										&& sourcename.indexOf(".exe") == -1 && sourcename.indexOf("刷") == -1
										&& sourcename.indexOf(".com") == -1 && sourcename.indexOf("美女") == -1) {
									System.out.println(sourcename);
									printWriter.println(sourcename);
									for (Element listem : lists) {
										String linkHref = url + listem.attr("href");
										printWriter.println(linkHref);
										System.out.println(linkHref);
									}
								} else {
									System.out.println(sourcename);
								}
							} else {
								System.out.println("是数字");
							}
						}
					}
				}
				printWriter.close();
				cachePrintWriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

