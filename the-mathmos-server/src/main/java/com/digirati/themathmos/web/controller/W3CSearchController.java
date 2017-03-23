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
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.service.W3CSearchService;




@RestController(W3CSearchController.CONTROLLER_NAME)
public class W3CSearchController {
    
    
    public static final String CONTROLLER_NAME = "w3cSearchController";
    
    private W3CAnnotationSearchService w3cAnnotationSearchService;
    private TextSearchService textSearchService;
    private W3CSearchService w3cSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    
    private ControllerUtility controllerUtility;
    
    @Autowired
    public W3CSearchController(W3CAnnotationSearchService w3cAnnotationSearchService,AnnotationAutocompleteService annotationAutocompleteService,
	    TextSearchService textSearchService,W3CSearchService searchService
	    ) {
	
        this.w3cAnnotationSearchService = w3cAnnotationSearchService;
        this.annotationAutocompleteService = annotationAutocompleteService;
        this.textSearchService = textSearchService;
        this.w3cSearchService = searchService;
        this.controllerUtility = new ControllerUtility();
    }
    
    
    //autocomplete parameter defaults to 1 if not specified
    public static final String PARAM_MIN = "min";
    
    
    private static final String W3C_MIXED_SEARCH_REQUEST_PATH = "/search/w3c";     
    private static final String W3C_MIXED_AUTOCOMPLETE_REQUEST_PATH = "/autocomplete/w3c";
    
    private static final String WITHIN_W3C_MIXED_SEARCH_REQUEST_PATH = "/{withinId}/search/w3c";     
    private static final String WITHIN_W3C_MIXED_AUTOCOMPLETE_REQUEST_PATH = "/{withinId}/autocomplete/w3c";
    
    
    @CrossOrigin
    @RequestMapping(value = W3C_MIXED_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchW3CGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = false) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_WIDTH, required = false) String width,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_HEIGHT, required = false) String height,
	    HttpServletRequest request) {
	

	String queryString = controllerUtility.createQueryString(request);
	String type = null;
	String widthHeight = null;
	if(!StringUtils.isEmpty(width) && !StringUtils.isEmpty(height)){
	    widthHeight = width+"|" + height;
	}
	if(StringUtils.isEmpty(query) && StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    throw new SearchQueryException(AnnotationSearchConstants.EMPTY_QUERY_MESSAGE);	    
	}
	ServiceResponse<Map<String, Object>> serviceResponse;
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = w3cSearchService.getAnnotationPage(query, queryString, page, null, type, widthHeight);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = textSearchService.getTextPositions(query, queryString, true, page, false, null, widthHeight);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = w3cAnnotationSearchService.getAnnotationPage(query, motivation, date, user, queryString, page, null, type); 
	    }else{
		serviceResponse = w3cSearchService.getAnnotationPage(query, queryString, page, null, type, widthHeight);
	    }
	}
	

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    
    @CrossOrigin
    @RequestMapping(value = WITHIN_W3C_MIXED_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchW3CWithinGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = false) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_WIDTH, required = false) String width,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_HEIGHT, required = false) String height,
	    HttpServletRequest request) {
	
	String within = withinId;
	String type = null;
	String widthHeight = null;
	if(!StringUtils.isEmpty(width) && !StringUtils.isEmpty(height)){
	    widthHeight = width+"|" + height;
	}
	String queryString = controllerUtility.createQueryString(request);

	if(StringUtils.isEmpty(query) && StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    throw new SearchQueryException(AnnotationSearchConstants.EMPTY_QUERY_MESSAGE);	    
	}
	ServiceResponse<Map<String, Object>> serviceResponse;
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = w3cSearchService.getAnnotationPage(query, queryString, page, within, type, widthHeight);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = textSearchService.getTextPositions(query, queryString, true, page, false, within, widthHeight);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = w3cAnnotationSearchService.getAnnotationPage(query, motivation, date, user, queryString, page, within, type); 
	    }else{
		serviceResponse = w3cSearchService.getAnnotationPage(query, queryString, page, within, type, widthHeight);
	    }
	}
	

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    
    
    

    @RequestMapping(value = W3C_MIXED_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteW3CMixedGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {
	
	
	String queryString = controllerUtility.createQueryString(request);
	
	ServiceResponse<Map<String, Object>> serviceResponse;
	
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, true, null);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = annotationAutocompleteService.getTerms(query, min, queryString, true, null);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = annotationAutocompleteService.getTerms(query, motivation, date, user, min, queryString, true, null);
	    }else{
		serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, true, null);
	    }
	}
	
	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

    }
    
    @RequestMapping(value = WITHIN_W3C_MIXED_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteW3CWithinMixedGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date, 
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user, 
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {
	
	String within = withinId;
	
	String queryString = controllerUtility.createQueryString(request);
	
	ServiceResponse<Map<String, Object>> serviceResponse;
	
	if(StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, true, within);	    
	}else{
	    if(AnnotationSearchConstants.PAINTING_MOTIVATION.equals(motivation)){
		serviceResponse = annotationAutocompleteService.getTerms(query, min, queryString, true, within);
	    }else if(motivation.indexOf(AnnotationSearchConstants.PAINTING_MOTIVATION) < 0){		
		serviceResponse = annotationAutocompleteService.getTerms(query, motivation, date, user, min, queryString, true, within);
	    }else{
		serviceResponse = annotationAutocompleteService.getMixedTerms(query, min, queryString, true, within);
	    }
	}
	
	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

    }

}
