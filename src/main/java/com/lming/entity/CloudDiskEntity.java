package com.lming.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Data;

@Data
@Document(indexName = "clouddisk", type = "disk")
public class CloudDiskEntity {
	@Id
	private String id;
	// 名称
	private String name;
	// 来源
	private String source;
	// 描述
	private String describe;
	// 分享时间
	private Date shartime;
	// 浏览次数
	private Long browsetimes;
	// 文件大小
	private Double filesize;
	// 分享人
	private String sharpeople;
	// 收录时间
	private String collectiontime;
	// 地址
	private String baiduaddres;

}
