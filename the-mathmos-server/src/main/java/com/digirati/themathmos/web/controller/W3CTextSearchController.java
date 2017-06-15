package com.digirati.themathmos.web.controller;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.exception.SearchException;
import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.impl.TextUtils;




@RestController(W3CTextSearchController.CONTROLLER_NAME)
public class W3CTextSearchController {
    
    
    public static final String CONTROLLER_NAME = "w3CTextSearchController";
    
    private TextSearchService textSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    private TextUtils textUtils;
    private ControllerUtility controllerUtility;
    
    @Autowired
    public W3CTextSearchController(TextSearchService textSearchService, AnnotationAutocompleteService annotationAutocompleteService) {
        this.textSearchService = textSearchService;
        this.annotationAutocompleteService = annotationAutocompleteService;
        this.controllerUtility = new ControllerUtility();
        this.textUtils = this.textSearchService.getTextUtils();
    }
    
    //autocomplete parameter defaults to 1 if not specified
    public static final String PARAM_MIN = "min";

    private static final String TEXT_SEARCH_REQUEST_PATH = "/w3c/text/search";       
    private static final String W3C_TEXT_AUTOCOMPLETE_REQUEST_PATH = "/w3c/text/autocomplete";
    
    private static final String WITHIN_TEXT_SEARCH_REQUEST_PATH = "/{withinId}/w3c/text/search";       
    private static final String WITHIN_W3C_TEXT_AUTOCOMPLETE_REQUEST_PATH = "/{withinId}/w3c/text/autocomplete";
    
    
    
    @CrossOrigin
    @RequestMapping(value = TEXT_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchTextGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 	
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_WIDTH, required = false) String width,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_HEIGHT, required = false) String height,
	    HttpServletRequest request) {

	String queryString = controllerUtility.createQueryString(request);
	String widthHeight = null;
	if(!StringUtils.isEmpty(width) && !StringUtils.isEmpty(height)){
	    widthHeight = width+"|" + height;
	}
	if(StringUtils.isEmpty(query)){
	    throw new SearchQueryException("Please enter a query to search");
	}
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchService.getTextPositions(query, queryString, true, page, false, null, widthHeight);

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	    return new ResponseEntity<Map<String,Object>>(serviceResponse.getObj(), controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    Map <String, Object> emptyMap = textUtils.returnEmptyResultSet(queryString,true, new PageParameters(),true);
	    return new ResponseEntity<Map<String,Object>>(emptyMap, controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    @CrossOrigin
    @RequestMapping(value = WITHIN_TEXT_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchTextWithinGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 	
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_WIDTH, required = false) String width,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_HEIGHT, required = false) String height,
	    HttpServletRequest request) {

	String queryString = controllerUtility.createQueryString(request);
	String within = withinId;
	
	String widthHeight = null;
	if(!StringUtils.isEmpty(width) && !StringUtils.isEmpty(height)){
	    widthHeight = width+"|" + height;
	}
	if(StringUtils.isEmpty(query)){
	    throw new SearchQueryException("Please enter a query to search");
	}
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchService.getTextPositions(query, queryString, true, page, false, within, widthHeight);

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	    return new ResponseEntity<Map<String,Object>>(serviceResponse.getObj(), controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    Map <String, Object> emptyMap = textUtils.returnEmptyResultSet(queryString,true, new PageParameters(),true);
	    return new ResponseEntity<Map<String,Object>>(emptyMap, controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    
    @RequestMapping(value = W3C_TEXT_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteTextW3CGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query,  
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {
	
	String queryString = controllerUtility.createQueryString(request);

	ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query, min, queryString, true, null);
	
	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	    return new ResponseEntity<Map<String,Object>>(serviceResponse.getObj(), controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    Map <String, Object> emptyMap = textUtils.returnEmptyAutocompleteResultSet(queryString,null,null,null);
	    return new ResponseEntity<Map<String,Object>>(emptyMap, controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

    }
    
    @RequestMapping(value = WITHIN_W3C_TEXT_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteTextWithinW3CGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query,  
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {
	
	String queryString = controllerUtility.createQueryString(request);
	String within = withinId;
	ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query, min, queryString, true, within);
	
	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	    return new ResponseEntity<Map<String,Object>>(serviceResponse.getObj(), controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    Map <String, Object> emptyMap = textUtils.returnEmptyAutocompleteResultSet(queryString,null,null,null);
	    return new ResponseEntity<Map<String,Object>>(emptyMap, controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

    }
    
    
 
    
  

}
