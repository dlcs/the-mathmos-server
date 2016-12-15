package com.digirati.themathmos.service.impl;



import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.model.annotation.w3c.W3CAnnotation;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CSearchService;




@Service(W3CSearchServiceImp.SERVICE_NAME)
public class W3CSearchServiceImp extends AnnotationSearchServiceImpl implements W3CSearchService{
    private final static Logger LOG = Logger.getLogger(W3CSearchServiceImp.class);
 
    public static final String SERVICE_NAME = "w3cSearchServiceImpl";
       
   
    private TextSearchService textSearchService;
    
    @Autowired
    public W3CSearchServiceImp(AnnotationUtils annotationUtils,ElasticsearchTemplate template,TextSearchService textSearchService ) {
	super(annotationUtils, template);
	this.textSearchService = textSearchService;
   
    }
    
   
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query,  String queryString, String page)  {
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, null, null, null, queryString, true, page);
	
	int annoListSize = annoSearchArray.length;
	LOG.info("annoListSize: " + annoListSize);
	ServiceResponse<Map<String, Object>> textAnnoMap = textSearchService.getTextPositions(query, queryString, true, page, true);
	
	long totalAnnotationHits = this.getTotalHits();
	long totalTextHits = textSearchService.getTotalHits();
	
	boolean isPageable = false;
	if(totalAnnotationHits+totalTextHits > DEFAULT_PAGING_NUMBER){	    
	    isPageable = true;
	}
	
	PageParameters textPagingParamters = textSearchService.getPageParameters();
	textPagingParamters.setTotalElements(totalAnnotationHits+totalTextHits+"");
	
	PageParameters pagingParameters = this.getPageParameters();
	
	Map<String, Object> annoMap = null;
	if(annoSearchArray.length != 0){
	    List<W3CAnnotation> annotationList = annotationUtils.getW3CAnnotations(annoSearchArray);

	    annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, true, pagingParameters, this.getTotalHits(), true);
	}
	
	if((null == textAnnoMap || textAnnoMap.getObj().isEmpty()) && (null == annoMap || annoMap.isEmpty())){
	    return new ServiceResponse<>(Status.NOT_FOUND, null);  
	}
	
	Map<String, Object> root =null;
	if(isPageable){
	    root = annotationUtils.buildAnnotationPageHead(queryString, true, textPagingParamters);
	}else{
	    root = annotationUtils.buildAnnotationListHead(queryString, true);
	}   
	
	if(null != textAnnoMap && !textAnnoMap.getObj().isEmpty()){	
	    Map map = (LinkedHashMap) textAnnoMap.getObj().get(CommonUtils.FULL_HAS_ANNOTATIONS);
	    List textResources = (List)map.get(CommonUtils.W3C_RESOURCELIST);
	    Map hitMap = (LinkedHashMap) textAnnoMap.getObj().get(CommonUtils.FULL_HAS_HITLIST);
	    List textHits = (List)hitMap.get(CommonUtils.W3C_RESOURCELIST);
	    
	    Map mapForResources = new LinkedHashMap<>();
	    root.put(CommonUtils.FULL_HAS_ANNOTATIONS, mapForResources);
	    mapForResources.put(CommonUtils.W3C_RESOURCELIST, textResources);
	    
	    Map mapForHits = new LinkedHashMap<>();
	    root.put(CommonUtils.FULL_HAS_HITLIST, mapForHits);
	    mapForHits.put(CommonUtils.W3C_RESOURCELIST, textHits);
	    	   
	}
	if(null != annoMap && !annoMap.isEmpty()){
	    Map map = (LinkedHashMap) annoMap.get(CommonUtils.FULL_HAS_ANNOTATIONS);
	    List annoResources = (List)map.get(CommonUtils.W3C_RESOURCELIST);
	    if(root.containsKey(CommonUtils.FULL_HAS_ANNOTATIONS)){
		Map rootMap = (LinkedHashMap) root.get(CommonUtils.FULL_HAS_ANNOTATIONS);
		List existingResources = (List)rootMap.get(CommonUtils.W3C_RESOURCELIST);
		existingResources.addAll(annoResources);
	    }else{
		Map mapForResources = new LinkedHashMap<>();
		root.put(CommonUtils.FULL_HAS_ANNOTATIONS, mapForResources);
		mapForResources.put(CommonUtils.W3C_RESOURCELIST, annoResources);
	    }
	}
	
	return  new ServiceResponse<>(Status.OK, root);

    }
   

}
