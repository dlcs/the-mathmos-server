package com.digirati.themathmos.repository;


import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.digirati.themathmos.model.W3CSearchAnnotation;

public interface W3CAnnotationSearchRepository extends ElasticsearchRepository<W3CSearchAnnotation, String>{
   
}


