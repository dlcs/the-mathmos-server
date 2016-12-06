package com.digirati.barbarella.repository;



import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.digirati.barbarella.TextAnnotation;


public interface TextSearchRepository extends ElasticsearchRepository<TextAnnotation, String>{
    

}


