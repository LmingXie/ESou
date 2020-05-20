package com.lming.esdao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.lming.entity.CloudDiskEntity;

public interface CloudDiskDao extends ElasticsearchRepository<CloudDiskEntity, String> {

}
