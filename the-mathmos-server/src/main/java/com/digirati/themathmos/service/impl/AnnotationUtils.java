package com.digirati.themathmos.service.impl;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.model.annotation.w3c.SuggestOption;
import com.digirati.themathmos.model.annotation.w3c.W3CAnnotation;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;



@Service(AnnotationUtils.SERVICE_NAME)
public class AnnotationUtils extends CommonUtils{
    
    private final static Logger LOG = Logger.getLogger(AnnotationUtils.class);
   
    public static final String SERVICE_NAME = "AnnotationUtils";  
    
    
    private static final String OA_SEARCH_TERMLIST = "search:TermList";
 
   
    
    
    private static final String[] AUTOCOMPLETE_IGNORE_PARAMETERS = new String[] 
	    {AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, AnnotationSearchConstants.PARAM_FIELD_USER, AnnotationSearchConstants.PARAM_FIELD_DATE};
    
    private static final String[] SCHEMES = new String[]{"http", "https", "ftp", "mailto", "file", "data"};
    
    
   

    public Map<String, Object> createAnnotationPage(String query, List<W3CAnnotation> annoList, boolean isW3c,
	    PageParameters pageParams, long totalHits, boolean isMixedSearch) {

	if (null == annoList || annoList.isEmpty()) {
	    return null;
	}

	Map<String, Object> root;
	
	if(isMixedSearch){
	    root = new LinkedHashMap<>();
	    this.setResources(root, isW3c);	    
	}else{
	    if (AnnotationSearchServiceImpl.DEFAULT_PAGING_NUMBER <= totalHits) {
		root = this.buildAnnotationPageHead(query, isW3c, pageParams);
	    } else {
		root = this.buildAnnotationListHead(query, isW3c);
	    } 
	}
	

	List resources = this.getResources(root, isW3c);

	// forEach result in the search get the annotation from the database and
	// populate resource element.
	for (W3CAnnotation w3CAnnotation : annoList) {
	    resources.add(w3CAnnotation.getJsonMap());
	}

	return root;

    }
    
  
    public Map<String,Object> createAutocompleteList(List <SuggestOption> options , boolean isW3c, String queryString,String motivation, String date, String user){
	
	if(null == options || options.isEmpty()){
	    return null;
	}
	try{
	 
	    Map<String, Object> root = this.buildAutoCompleteHead(queryString, isW3c,motivation, date,user);
	    List resources ;
	    if (isW3c){
		LinkedTreeMap map = (LinkedTreeMap)root.get(FULL_HAS_TERMLIST);
		resources = (List)map.get(W3C_RESOURCELIST);
	    }else{
		resources = (List)root.get(OA_TERMSLIST);
	    }
	    
	    
	    //forEach result in the search get the annotation from the database and populate resource element.
	    for(SuggestOption option:options){
		Map<String, Object> optionRoot = new HashMap<>();
		String optionText = option.getText();
		optionRoot.put("match",optionText );

		optionRoot.put("url",getSearchQueryFromAutocompleteQuery(queryString, optionText) );
		
		if(!resources.contains(optionRoot)){
		    resources.add(optionRoot);  
		}
	    }

	    LOG.info("resources are " + JsonUtils.toString(root));
	    return root;
	}catch (IOException ioe){
	   LOG.error("Error getting default file " + ioe);
	    return null;
	}	
    }
    
    /**
     * Method to create the terms urls will replace
     * @param query
     * @param optionText
     * @return
     */
    private String getSearchQueryFromAutocompleteQuery(String query, String optionText){
	
	String searchQuery = query;
	searchQuery = searchQuery.replace("autocomplete","search");
	
	String tidyQuery = removeParametersAutocompleteQuery(searchQuery,AUTOCOMPLETE_IGNORE_PARAMETERS);
	String encodedOptionText = optionText;
	try {
	    encodedOptionText = URLEncoder.encode(optionText, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    
	   LOG.error(String.format("Autocomplete: unable to encode [%s]", optionText), e);
	}

	return tidyQuery.replaceAll("q=[^&]+","q="+encodedOptionText);
    }
    
    
   
    
   
    
    
    @SuppressWarnings("unchecked") 
    protected Map<String, Object> buildAutoCompleteHead(String query, boolean isW3c,String motivation, String date, String user) {
	

	Map<String, Object> root = new HashMap<>();
	if(isW3c){
	    root.put(CONTEXT, WC3CONTEXT_PATH);
	}else{
	    root.put(CONTEXT, SEARCHCONTEXT_PATH);
	}
	String queryWithRemovedIgnoredParamters = removeParametersAutocompleteQuery(query,AUTOCOMPLETE_IGNORE_PARAMETERS);
	root.put(ROOT_ID, queryWithRemovedIgnoredParamters);
	
	if(isW3c){
	    root.put(ROOT_TYPE, FULL_HAS_TERMLIST);
	}else{
	    root.put(ROOT_TYPE, OA_SEARCH_TERMLIST);
	}
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
	
	if(isW3c){
	   LinkedTreeMap map = new LinkedTreeMap();
	   root.put(FULL_HAS_TERMLIST, map);
	   map.put(W3C_RESOURCELIST, resources);
	   
	}else{
	    root.put(OA_TERMSLIST, resources); 
	}

	return root;
    }
    
    
    
    
    
    
    
    public List<W3CAnnotation > getW3CAnnotations(String[] annotationArray){
   	if(null != annotationArray && annotationArray.length > 0){
   	    List<W3CAnnotation> annoList  = new ArrayList<>();
   	    for(String anno:annotationArray){
   		W3CAnnotation annotation  = new W3CAnnotation();
   		LOG.info(String.format("The annotation is [%s] ", anno));
   		if(null != anno){
   		    Map<String, Object> javaRootMapObject = new Gson().fromJson(anno, Map.class);
   		    annotation.setJsonMap(javaRootMapObject);
   		    annoList.add(annotation); 
   		}
   	    }
   	    return annoList;
   	}
   	return new ArrayList<>();
    }
    
    public String convertSpecialCharacters(String input){

	 String returnInput = input;
	if(null != input && input.contains(":")){
	   
	    List<String> inputList = getListFromSpaceSeparatedTerms(input);
	    for (String replacement : inputList) {
		
		String start = replacement.toLowerCase().substring(0, replacement.indexOf(":"));
		String tidyQuery;
		if(ArrayUtils.contains(SCHEMES, start)){
		    tidyQuery = "(\"" +replacement + "\")";
		}else{
		    tidyQuery = replacement.replaceAll(":", "\\\\:");
		}
		returnInput = returnInput.replace(replacement, tidyQuery);
	    }
	}else{
	    return input;
	}
   	
	return returnInput;
       }
       
    
    
    public int[] getPageParams(Map<String, Object> root, boolean isW3c){
	Map map;
	String total = "" ;

	List resources = getResources(root, isW3c);
   	
   	int resourcesSize = resources.size();
   	LOG.info("resourcesSize in getPageParams" + resourcesSize);

	String startIndex = "" ;
	int[] pageParams = new int[2];
	if(isW3c){
   	    map = (LinkedHashMap) root.get("dcterms:isPartOf"); 
   	    total = (String) map.get("as:totalItems");
   	}else{
   	    map = (LinkedHashMap) root.get("within");
   	    total = (String) map.get("total");
   	}
	pageParams[0] =  Integer.parseInt(total);
   	
   	if (isW3c) {
   	    startIndex = (String)root.get("as:startIndex");
	} else {
	    startIndex = (String)root.get("startIndex");
	}
   	pageParams[1] = Integer.parseInt(startIndex); //startIndex.intValue()
   		 
   	
   	return pageParams;
    }
   
   
 

}
