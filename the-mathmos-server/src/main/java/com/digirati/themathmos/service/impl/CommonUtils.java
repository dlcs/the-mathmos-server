package com.digirati.themathmos.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.model.TermOffsetStart;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;


public class CommonUtils {
 
    private static final Logger LOG = Logger.getLogger(CommonUtils.class);
    
    protected static final String WC3CONTEXT_PATH = "http://www.w3.org/ns/anno.jsonld";
    protected static final String FULL_LAYER = "http://iiif.io/api/presentation/2#Layer";

    protected static final String PRESENTATIONCONTEXT_PATH = "http://iiif.io/api/presentation/2/context.json";
    
    protected static final String OA_SEARCH_TERMLIST = "search:TermList";
    protected static final String SEARCHCONTEXT_PATH = "http://iiif.io/api/search/1/context.json";

    protected static final String FULL_HAS_ANNOTATIONS = "http://iiif.io/api/presentation/2#hasAnnotations";
    protected static final String FULL_HAS_TERMLIST = "http://iiif.io/api/search/1#hasTermList";

    protected static final String FULL_ANNOTATIONLIST = "http://iiif.io/api/presentation/2#AnnotationList";
    protected static final String OA_ANNOTATIONLIST = "sc:AnnotationList";
    
    protected static final String FULL_HAS_HITLIST = "http://iiif.io/api/search/1#hasHitList";
    protected static final String OA_HITS = "hits";

    protected static final String W3C_RESOURCELIST = "@list";
    protected static final String OA_TERMSLIST = "terms";
    protected static final String OA_RESOURCELIST = "resources";

    protected static final String CONTEXT = "@context";

    protected static final String ROOT_ID = "@id";
    protected static final String ROOT_TYPE = "@type";
    
    protected static final String W3C_WITHIN_TYPE = "type";
    protected static final String W3C_WITHIN_IS_PART_OF = "dcterms:isPartOf";
    protected static final String W3C_WITHIN_AS_TOTALITEMS = "as:totalItems";
    
    protected static final String FIRST = "first";
    protected static final String LAST = "last";
    protected static final String NEXT = "next";
    protected static final String PREV = "prev";
    
    protected static final String OA_WITHIN_TOTAL = "total";
    protected static final String OA_WITHIN = "within";
    
    protected static final String W3C_STARTINDEX =  "as:startIndex";
    protected static final String OA_STARTINDEX =  "startIndex";
    
    protected static final String[] AUTOCOMPLETE_IGNORE_PARAMETERS = new String[] 
	    {AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, AnnotationSearchConstants.PARAM_FIELD_USER, AnnotationSearchConstants.PARAM_FIELD_DATE};

    protected List getResources(Map<String, Object> root, boolean isW3c) {
	List resources;
	if (isW3c) {
	    Map map = (LinkedHashMap) root.get(FULL_HAS_ANNOTATIONS);
	    if(null == map){
		return null;
	    }
	    resources = (List) map.get(W3C_RESOURCELIST);
	} else {
	    resources = (List) root.get(OA_RESOURCELIST);
	}
	return resources;
    }
    
    protected void removeResources(Map<String, Object> root, boolean isW3c) {

   	if (isW3c) { 	 
   	    root.remove(FULL_HAS_ANNOTATIONS);
   	} else {
   	    root.remove(OA_RESOURCELIST);
   	} 	
    }

    protected void setResources(Map<String, Object> root, boolean isW3c) {
	List resources = new ArrayList();

	if (isW3c) {
	    Map <String, List>map = new LinkedHashMap<>();
	    root.put(FULL_HAS_ANNOTATIONS, map);
	    map.put(W3C_RESOURCELIST, resources);
	} else {
	    root.put(OA_RESOURCELIST, resources);
	}
    }

    protected void setHits(Map<String, Object> root) {
	root.put(OA_HITS, new ArrayList());
	
    }

    protected List getHits(Map<String, Object> root) {
	List hits = (List) root.get(OA_HITS);
	return hits;
    }


    protected void setContextIdType(Map<String, Object> root, boolean isW3c, String query, boolean isText) {
	if (isText) {
	    List resources = new ArrayList();
	    if (isW3c) {
		resources.add(WC3CONTEXT_PATH);
	    } else {
		resources.add(PRESENTATIONCONTEXT_PATH);
	    }
	    resources.add(SEARCHCONTEXT_PATH);
	    root.put(CONTEXT, resources); 
	} else {
	    if (isW3c) {
		root.put(CONTEXT, WC3CONTEXT_PATH);
	    } else {
		root.put(CONTEXT, PRESENTATIONCONTEXT_PATH);
	    }
	}

	root.put(ROOT_ID, query);
	if (isW3c) {
	    root.put(ROOT_TYPE, FULL_ANNOTATIONLIST);
	} else {
	    root.put(ROOT_TYPE, OA_ANNOTATIONLIST);
	}
    }

    protected Map<String, Object> buildAnnotationListHead(String query, boolean isW3c, boolean isText) {
	Map<String, Object> root = new LinkedHashMap<>();
	setContextIdType(root, isW3c, query, isText);
	setResources(root, isW3c);
	return root;
    }

    @SuppressWarnings("unchecked") 
    protected Map<String, Object> buildAutoCompleteHead(String query, String motivation, String date, String user) {
	

	Map<String, Object> root = new LinkedTreeMap<>();

	root.put(CONTEXT, SEARCHCONTEXT_PATH);
	
	String queryWithRemovedIgnoredParamters = removeParametersAutocompleteQuery(query,AUTOCOMPLETE_IGNORE_PARAMETERS);
	root.put(ROOT_ID, queryWithRemovedIgnoredParamters);
	

	root.put(ROOT_TYPE, OA_SEARCH_TERMLIST);
	
	if(!StringUtils.isEmpty(motivation) || !StringUtils.isEmpty(date) || !StringUtils.isEmpty(user)){
	    List ignored = new ArrayList();
	    if(!StringUtils.isEmpty(motivation)){
		ignored.add(AnnotationSearchConstants.PARAM_FIELD_MOTIVATION);
	    }
	    if(!StringUtils.isEmpty(date)){
		ignored.add(AnnotationSearchConstants.PARAM_FIELD_DATE);
	    }
	    if(!StringUtils.isEmpty(user)){
		ignored.add(AnnotationSearchConstants.PARAM_FIELD_USER);
	    }
	    root.put("ignored",ignored);
	}

	List resources = new ArrayList();
	
	root.put(OA_TERMSLIST, resources); 
	

	return root;
    }
    
    
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> buildAnnotationPageHead(String query, boolean isW3c, PageParameters pagingParams, boolean isText) {
	int total = pagingParams.getTotal();
	String first = pagingParams.getFirstPageNumber();
	String last = pagingParams.getLastPageNumber();
	String next = pagingParams.getNextPageNumber();
	String previous = pagingParams.getPreviousPageNumber();
	int startIndex = pagingParams.getStartIndex();

	Map<String, Object> root = new LinkedHashMap<>();

	setContextIdType(root, isW3c, query,isText);

	Map withinMap = new LinkedHashMap();
	if (isW3c) {
	    withinMap.put(W3C_WITHIN_TYPE, FULL_LAYER);
	    withinMap.put(W3C_WITHIN_AS_TOTALITEMS, total);
	    withinMap.put(FIRST, first);
	    withinMap.put(LAST, last);
	    root.put(W3C_WITHIN_IS_PART_OF, withinMap);
	} else {
	    
	    withinMap.put(ROOT_TYPE, "sc:Layer");
	    withinMap.put(OA_WITHIN_TOTAL, total);
	    withinMap.put(FIRST, first);
	    withinMap.put(LAST, last);
	    root.put(OA_WITHIN, withinMap);
	}

	if (null != next) {
	    root.put(NEXT, next);
	}
	if (null != previous) {
	    root.put(PREV, previous);
	}
	if (isW3c) {
	    root.put(W3C_STARTINDEX, startIndex);
	} else {
	    root.put(OA_STARTINDEX, startIndex);
	}

	setResources(root, isW3c);

	return root;
    }
    
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> buildEmptyAnnotationPageHead(String query, boolean isW3c, PageParameters pagingParams, boolean isText) {
	int total = pagingParams.getTotal();;
	int startIndex = pagingParams.getStartIndex();

	Map<String, Object> root = new LinkedHashMap<>();

	setContextIdType(root, isW3c, query,isText);

	Map withinMap = new LinkedHashMap();
	if (isW3c) {
	    withinMap.put(W3C_WITHIN_TYPE, FULL_LAYER);
	    withinMap.put(W3C_WITHIN_AS_TOTALITEMS, total);
	    root.put(W3C_WITHIN_IS_PART_OF, withinMap);
	} else {
	    
	    withinMap.put(ROOT_TYPE, "sc:Layer");
	    withinMap.put(OA_WITHIN_TOTAL, total);
	    
	    root.put(OA_WITHIN, withinMap);
	}

	if (isW3c) {
	    root.put(W3C_STARTINDEX, startIndex);
	} else {
	    root.put(OA_STARTINDEX, startIndex);
	}

	setResources(root, isW3c);
	setHits(root);
	return root;
    }

    public List<String> getListFromSpaceSeparatedTerms(String terms) {
	if (null != terms) {
	    String[] termsArray = terms.split("[ ]");

	    return Arrays.asList(termsArray);
	}
	return new ArrayList<>();
    }

 
    
    /**
     * Method to get the before and after text surrounding the query term(s).
     * @param start <code>int</code> the start of the term relative to the entire text
     * @param end <code>int</code>  the end of the term relative to the entire text
     * @param surroundingText <code>int</code> how may terms to get before and after the query
     * @param sourcePositionMap <code>Map</code> containing <code>String</code> term positions as keys and <code>TermOffsetStart</code> as values. This gets our terms for the before and after text.
     * @return <code>String[]</code> beforeAfter[0] is the before text and beforeAfter[1] is the after text.
     */
    public String[] getHighlights(int start, int end, int surroundingText,
	    Map<String, TermOffsetStart> sourcePositionMap) {
	
	String[] beforeAfter = new String[2];

	StringBuilder before = new StringBuilder();
	for (int s = start - surroundingText; s < start; s++) {
	    String sString = Integer.toString(s);
	    if (sourcePositionMap.containsKey(sString)) {
		before.append(sourcePositionMap.get(sString).getTerm() + " ");
	    }
	}	
	beforeAfter[0] = before.toString();
	
	StringBuilder after = new StringBuilder();
	for (int e = end + 1; e < end + surroundingText; e++) {
	    String eString = Integer.toString(e);
	    if (sourcePositionMap.containsKey(eString)) {
		after.append(sourcePositionMap.get(eString).getTerm() + " ");
	    }
	}

	if (after.length() > 0) {
	    String afterSubstring = after.substring(0, after.length() - 1);
	    after = new StringBuilder();
	    after.append(" "+afterSubstring);
	}
	
	beforeAfter[1] = after.toString();

	return beforeAfter;
    }
    
    
    
    public <T> PageParameters getAnnotationPageParameters(Page<T> annotationPage, String queryString, int defaultPagingNumber, long totalHits){
   	PageParameters parameters = new PageParameters();

   	
   	parameters.setTotal((int)totalHits);
   	
   	int lastPage = (int) (totalHits/defaultPagingNumber)+1;
   	parameters.setFirstPageNumber(getPagingParam(queryString, 1));

   	parameters.setLastPageNumber(getPagingParam(queryString,lastPage));
   	parameters.setLastPage(lastPage);

   	int nextPage = (annotationPage.getNumber()/defaultPagingNumber)+2;
   	if(lastPage >= nextPage){
   	    parameters.setNextPageNumber(getPagingParam(queryString,nextPage)); 
   	    parameters.setNextPage(nextPage);
   	}
   	
   	if(annotationPage.hasPrevious() ){
   	    int previousPage = annotationPage.getNumber() /defaultPagingNumber ;
   	    parameters.setPreviousPageNumber(getPagingParam(queryString,previousPage)); 
   	}
   	parameters.setStartIndex(annotationPage.getNumber());
   	return parameters;
       }
       
       
    public String getPagingParam(String queryString, int replacementParamValue) {
	if (!queryString.contains("page=")) {
	    return queryString + "&page=" + replacementParamValue;
	}
	return queryString.replaceAll("page=[^&]+", "page=" + replacementParamValue);
    }
    
    /**
     * Method to remove any ignored parameters from the query strings
     * @param query <code>String</code> containing the paramters
     * @param paramsToRemove <code>String[]</code> of params to remove.
     * @return <code>String</code> clear of requested params
     */
    protected String removeParametersAutocompleteQuery(String query, String[] paramsToRemove){
	
	String tidyQuery = query;
	for (String param:paramsToRemove){
	    tidyQuery = tidyQuery.replaceAll("[&?]"+param+ "=[^&]+","");
	}
	if(!tidyQuery.contains("?")){
	    tidyQuery = tidyQuery.replaceFirst("[&]","?");
	}
	return tidyQuery;
    }
    
    
    
    public void amendPagingParameters(Map<String, Object> root, PageParameters pageParams, boolean isW3c){
	
	List resources = getResources(root, isW3c);
	int resourcesSize = resources.size();
	LOG.info("resourcesSize in amendPagingParameters " + resourcesSize);
	int totalElements = pageParams.getTotal();
	LOG.info("totalElements from pageParames in amendPagingParameters " + totalElements);
	int newElementsforPage = 0;
	if(resourcesSize > AnnotationSearchConstants.DEFAULT_PAGING_NUMBER){
	    newElementsforPage = resourcesSize - (AnnotationSearchConstants.DEFAULT_PAGING_NUMBER );
	}

	totalElements += newElementsforPage;
	LOG.info("totalElements in amendPagingParameters: " + totalElements);
	Map map;
	
	if(isW3c){
	    map = (LinkedHashMap) root.get(W3C_WITHIN_IS_PART_OF); 
	    if(null != map){
		map.put(W3C_WITHIN_AS_TOTALITEMS, resourcesSize);
	    }
	}else{
	    map = (LinkedHashMap) root.get(OA_WITHIN);
	    if(null != map){
		map.put(OA_WITHIN_TOTAL, resourcesSize);
	    }
	}
	
    }
    
    public void amendTotal(Map<String, Object> root, int total, boolean isW3c){
	Map map ;
	  
   	if(isW3c){
   	    map = (LinkedHashMap) root.get(W3C_WITHIN_IS_PART_OF); 
   	    map.put(W3C_WITHIN_AS_TOTALITEMS, total);
   	}else{
   	    map = (LinkedHashMap) root.get(OA_WITHIN);
   	    map.put(OA_WITHIN_TOTAL, total);
   	}
	
    }
    
    /*
    public int[] tallyPagingParameters(Map<String, Object> root, boolean isW3c, int totalElements, int startIndex){
	
   	List resources = getResources(root, isW3c);
   	
   	int resourcesSize = 0;
   	if(null != resources){
   	    resourcesSize = resources.size();
   	}
  
   	LOG.info("resourcesSize in tallyPagingParameters " + resourcesSize);
   	int[] returnArray = new int[2];
   	int newElementsforPage = resourcesSize;
   	int totalElementsTally = totalElements;
   	returnArray[1]  = startIndex + resourcesSize; 
   	totalElementsTally += newElementsforPage;
   	returnArray[0] = totalElementsTally;
   	Map map ;
  
   	if(isW3c){
   	    map = (LinkedHashMap) root.get(W3C_WITHIN_IS_PART_OF); 
   	    map.put(W3C_WITHIN_AS_TOTALITEMS, totalElementsTally);
   	}else{
   	    map = (LinkedHashMap) root.get(OA_WITHIN);
   	    map.put(OA_WITHIN_TOTAL, totalElementsTally);
   	}
   	
   	if (isW3c) {
	    root.put(W3C_STARTINDEX, startIndex);
	} else {
	    root.put(OA_STARTINDEX, startIndex);
	}
   	
   	return returnArray;
       }
    */
    
public int[] tallyPagingParameters(Map<String, Object> root, boolean isW3c, int totalElements, int startIndex){
	
	LOG.info(root.toString());
   	List resources = getResources(root, isW3c);
   	int resourcesSize = 0;
   	if(null != resources ){
   	    LOG.info("resources size in tallyPagingParameters " + resources.size());
   	    resourcesSize = resources.size();
   	}else{
   	    LOG.info("resources size in tallyPagingParameters 0 "); 
   	}
   	int extraResourcesSize = 0;
   	if(null != resources && resourcesSize > AnnotationSearchConstants.DEFAULT_PAGING_NUMBER){
   	    extraResourcesSize = resourcesSize - AnnotationSearchConstants.DEFAULT_PAGING_NUMBER;
   	} 
   	LOG.info("extraResourcesSize in tallyPagingParameters " + extraResourcesSize);
   	int[] returnArray = new int[2]; 	
 	
   	int totalElementsTally = totalElements + extraResourcesSize;  	
   	int startIndexforPage = startIndex + resourcesSize;

   	returnArray[0]  = totalElementsTally;
   	
   	returnArray[1]  = startIndexforPage; 
   	
   	Map map;
   	int existingTotal = 0;

   	if(extraResourcesSize > 0){
   	    if(isW3c){
   		map = (LinkedHashMap) root.get(W3C_WITHIN_IS_PART_OF); 
   		existingTotal = (Integer) map.get(W3C_WITHIN_AS_TOTALITEMS);
   		existingTotal += totalElementsTally;
   		map.put(W3C_WITHIN_AS_TOTALITEMS, existingTotal);
   	    }else{
   		map = (LinkedHashMap) root.get(OA_WITHIN);
   		existingTotal = (Integer) map.get(OA_WITHIN_TOTAL);
   		existingTotal += totalElementsTally;
   		map.put(OA_WITHIN_TOTAL,existingTotal);
   	    }
   	}
   	int existingStartIndex = 0;
	    if (isW3c) {
		existingStartIndex = (Integer)root.get(W3C_STARTINDEX);
		root.put(W3C_STARTINDEX, startIndex);
	    } else {
		existingStartIndex = (Integer)root.get(OA_STARTINDEX);
		root.put(OA_STARTINDEX, startIndex);
	    }
   	
   	return returnArray;
       }
    
    
    public Map<String, Object>  setSource(Map<String, Object> root, String within, String index, int maxSize){
	String type = "sc:Canvas";
	if(!index.equals("w3cannotation")){
	    type = "svcs:has_service";
	}
	Map <String, Object> gasFilter = new LinkedHashMap<>();
	root.put("gas-filter", gasFilter);
	gasFilter.put("name", "SearchResultCypherFilter");
	gasFilter.put("query", "MATCH (c:IIIF {uri:'"+ within +"'})-[:hasPart*1..]->(m:IIIF) WHERE (m.type = '"+ type +"') return DISTINCT m.uri as id");
	gasFilter.put("maxResultSize", new Integer(maxSize));
	gasFilter.put("protocol","bolt");
	LOG.info(root.toString());
   	return root;
    } 
    
    public Map<String, Object> setESSource(int from, int size, String query, String[] fields,String type) {
	Map<String, Object> root = new LinkedHashMap<>();
	Map<String, Object> queryMap = new LinkedHashMap<>();
	Map<String, Object> boolMap = new LinkedHashMap<>();
	Map<String, Object> mustMap = new LinkedHashMap<>();
	Map<String, Object> multiMatchMap = new LinkedHashMap<>();
	root.put("from", new Integer(from));
	root.put("size", new Integer(size));
	root.put("query", queryMap);
	queryMap.put("bool", boolMap);
	boolMap.put("must", mustMap);
	mustMap.put("multi_match", multiMatchMap);
	multiMatchMap.put("query", query);
	List<String>fieldList = Arrays.asList(fields);
	multiMatchMap.put("fields", fieldList);
	multiMatchMap.put("type", type);
	
   	return root;
    }
    
    /**
     * Utility method the get an int value from a Double
     * @param input {@code double}
     * @return {@code int} value
     */
    protected int removeDotZero(Double input) {
	return input.intValue();

    }
    
    public String getQueryString(String rawJson){
	LOG.info("rawJson" + rawJson);
	if(rawJson.indexOf("{") > -1 && rawJson.indexOf("}") > -1){
	    int indexOfFirstBrace = rawJson.indexOf("{");
		int indexOfLastBrace = rawJson.lastIndexOf("}");
		String amendedRawJson = rawJson.substring(indexOfFirstBrace, indexOfLastBrace+1);
		return amendedRawJson; 
	}else{
	    return "";
	}
	
	//Map<String, Object> javaRootBodyMapObject  = new Gson().fromJson(rawJson, Map.class);
    }
   
    public Map<String, Object> getQueryMap(String rawJson){
	
	String queryString = getQueryString(rawJson);
	
	Map<String, Object> jsonMap  = new Gson().fromJson(queryString, Map.class);
	if(null != jsonMap && jsonMap.containsKey("from")){
	    Double fromDouble = (Double)jsonMap.get("from"); 
	    LOG.info(fromDouble);
	    jsonMap.put("from", removeDotZero(fromDouble));
	}
	if(null != jsonMap && jsonMap.containsKey("size")){
	    Double sizeDouble = (Double)jsonMap.get("size");
	    LOG.info(sizeDouble);		
	    jsonMap.put("size", removeDotZero(sizeDouble));
	}
	return jsonMap;
    }
    
    
    public String decodeWithinUrl(String within){
	
	return new String(Base64.getDecoder().decode(within)); 
    }
    
    public Map<String,Object> returnEmptyResultSet(String queryString, boolean isW3c, PageParameters pageParams,boolean isText ){
	return this.buildEmptyAnnotationPageHead(queryString, isW3c, pageParams, true);
	
    }
    public Map<String,Object> returnEmptyAutocompleteResultSet(String queryString, String motivation, String date,String user ){
	return this.buildAutoCompleteHead(queryString,motivation, date,user);
    }
}
