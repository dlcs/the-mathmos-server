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
    private final static Logger LOG = Logger.getLogger(W3CSearchServiceImp.class);
 
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
		if(null == firstObj){
		    Map<String, Object> firstTextMap = this.getMap(query,queryString,true, null, true); 
		    if(null != firstTextMap){
			mixedCache.put(queryWithNoPageParamter, firstTextMap);
			firstObj = mixedCache.get(queryWithNoPageParamter);
		    }
		}
		if(null != firstObj){
		    Map<String, Object> firstTextMap = (Map)firstObj.get();
			int[] totalElements = annotationUtils.tallyPagingParameters(firstTextMap,true, 0, 0);
			for(int y = 2; y <= pageNumber; y++){
			    Map<String, Object> textMap = this.getMap(query, queryString, true, Integer.toString(y),true);		    
			    if(null != textMap){
				totalElements = annotationUtils.tallyPagingParameters(textMap,true, totalElements[0], totalElements[1]);
				String queryWithPageParamter = queryWithNoPageParamter + (y);
				mixedCache.put(queryWithPageParamter, textMap);
			    }else{
				LOG.error("Error with the cache ");
			    }
			}
			obj = mixedCache.get(queryWithAmendedPageParamter);
			if(null != obj){	    
			    Map<String, Object> textMap = (Map)obj.get();
			    return new ServiceResponse<>(Status.OK, textMap);
			}
		}				
	    }else{
		textAnnoMap = this.getMap(query,queryString,true, page, true); 
		if(null != textAnnoMap){
		    mixedCache.put(queryWithNoPageParamter, textAnnoMap);
		    LOG.info(mixedCache.get(queryWithNoPageParamter).get().toString());
		    return new ServiceResponse<>(Status.OK, textAnnoMap);
		}else{
		    return new ServiceResponse<>(Status.NOT_FOUND, null);
		}
	    }
	}
	
	return new ServiceResponse<>(Status.NOT_FOUND, null);

    }
	/*
    private Map<String, Object> getMap(String query, String queryString, boolean isW3c, String page, boolean isMixedSearch) {
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, null, null, null, queryString, true, page);
	
	int annoListSize = annoSearchArray.length;
	LOG.info("annoListSize: " + annoListSize);
	
	ServiceResponse<Map<String, Object>> textAnnoMap = textSearchService.getTextPositions(query, queryString, true, page, true);
	int[] textPageParams  = new int[]{0,0};
	if(null != textAnnoMap && null != textAnnoMap.getObj()){
	    textPageParams = annotationUtils.getPageParams(textAnnoMap.getObj(), true);		
	}
	
	long totalAnnotationHits = this.getTotalHits();
	long totalTextHits = (long)textPageParams[0];
	
	boolean isPageable = false;
	if(totalAnnotationHits+totalTextHits > DEFAULT_PAGING_NUMBER){	    
	    isPageable = true;
	}
	
	PageParameters textPagingParamters = textSearchService.getPageParameters();
	textPagingParamters.setTotalElements(Long.toString(totalAnnotationHits+totalTextHits));
	textPagingParamters.setStartIndex(Integer.toString(textPageParams[1]));
	
	PageParameters pagingParameters = this.getPageParameters();
	int lastAnnoPage = pagingParameters.getLastPage();
	int lastTextPage = textPagingParamters.getLastPage();
	
	if(lastAnnoPage > lastTextPage){
	    textPagingParamters.setLastPageNumber(pagingParameters.getLastPageNumber()); 
	}
	
	if(null != textAnnoMap.getObj()){
	    annotationUtils.amendPagingParameters(queryString, textAnnoMap.getObj(), textPagingParamters, isW3c);
	}
	
	Map<String, Object> annoMap = null;
	if(annoSearchArray.length != 0){
	    List<W3CAnnotation> annotationList = annotationUtils.getW3CAnnotations(annoSearchArray);

	    annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, true, pagingParameters, this.getTotalHits(), true);
	}
	
	if((null == textAnnoMap || null == textAnnoMap.getObj() || textAnnoMap.getObj().isEmpty()) && (null == annoMap || annoMap.isEmpty())){
	    return  null;  
	}
	
	Map<String, Object> root;
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
	
	return  root;

    }*/
   

}
