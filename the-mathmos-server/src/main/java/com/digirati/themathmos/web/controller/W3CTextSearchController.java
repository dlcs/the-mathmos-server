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
import com.digirati.themathmos.service.TextSearchService;




@RestController(W3CTextSearchController.CONTROLLER_NAME)
public class W3CTextSearchController {
    
    
    public static final String CONTROLLER_NAME = "w3CTextSearchController";
    
    private TextSearchService textSearchService;
 
    
    @Autowired
    public W3CTextSearchController(TextSearchService textSearchService ) {
        this.textSearchService = textSearchService;

    }

    private static final String TEXT_SEARCH_REQUEST_PATH = "/w3c/text/search";   
    
    
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
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchService.getTextPositions(query, queryString, true, page);

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
