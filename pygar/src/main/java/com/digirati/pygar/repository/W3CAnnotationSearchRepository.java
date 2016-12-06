package com.digirati.pygar.repository;



import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.digirati.pygar.W3CSearchAnnotation;

public interface W3CAnnotationSearchRepository extends ElasticsearchRepository<W3CSearchAnnotation, String>{
    

}


