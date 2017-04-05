package com.digirati.themathmos.service.impl;



import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;

import com.digirati.themathmos.service.OASearchService;
import com.digirati.themathmos.service.TextSearchService;




@Service(OASearchServiceImpl.OA_SERVICE_NAME)
public class OASearchServiceImpl extends AnnotationSearchServiceImpl implements OASearchService{
    private static final Logger LOG = Logger.getLogger(OASearchServiceImpl.class);
 
    public static final String OA_SERVICE_NAME = "oaSearchServiceImpl";
       
    
    @Autowired
    public OASearchServiceImpl(AnnotationUtils annotationUtils,ElasticsearchTemplate template,TextSearchService textSearchService,  CacheManager cacheManager) {
	super(annotationUtils, template, textSearchService, cacheManager);
    }
    
   
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query,  String queryString, String page, String within, String type, String widthHeight)  {
	

	String pageTest;
	int pageNumber = 1;
	if ("1".equals(page) || null == page) {
	    pageTest = "";
	}else{
	    pageTest = page; 
	    pageNumber = Integer.parseInt(page);
	}
	
	
	String queryWithNoPageParamter = annotationUtils.removeParametersAutocompleteQuery(queryString,new String[]{"page"});
	String queryWithAmendedPageParamter = queryWithNoPageParamter + pageTest;

	Cache mixedCache = cacheManager.getCache("mixedSearchCache");
	Cache.ValueWrapper obj = mixedCache.get(queryWithAmendedPageParamter);
	
	Map<String, Object> textAnnoMap;
	if(null != obj){	    
	    Map<String, Object> textAndAnnoMap = (Map)obj.get();
	    return new ServiceResponse<>(Status.OK, textAndAnnoMap);
	}else{	
	    if(pageNumber > 1){
		Cache.ValueWrapper firstObj = mixedCache.get(queryWithNoPageParamter);
		Map<String, Object> firstTextMap;
		if(null == firstObj){
		    firstTextMap = this.getMap(query,queryString,false, null, within, type, widthHeight); 
		    if(null != firstTextMap){
			mixedCache.put(queryWithNoPageParamter, firstTextMap);
			firstObj = mixedCache.get(queryWithNoPageParamter);
		    }
		}
		if(null != firstObj){
		    Map<String, Object> firstMap = (Map)firstObj.get();
		    int[] totalElementsForTally = annotationUtils.tallyPagingParameters(firstMap,false, 0, 0);
		    LOG.info("totalElementsForTally 0:" + totalElementsForTally[0] + " 1:" + totalElementsForTally[1]);
		    for(int y = 2; y <= pageNumber; y++){
			Map<String, Object> textMap = this.getMap(query, queryString, false, Integer.toString(y), within, type, widthHeight);		    
			if(null != textMap){
			    totalElementsForTally = annotationUtils.tallyPagingParameters(textMap,false, totalElementsForTally[0], totalElementsForTally[1]);
			    LOG.error("totalElementsForTally " +totalElementsForTally[0] + " and " + totalElementsForTally[1]);
			    String queryWithPageParamter = queryWithNoPageParamter + (y);
			    mixedCache.put(queryWithPageParamter, textMap);
			}else{				
			  LOG.error("Error with the cache ");
			}
		    }
		    annotationUtils.amendTotal(firstMap,totalElementsForTally[0], false);
		    mixedCache.put(queryWithNoPageParamter, firstMap);
		    
		    obj = mixedCache.get(queryWithAmendedPageParamter);
		    if(null != obj){	    
			Map<String, Object> textMap = (Map)obj.get();
			return new ServiceResponse<>(Status.OK, textMap);
		    }
		}				
	    }else{
		textAnnoMap = this.getMap(query,queryString,false, page, within, type, widthHeight); 
		if(null != textAnnoMap){
		    mixedCache.put(queryWithNoPageParamter, textAnnoMap);
		    return new ServiceResponse<>(Status.OK, textAnnoMap);
		}else{
		    return new ServiceResponse<>(Status.NOT_FOUND, null);
		}
	    }
	}
	
	return new ServiceResponse<>(Status.NOT_FOUND, null);

    }
    
    
    
    
  
}
