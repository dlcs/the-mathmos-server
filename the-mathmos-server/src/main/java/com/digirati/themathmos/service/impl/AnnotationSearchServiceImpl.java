package com.digirati.themathmos.service.impl;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.mapper.W3CSearchAnnotationMapper;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.W3CSearchAnnotation;
import com.digirati.themathmos.model.Within;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.model.annotation.w3c.W3CAnnotation;
import com.digirati.themathmos.service.TextSearchService;


@Service(AnnotationSearchServiceImpl.SERVICE_NAME)
public class AnnotationSearchServiceImpl {

    private static final Logger LOG = Logger.getLogger(AnnotationSearchServiceImpl.class);

    public static final String SERVICE_NAME = "annotationSearchServiceImpl";
    SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ssZ");

    private static final String W3C_INDEX = "w3cannotation";
    private Client client;

    protected AnnotationUtils annotationUtils;
    
    protected TextSearchService textSearchService;
    
    protected CacheManager cacheManager;
    
    protected static final int DEFAULT_PAGING_NUMBER = AnnotationSearchConstants.DEFAULT_PAGING_NUMBER;
    private static final int DEFAULT_STARTING_PAGING_NUMBER = 0;
        
    private long totalHits = 0;  
 
    private PageParameters pagingParameters = null;
    
    

    @Autowired
    /**
     * 
     * @param annotationUtils {@code AnnotationUtils} helper for annotations
     * @param template {@code ElasticsearchTemplate} what we use to get the elasticsearch client
     * @param textSearchService {@code TextSearchService} our text search service
     * @param cacheManager {@code CacheManager} our cache manager
     */
    public AnnotationSearchServiceImpl(AnnotationUtils annotationUtils, ElasticsearchTemplate template,TextSearchService textSearchService, CacheManager cacheManager) {
	this.annotationUtils = annotationUtils;
	this.client = template.getClient();
	this.textSearchService = textSearchService;
	this.cacheManager = cacheManager;
    }
 
    
    public PageParameters getPageParameters(){
	return pagingParameters;
    }
    
    public long getTotalHits(){
	return totalHits;
    }
    
   /**
    * Method to get annotation page form elasticsearch
    * @param query {@code String} e.g. q=test
    * @param motivation {@code String} e.g. motivation=tagging
    * @param date {@code String} e.g. date=date in format (YYYY-MM-DD'T'hh:mm:ssZ)
    * @param user {@code String} e.g. user=sarah
    * @param queryString {@code String} e.g/ http://www.examples.com/search/search/oa?q=test
    * @param isW3c {@code boolean} true if w3c annotation and false if oa
    * @param page {@code String} page parameter e.g. page=2
    * @return {@code String[]} containing either the w3c or oa annotations
    */
    public String[] getAnnotationsPage(String query, String motivation, String date, String user, String queryString,
	    boolean isW3c, String page, String within, String type)  {
    
	totalHits = 0;
	
	pagingParameters = null;
	
	int pagingSize = DEFAULT_PAGING_NUMBER;
	int from = DEFAULT_STARTING_PAGING_NUMBER;
	
	//TODO validate that pagenumber is int and is in expected range.
	if(!StringUtils.isEmpty(page)){
	    Integer pagingInteger =  Integer.parseInt(page);	    
	    from = (pagingInteger.intValue()-1) * pagingSize;
	}

	QueryBuilder builder = buildAllThings(query,motivation,date, user, type);
	Page<W3CSearchAnnotation> annotationPage;
	
	annotationPage= formQuery(builder,from,pagingSize, within);
	
	if(null == annotationPage){
	    return new String[0];
	}
	String[] annoSearchArray = new String[annotationPage.getNumberOfElements()];
	
	LOG.info(String.format("Our paged search returned [%s] items ", annotationPage.getNumberOfElements()));
	int count = 0;
	for (W3CSearchAnnotation w3CAnnotation : annotationPage) {
	    String jsonLd;
	    if (isW3c) {
		jsonLd = w3CAnnotation.getW3cJsonLd();
	    } else {
		jsonLd = w3CAnnotation.getOaJsonLd();
	    }
	    annoSearchArray[count] = jsonLd;
	    count++;
	}
	pagingParameters = annotationUtils.getAnnotationPageParameters(annotationPage, queryString, DEFAULT_PAGING_NUMBER, totalHits);
	return annoSearchArray;	
	
    }
   
    
    private Page<W3CSearchAnnotation> formQuery(QueryBuilder queryBuilder,int pageNumber, int pagingSize, String within){
   	Pageable pageable  = new PageRequest(pageNumber, pagingSize);
   	
   	W3CSearchAnnotationMapper resultsMapper = new W3CSearchAnnotationMapper();

   	// need a new SearchRequestBuilder or the source does not change
   	SearchRequestBuilder searchRequestBuilderReal  = client.prepareSearch(W3C_INDEX);
   	
   	SearchRequestBuilder searchRequestBuilder  = client.prepareSearch(W3C_INDEX);
   	searchRequestBuilder.setQuery(queryBuilder);	
   	searchRequestBuilder.setPostFilter(QueryBuilders.boolQuery());
   	searchRequestBuilder.setFrom(pageNumber).setSize(pagingSize);
   	
   	if(null != within){
   	    String decodedWithinUrl =  annotationUtils.decodeWithinUrl(within); 
   	
   		
   	    Map <String, Object> map = annotationUtils.getQueryMap(searchRequestBuilder.toString());
   	    if(null != decodedWithinUrl){
   		map = annotationUtils.setSource(map,decodedWithinUrl, W3C_INDEX);
   		searchRequestBuilderReal.setSource(map);
   	    }else{
   	   	LOG.error("Unable to find match to within");
   	    }
   	  
   	}else{
   	    searchRequestBuilderReal = searchRequestBuilder;
   	}

   	LOG.info("doSearch query "+ searchRequestBuilderReal.toString());
   	
   	SearchResponse response = searchRequestBuilderReal.execute()
   		.actionGet();
   	
   	totalHits = response.getHits().totalHits();
   	LOG.info("Total hits are: "+totalHits);
   	
   	return resultsMapper.mapResults(response, W3CSearchAnnotation.class, pageable);
   }
    
    
    private Page<W3CSearchAnnotation> formQueryWithWithin(String query, String type,int from, int size, String within){
   	Pageable pageable  = new PageRequest(from, size);
   	String[] fields = new String[]{"body", "target", "bodyURI", "targetURI"};
   	W3CSearchAnnotationMapper resultsMapper = new W3CSearchAnnotationMapper();

   	SearchRequestBuilder searchRequestBuilder  = client.prepareSearch(W3C_INDEX);
   	
   	Within withinObj = new Within();
   	Map <String, String>withinMap = withinObj.getWithinMap();
   	String withinUri = null;
   	if(withinMap.containsKey(within)){
   	    withinUri = withinMap.get(within);
   	}
   	
   	Map <String, Object> map = annotationUtils.setESSource(from, size, query, fields, type);
   	//String within = "http://wellcomelibrary.org/service/collections/collections/digukmhl/";
   	if(null != withinUri){
   	    map = annotationUtils.setSource(map,withinUri, W3C_INDEX);
   	}else{
   	    LOG.error("Unable to find match to within");
   	}
   	
   	searchRequestBuilder.setSource(map);
  
   	LOG.info("source "+ searchRequestBuilder.toString());
   	LOG.info("doSearch query "+ searchRequestBuilder.toString());
   	
   	SearchResponse response = searchRequestBuilder.execute()
   		.actionGet();
   	
   	totalHits = response.getHits().totalHits();
   	LOG.info("Total hits are: "+totalHits);
   	
   	return resultsMapper.mapResults(response, W3CSearchAnnotation.class, pageable);
   } 
    

   private QueryBuilder buildDateRangeQuery(String field,String from, String to){
       return QueryBuilders.rangeQuery(field).from(from).to(to).includeLower(true).includeUpper(true);
   }
   
   private List<QueryBuilder> buildDateRangeQuery(String field, String allRanges) {
	List<QueryBuilder> queryBuilders = new ArrayList<>();
	List<String> dates = annotationUtils.getListFromSpaceSeparatedTerms(allRanges);
	QueryBuilder buildDateRangeQuery;
	for (String dateString : dates) {

	    try {
		String[] splitDate = dateString.split("[/]");
		if (splitDate.length != 2) {
		    throw new SearchQueryException(
			    "Please enter dates with the ISO8601 format YYYY-MM-DDThh:mm:ssZ/YYYY-MM-DDThh:mm:ssZ");
		} else {
		    DatatypeConverter.parseDateTime(splitDate[0]);
		    DatatypeConverter.parseDateTime(splitDate[1]);
		    
		    buildDateRangeQuery = buildDateRangeQuery(field, splitDate[0], splitDate[1]);
		    queryBuilders.add(buildDateRangeQuery);  
		}
	    } catch (IllegalArgumentException e) {
		LOG.debug(String.format("Wrong date format entered for [%s] ",allRanges), e);
		throw new SearchQueryException("Please enter dates with the ISO8601 format YYYY-MM-DDThh:mm:ssZ");
	    } 
	}
	
	return queryBuilders;
    }
   
    private QueryBuilder buildDates(String field, String allRanges){
	List<QueryBuilder> dates = buildDateRangeQuery(field, allRanges);
	
	BoolQueryBuilder should = QueryBuilders.boolQuery();
	
	for(QueryBuilder dateRange:dates){
	    should =  should.should(dateRange);
	}
	return should;
    }
	
    private QueryBuilder buildAllThings(String query,String motivations, String allDateRanges, String users, String type) {
	List <QueryBuilder> queryList  = new ArrayList<>();
	
	BoolQueryBuilder must = QueryBuilders.boolQuery();	
	
	if(null != query){
	    String tidyQuery = annotationUtils.convertSpecialCharacters(query);
	    if(null == type){
		must = must.must(QueryBuilders.multiMatchQuery(tidyQuery, "body","target","bodyURI", "targetURI").type(Type.PHRASE)); 
	    }
	    if ("topic".equals(type)){
		must = must.must(QueryBuilders.multiMatchQuery(tidyQuery, "bodyURI"));
	    }
	}
		
	if(null != motivations){
   
	    if(motivations.contains("non-")){
		
		List<String>motivationsList = annotationUtils.getListFromSpaceSeparatedTerms(motivations);
		if(motivationsList.size() > 1){
		    throw new SearchQueryException(
    			"You have a motivation that is a non-<motivation>, there can only be one motivation in this instance."); 
		}else{		  
		    String tidyMotivations = motivations.replaceAll("non-", "");
		    queryList.add(QueryBuilders.existsQuery(AnnotationSearchConstants.FIELD_MOTIVATIONS));		    
		    must = must.mustNot(QueryBuilders.queryStringQuery(tidyMotivations).field(AnnotationSearchConstants.FIELD_MOTIVATIONS));
   
		}
	    }else{
		queryList.add(QueryBuilders.queryStringQuery(motivations).field(AnnotationSearchConstants.FIELD_MOTIVATIONS));
	    }
	}

	if(null != allDateRanges){
	    queryList.add(buildDates("created", allDateRanges));
	}

	if(null != users){
	    queryList.add(QueryBuilders.queryStringQuery(users).field("creators"));
	}
   	
 
   	for(QueryBuilder eachQuery:queryList){
   	    must =  must.must(eachQuery);
   	}
   	
   	
   	return must;
     }
    
   
    protected Map<String, Object> getMap(String query, String queryString, boolean isW3c, String page, String within, String type) {
	
	
	
	String[] annoSearchArray  = this.getAnnotationsPage(query, null, null, null, queryString, isW3c, page, within, type);
	
	int annoListSize = annoSearchArray.length;
	LOG.info("annoListSize: " + annoListSize);
	
	ServiceResponse<Map<String, Object>> textAnnoMap = textSearchService.getTextPositions(query, queryString, isW3c, page, true, within);
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
	LOG.info("total annotations are : " + (totalAnnotationHits+totalTextHits));
	textPagingParamters.setTotalElements(Long.toString(totalAnnotationHits+totalTextHits));
	textPagingParamters.setStartIndex(Integer.toString(textPageParams[1]));
	
	
	PageParameters mapPagingParameters = this.getPageParameters();
	int lastAnnoPage = mapPagingParameters.getLastPage();
	int lastTextPage = textPagingParamters.getLastPage();
	
	if(lastAnnoPage > lastTextPage){
	    textPagingParamters.setLastPageNumber(mapPagingParameters.getLastPageNumber()); 
	}
	
	if(null != textAnnoMap && null != textAnnoMap.getObj()){
	    Map<String, Object> testingRoot = textAnnoMap.getObj();
	    if(null != annotationUtils.getResources(testingRoot, isW3c)){
		annotationUtils.amendPagingParameters(textAnnoMap.getObj(), textPagingParamters, isW3c);
	    }else{
		textAnnoMap = null;
	    }
	}
	
	Map<String, Object> annoMap = null;
	if(annoSearchArray.length != 0){
	    List<W3CAnnotation> annotationList = annotationUtils.getW3CAnnotations(annoSearchArray);
	    annoMap = annotationUtils.createAnnotationPage(queryString, annotationList, isW3c, mapPagingParameters, AnnotationSearchServiceImpl.DEFAULT_PAGING_NUMBER - 1, true);
	}
	if((null == textAnnoMap || null == textAnnoMap.getObj()) && (null == annoMap || annoMap.isEmpty())){	    
	    return  null;  
	}
	
	
	return createResources(isPageable,isW3c,queryString, textAnnoMap, annoMap, textPagingParamters);
    }
	
    
    
    
    private Map<String, Object> createResources(boolean isPageable,boolean isW3c,String queryString, ServiceResponse<Map<String, Object>> textAnnoMap, Map<String, Object> annoMap, PageParameters textPagingParamters){
	
	Map<String, Object> root = createHead(isPageable,queryString, isW3c, textPagingParamters);
	
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
        	     if(null == existingResources){
        		 existingResources = new ArrayList<>();
        	     }
        	     existingResources.addAll(annoResources);
        	     root.put(CommonUtils.OA_RESOURCELIST, existingResources);
        	  }else{
        	      root.put(CommonUtils.OA_RESOURCELIST, annoResources);
        	 }
	    }
	}
	
	return  root;
    }
    
    private Map<String, Object> createHead(boolean isPageable, String queryString, boolean isW3c, PageParameters textPagingParamters){
	Map<String, Object> root;
	if(isPageable){
	    root = annotationUtils.buildAnnotationPageHead(queryString, isW3c, textPagingParamters);
	}else{
	    root = annotationUtils.buildAnnotationListHead(queryString, isW3c);
	} 
	return root;
    }
    
    
    
    
    
    	
    
    
}
