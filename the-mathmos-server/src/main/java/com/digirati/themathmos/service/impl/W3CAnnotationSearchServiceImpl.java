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
import com.digirati.themathmos.service.W3CAnnotationSearchService;



@Service(W3CAnnotationSearchServiceImpl.SERVICE_NAME)
public class W3CAnnotationSearchServiceImpl extends AnnotationSearchServiceImpl implements W3CAnnotationSearchService{
    
    
    public static final String SERVICE_NAME = "w3CAnnotationSearchServiceImpl";
     
    
    
    @Autowired
    public W3CAnnotationSearchServiceImpl( AnnotationUtils annotationUtils, ElasticsearchTemplate template) {
	super(annotationUtils, template);
        
    }
    
    
    
 
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query, String motivation, String date, String user, String queryString, String page) {
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, motivation, date, user, queryString, true, page);
	if(annoSearchArray.length == 0){
	    return new ServiceResponse<Map<String, Object>>(Status.NOT_FOUND, null); 
	}

	PageParameters pagingParameters = this.getPageParameters();

	List<W3CAnnotation> annotationList = annotationUtils.getW3CAnnotations(annoSearchArray);
		
	Map<String, Object> annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, true, pagingParameters, this.getTotalHits());
	   	
	if(null != annoMap && !annoMap.isEmpty()){

	    return new ServiceResponse<>(Status.OK, annoMap);
	}else{
	    return new ServiceResponse<>(Status.NOT_FOUND, null); 
	}
	
	
	
    }
   

}
