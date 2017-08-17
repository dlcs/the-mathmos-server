package com.digirati.themathmos.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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




@RestController(OATextSearchController.CONTROLLER_NAME)
public class OATextSearchController{


    public static final String CONTROLLER_NAME = "OATextSearchController";

    private TextSearchService textSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    private ControllerUtility controllerUtility;
    private TextUtils textUtils;
  //autocomplete parameter defaults to 1 if not specified
    public static final String PARAM_MIN = "min";


    @Autowired
    public OATextSearchController(TextSearchService textSearchService, AnnotationAutocompleteService annotationAutocompleteService ) {
        this.textSearchService = textSearchService;
        this.annotationAutocompleteService = annotationAutocompleteService;
        this.controllerUtility = new ControllerUtility();
        this.textUtils = this.textSearchService.getTextUtils();
    }

    private static final String OA_TEXT_SEARCH_REQUEST_PATH = "/oa/text/search";
    private static final String OA_TEXT_AUTOCOMPLETE_REQUEST_PATH = "/oa/text/autocomplete";

    private static final String WITHIN_OA_TEXT_SEARCH_REQUEST_PATH = "/{withinId}/oa/text/search";
    private static final String WITHIN_OA_TEXT_AUTOCOMPLETE_REQUEST_PATH = "/{withinId}/oa/text/autocomplete";

    @CrossOrigin
    @RequestMapping(value = OA_TEXT_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchTextOAGet(
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
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchService.getTextPositions(query, queryString, false, page, false, null, widthHeight);

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return new ResponseEntity<Map<String,Object>>(serviceResponse.getObj(), controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    Map <String, Object> emptyMap = textUtils.returnEmptyResultSet(queryString,false, new PageParameters(),true);
	    return new ResponseEntity<Map<String,Object>>(emptyMap, controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }

    @CrossOrigin
    @RequestMapping(value = WITHIN_OA_TEXT_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> searchTextWithinOAGet(
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
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchService.getTextPositions(query, queryString, false, page, false, within, widthHeight);

	Status serviceResponseStatus = serviceResponse.getStatus();

	if (serviceResponseStatus.equals(Status.OK)) {
	     return new ResponseEntity<Map<String,Object>>(serviceResponse.getObj(), controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
	    Map <String, Object> emptyMap = textUtils.returnEmptyResultSet(queryString,false, new PageParameters(),true);
	    return new ResponseEntity<Map<String,Object>>(emptyMap, controllerUtility.getResponseHeaders(),  HttpStatus.OK);
	}

	throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
    }


    @RequestMapping(value = OA_TEXT_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteTextOAGet(
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query,
	    @RequestParam(value = PARAM_MIN, required = false) String min,
	    HttpServletRequest request) {

	String queryString = controllerUtility.createQueryString(request);

	ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query,  min, queryString, false, null);

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

    @RequestMapping(value = WITHIN_OA_TEXT_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> autocompleteTextWithinOAGet(
	    @PathVariable String withinId,
	    @RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query,
	    @RequestParam(value = PARAM_MIN, required = false) String min,
	    HttpServletRequest request) {

	String queryString = controllerUtility.createQueryString(request);
	String within = withinId;
	ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query,  min, queryString, false, within);

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
