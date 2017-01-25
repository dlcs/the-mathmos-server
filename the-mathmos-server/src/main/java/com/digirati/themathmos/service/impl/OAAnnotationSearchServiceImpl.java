package com.digirati.themathmos.service.impl;



import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.model.annotation.w3c.W3CAnnotation;
import com.digirati.themathmos.service.OAAnnotationSearchService;




@Service(OAAnnotationSearchServiceImpl.OA_ANNOTATION_SERVICE_NAME)
public class OAAnnotationSearchServiceImpl extends AnnotationSearchServiceImpl implements OAAnnotationSearchService{
    
 
    public static final String OA_ANNOTATION_SERVICE_NAME = "oaAnnotationSearchServiceImpl";
       
   
    
    
    @Autowired
    public OAAnnotationSearchServiceImpl(AnnotationUtils annotationUtils,ElasticsearchTemplate template) {
	super(annotationUtils, template);
   
    }
    
   
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query, String motivation, String date, String user, String queryString, String page)  {
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, motivation, date, user, queryString, false, page);
	if(annoSearchArray.length == 0){
	    return new ServiceResponse<Map<String, Object>>(Status.NOT_FOUND, null); 
	}
	
	PageParameters pagingParameters = this.getPageParameters();

	List<W3CAnnotation> annotationList = annotationUtils.getW3CAnnotations(annoSearchArray);

	Map<String, Object> annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, false, pagingParameters, this.getTotalHits(), false);
	   	
	if(null != annoMap && !annoMap.isEmpty()){
	    return new ServiceResponse<>(Status.OK, annoMap);
	}else{
	    return new ServiceResponse<>(Status.NOT_FOUND, null); 
	}
    }
   
    
   

}
