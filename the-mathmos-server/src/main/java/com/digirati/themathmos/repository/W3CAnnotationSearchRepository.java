package com.digirati.themathmos.repository;


import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.digirati.themathmos.model.W3CSearchAnnotation;

public interface W3CAnnotationSearchRepository extends ElasticsearchRepository<W3CSearchAnnotation, String>{
   /* 
    Page findAll(Pageable pageable);
    
    List findAnnotationsByBody(String body);
    Page findAnnotationsByBody(String body, Pageable pageable);
    
    List findAnnotationsByMotivationsIn(List motivations);
    Page findAnnotationsByMotivationsIn(List motivations, Pageable pageable);
    
    List findAnnotationsByMotivationsNotIn(List motivations);
    Page findAnnotationsByMotivationsNotIn(List motivations, Pageable pageable);
    
    List findAnnotationsByCreatorsIn(List creators);
    Page findAnnotationsByCreatorsIn(List creators, Pageable pageable);
    
    List findAnnotationsByCreated(Date createdDate);
    Page findAnnotationsByCreated(Date createdDate, Pageable pageable);
    
    List findAnnotationsByTarget(String target);
    Page findAnnotationsByTarget(String target, Pageable pageable);
    
    List findAnnotationsByBodyOrTarget(String body, String target);
    Page findAnnotationsByBodyOrTarget(String body, String target, Pageable pageable);
    
    List findAnnotationsByBodyOrTargetAndMotivations(String body, String target, String motivation);
    Page findAnnotationsByBodyOrTargetAndMotivations(String body, String target, String motivation, Pageable pageable);
    
    
    List findAnnotationsByMotivationsInAndBodyOrTarget(List motivation,String body, String target);
    Page findAnnotationsByMotivationsInAndBodyOrTarget(List motivation,String body, String target, Pageable pageable);
    
    List findAnnotationsByCreatorsInAndBodyOrTarget(List creators,String body, String target);
    Page findAnnotationsByCreatorsInAndBodyOrTarget(List creators,String body, String target, Pageable pageable);
    
    List findAnnotationsByCreatedBetween(Date first,Date second);
    Page findAnnotationsByCreatedBetween(Date first,Date second, Pageable pageable);
    */
}


