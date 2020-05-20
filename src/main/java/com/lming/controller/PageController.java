package com.lming.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lming.entity.CloudDiskEntity;
import com.lming.esdao.CloudDiskDao;

@Controller
public class PageController {
	@Autowired
	private CloudDiskDao cloudDiskDao;

	@RequestMapping("/search")
	public String search(String keyword, @PageableDefault(page = 0, value = 3) Pageable pageable,
			HttpServletRequest req) {
		Long startTime = System.currentTimeMillis();
		// 查询所有的
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		if (!StringUtils.isEmpty(keyword)) {
			// 模糊查询 一定要ik中文
			MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("name", keyword);
			boolQuery.must(matchQuery);
		}
		Page<CloudDiskEntity> page = cloudDiskDao.search(boolQuery, pageable);
		req.setAttribute("page", page);
		// 计算查询总数
		long total = page.getTotalElements();
		req.setAttribute("total", page.getTotalElements());
		// 计算分页数
		int totalPage = (int) ((total - 1) / pageable.getPageSize() + 1);
		req.setAttribute("totalPage", totalPage);
		Long endTime = System.currentTimeMillis();
		// 计算程序的耗时时间
		req.setAttribute("time", endTime - startTime);
		req.setAttribute("keyword", keyword);
		return "search";
	}
}
