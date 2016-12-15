package com.digirati.themathmos.service.impl;


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
import com.digirati.themathmos.service.OASearchService;
import com.digirati.themathmos.service.TextSearchService;




@Service(OASearchServiceImp.SERVICE_NAME)
public class OASearchServiceImp extends AnnotationSearchServiceImpl implements OASearchService{
    private final static Logger LOG = Logger.getLogger(OASearchServiceImp.class);
 
    public static final String SERVICE_NAME = "oaSearchServiceImpl";
       
   
    private TextSearchService textSearchService;
    
    @Autowired
    public OASearchServiceImp(AnnotationUtils annotationUtils,ElasticsearchTemplate template,TextSearchService textSearchService ) {
	super(annotationUtils, template);
	this.textSearchService = textSearchService;
   
    }
    
   
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query,  String queryString, String page)  {
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, null, null, null, queryString, false, page);
	
	int annoListSize = annoSearchArray.length;
	LOG.info("annoListSize: " + annoListSize);
	ServiceResponse<Map<String, Object>> textAnnoMap = textSearchService.getTextPositions(query, queryString, false, page, true);
	
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

	    annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, false, pagingParameters, this.getTotalHits(), true);
	}
	if((null == textAnnoMap || textAnnoMap.getObj().isEmpty()) && (null == annoMap || annoMap.isEmpty())){
	    return new ServiceResponse<>(Status.NOT_FOUND, null);  
	}
	Map<String, Object> root = null;
	if(isPageable){
	    root = annotationUtils.buildAnnotationPageHead(queryString, false, textPagingParamters);
	}else{
	    root = annotationUtils.buildAnnotationListHead(queryString, false);
	}   
	
	if(null != textAnnoMap && !textAnnoMap.getObj().isEmpty()){	
	    List textResources = (List)textAnnoMap.getObj().get("resources");
	    List textHits = (List)textAnnoMap.getObj().get("hits");
	    
	    root.put("resources", textResources);
	    root.put("hits", textHits);
	    	   
	}
	if(null != annoMap && !annoMap.isEmpty()){
	    List annoResources = (List)annoMap.get("resources");
	    if(root.containsKey("resources")){
		List existingResources = (List)root.get("resources");
		existingResources.addAll(annoResources);
	    }else{
		root.put("resources", annoResources);
	    }
	}
	
	return new ServiceResponse<>(Status.OK, root);

    }
  
}
