package com.digirati.themathmos.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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




@RestController(OATextSearchController.CONTROLLER_NAME)
public class OATextSearchController {
    
    
    public static final String CONTROLLER_NAME = "OATextSearchController";
    
    private TextSearchService textSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    
  //autocomplete parameter defaults to 1 if not specified
    public static final String PARAM_MIN = "min";
 
    
    @Autowired
    public OATextSearchController(TextSearchService textSearchService, AnnotationAutocompleteService annotationAutocompleteService ) {
        this.textSearchService = textSearchService;
        this.annotationAutocompleteService = annotationAutocompleteService;
    }

    private static final String TEXT_SEARCH_REQUEST_PATH = "/oa/text/search";   
    
    
    private static final String OA_AUTOCOMPLETE_REQUEST_PATH = "/oa/text/autocomplete";
    
    @CrossOrigin
    @RequestMapping(value = TEXT_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchTextGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 	
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
	    HttpServletRequest request) {
	//TODO implement xy parameters here to pass back to Text Server. 
	String queryString = request.getRequestURL().toString();
	if(null != request.getQueryString()){
	    queryString += "?"+ request.getQueryString();
	}
	if(StringUtils.isEmpty(query)){
	    throw new SearchQueryException("Please enter a query to search");
	}
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchService.getTextPositions(query, queryString, false, page, false);

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return ResponseEntity.ok(serviceResponse.getObj());
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }
    
    
    @RequestMapping(value = OA_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query, 
	    @RequestParam(value = PARAM_MIN, required = false) String min, 
	    HttpServletRequest request) {
	
	String queryString = request.getRequestURL().toString();
	if(null != request.getQueryString()){
	    queryString += "?"+ request.getQueryString();
	}
	
	ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query,  min, queryString, false);
	
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
