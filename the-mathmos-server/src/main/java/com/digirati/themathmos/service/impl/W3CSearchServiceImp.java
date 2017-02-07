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
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CSearchService;




@Service(W3CSearchServiceImp.W3C_SERVICE_NAME)
public class W3CSearchServiceImp extends AnnotationSearchServiceImpl implements W3CSearchService{
    private static final Logger LOG = Logger.getLogger(W3CSearchServiceImp.class);
 
    public static final String W3C_SERVICE_NAME = "w3cSearchServiceImpl";
       
    
    @Autowired
    public W3CSearchServiceImp(AnnotationUtils annotationUtils,ElasticsearchTemplate template,TextSearchService textSearchService, CacheManager cacheManager ) {
	super(annotationUtils, template, textSearchService, cacheManager);
    }
    
    
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query,  String queryString, String page)  {
	
	String pageTest = "";
	int pageNumber = 1;
	
	if (!"1".equals(page) && null != page) {
	    pageTest = page; 
	    pageNumber = Integer.parseInt(page);
	}
	
	String noPageParamter = annotationUtils.removeParametersAutocompleteQuery(queryString,new String[]{"page"});
	String amendedPageParamter = noPageParamter + pageTest;
	
	Cache mixedCache = cacheManager.getCache("mixedSearchCache");
	Cache.ValueWrapper obj = mixedCache.get(amendedPageParamter);
	
	Map<String, Object> textAnnoMap;
	if(null != obj){	    
	    Map<String, Object> textAndAnnoMap = (Map)obj.get();
	    return new ServiceResponse<>(Status.OK, textAndAnnoMap);
	}else{	
	    if(pageNumber > 1){
		Cache.ValueWrapper firstObj = mixedCache.get(noPageParamter);
		if(null == firstObj){
		    Map<String, Object> firstTextMap = this.getMap(query,queryString,true, null); 
		    if(null != firstTextMap){
			mixedCache.put(noPageParamter, firstTextMap);
			firstObj = mixedCache.get(noPageParamter);
		    }
		}
		if(null != firstObj){
		    Map<String, Object> firstTextMap = (Map)firstObj.get();
			int[] totalElements = annotationUtils.tallyPagingParameters(firstTextMap,true, 0, 0);
			LOG.info("totalElements 0:" + totalElements[0] + " 1:" + totalElements[1]);
			for(int y = 2; y <= pageNumber; y++){
			    Map<String, Object> textMap = this.getMap(query, queryString, true, Integer.toString(y));		    
			    if(null != textMap){
				totalElements = annotationUtils.tallyPagingParameters(textMap,true, totalElements[0], totalElements[1]);
				String queryWithPageParamter = noPageParamter + (y);
				mixedCache.put(queryWithPageParamter, textMap);
			    }else{
				LOG.error("Error with the cache ");
			    }
			}
			annotationUtils.amendTotal(firstTextMap,totalElements[0], true);
			mixedCache.put(noPageParamter, firstTextMap);
			
			obj = mixedCache.get(amendedPageParamter);
			if(null != obj){	    
			    Map<String, Object> textMap = (Map)obj.get();
			    return new ServiceResponse<>(Status.OK, textMap);
			}
		}				
	    }else{
		textAnnoMap = this.getMap(query,queryString,true, page); 
		if(null != textAnnoMap){
		    mixedCache.put(noPageParamter, textAnnoMap);
		    LOG.info(mixedCache.get(noPageParamter).get().toString());
		    return new ServiceResponse<>(Status.OK, textAnnoMap);
		}else{
		    return new ServiceResponse<>(Status.NOT_FOUND, null);
		}
	    }
	}
	
	return new ServiceResponse<>(Status.NOT_FOUND, null);

    }
	
   

}
