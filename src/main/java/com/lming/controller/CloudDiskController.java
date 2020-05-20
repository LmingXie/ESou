package com.lming.controller;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.lming.entity.CloudDiskEntity;
import com.lming.esdao.CloudDiskDao;
import com.lming.reptile.CrawlBook;

@RestController
public class CloudDiskController {
	@Autowired
	private CloudDiskDao cloudDiskDao;

	// springboot 整合 es 查询
	// 根据id查询文档信息
	@RequestMapping("/findById/{id}")
	public Optional<CloudDiskEntity> findById(@PathVariable String id) {
		return cloudDiskDao.findById(id);
	}
	
	// 爬取数据
	@GetMapping("/CrawlBookGo")
	public void addEs(CloudDiskEntity cde) {
		CrawlBook.crawlingBook(cloudDiskDao);
	}

	// 实现分页查询
	@RequestMapping("/search1")
	public List<CloudDiskEntity> search(String name, String describe,
			@PageableDefault(page = 0, value = 2) Pageable pageable) {
		// 1.创建查询对象
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		if (!StringUtils.isEmpty(name)) {
			MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", name);
			boolQuery.must(matchQuery);
		}
		if (!StringUtils.isEmpty(describe)) {
			MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("describe", describe);
			boolQuery.must(matchQuery);
		}
		// 2.调用查询接口
		Iterable<CloudDiskEntity> search = cloudDiskDao.search(boolQuery, pageable);
		// 3.将迭代器转换为集合
		return Lists.newArrayList(search);
	}
}

