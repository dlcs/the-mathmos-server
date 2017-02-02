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




@Service(OASearchServiceImp.OA_SERVICE_NAME)
public class OASearchServiceImp extends AnnotationSearchServiceImpl implements OASearchService{
    private final static Logger LOG = Logger.getLogger(OASearchServiceImp.class);
 
    public static final String OA_SERVICE_NAME = "oaSearchServiceImpl";
       
    
    @Autowired
    public OASearchServiceImp(AnnotationUtils annotationUtils,ElasticsearchTemplate template,TextSearchService textSearchService,  CacheManager cacheManager) {
	super(annotationUtils, template, textSearchService, cacheManager);
    }
    
   
    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query,  String queryString, String page)  {
	

	String pageTest = "";
	int pageNumber = 1;
	Cache mixedCache = cacheManager.getCache("mixedSearchCache");
	if ("1".equals(page) || null == page) {
	    pageTest = "";
	}else{
	    pageTest = page; 
	    pageNumber = Integer.parseInt(page);
	}
	
	String queryWithNoPageParamter = annotationUtils.removeParametersAutocompleteQuery(queryString,new String[]{"page"});
	String queryWithAmendedPageParamter = queryWithNoPageParamter + pageTest;
	String queryWithPageParamter = "";
	Cache.ValueWrapper obj = mixedCache.get(queryWithAmendedPageParamter);
	
	Map<String, Object> textAnnoMap;
	if(null != obj){	    
	    Map<String, Object> textAndAnnoMap = (Map)obj.get();
	    return new ServiceResponse<>(Status.OK, textAndAnnoMap);
	}else{	
	    if(pageNumber > 1){
		Cache.ValueWrapper firstObj = mixedCache.get(queryWithNoPageParamter);
		if(null == firstObj){
		    Map<String, Object> firstTextMap = this.getMap(query,queryString,false, null, true); 
		    if(null != firstTextMap){
			mixedCache.put(queryWithNoPageParamter, firstTextMap);
			firstObj = mixedCache.get(queryWithNoPageParamter);
		    }
		}
		if(null != firstObj){
		    Map<String, Object> firstTextMap = (Map)firstObj.get();
			int[] totalElements = annotationUtils.tallyPagingParameters(firstTextMap,false, 0, 0);
			for(int y = 2; y <= pageNumber; y++){
			    Map<String, Object> textMap = this.getMap(query, queryString, false, Integer.toString(y),true);		    
			    if(null != textMap){
				totalElements = annotationUtils.tallyPagingParameters(textMap,false, totalElements[0], totalElements[1]);
				queryWithPageParamter = queryWithNoPageParamter + (y);
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
		textAnnoMap = this.getMap(query,queryString,false, page, true); 
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
    
    protected Map<String, Object> getMap(String query, String queryString, boolean isW3c, String page, boolean isMixedSearch) {
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, null, null, null, queryString, isW3c, page);
	
	int annoListSize = annoSearchArray.length;
	LOG.info("annoListSize: " + annoListSize);
	
	ServiceResponse<Map<String, Object>> textAnnoMap = textSearchService.getTextPositions(query, queryString, isW3c, page, true);
	int[] textPageParams  = new int[]{0,0};
	if(null != textAnnoMap && null != textAnnoMap.getObj()){
	    textPageParams = annotationUtils.getPageParams(textAnnoMap.getObj(), isW3c);		
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
	    annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, isW3c, pagingParameters, (AnnotationSearchServiceImpl.DEFAULT_PAGING_NUMBER - 1), true);
	}
	if((null == textAnnoMap || null == textAnnoMap.getObj()) && (null == annoMap || annoMap.isEmpty())){	    
	    return  null;  
	}
	Map<String, Object> root;
	if(isPageable){
	    root = annotationUtils.buildAnnotationPageHead(queryString, isW3c, textPagingParamters);
	}else{
	    root = annotationUtils.buildAnnotationListHead(queryString, isW3c);
	}   
	
	if(null != textAnnoMap && null != textAnnoMap.getObj()){
	    if(isW3c){
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
    	    	
	    }else{
		List textResources = (List)textAnnoMap.getObj().get(CommonUtils.OA_RESOURCELIST);
    	    	List textHits = (List)textAnnoMap.getObj().get(CommonUtils.OA_HITS);
    	    
    	    	root.put(CommonUtils.OA_RESOURCELIST, textResources);
    	    	root.put(CommonUtils.OA_HITS, textHits);
	    }
	}
	if(null != annoMap && !annoMap.isEmpty()){
	    if(isW3c){
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
	    }else{
        	 List annoResources = (List)annoMap.get(CommonUtils.OA_RESOURCELIST);
        	 if(root.containsKey(CommonUtils.OA_RESOURCELIST)){
        	     List existingResources = (List)root.get(CommonUtils.OA_RESOURCELIST);
        	     existingResources.addAll(annoResources);
        	     root.put(CommonUtils.OA_RESOURCELIST, existingResources);
        	  }else{
        	      root.put(CommonUtils.OA_RESOURCELIST, annoResources);
        	 }
	    }
	}
	
	return  root;
	
	
    }
    
  */
}
