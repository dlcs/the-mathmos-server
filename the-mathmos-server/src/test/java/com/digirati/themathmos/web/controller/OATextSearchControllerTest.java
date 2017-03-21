package com.digirati.themathmos.web.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.OAAnnotationSearchService;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.web.controller.OAAnnotationSearchController;

public class OATextSearchControllerTest {

    OATextSearchController controller;
    
    private TextSearchService textSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    HttpServletRequest request;
    

    HttpServletRequest withinRequest;
    HttpServletRequest withinAutocompleteRequest;
    
    String within ="http://www.google.com";
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	textSearchService = mock(TextSearchService.class);
	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	controller = new OATextSearchController(textSearchService,annotationAutocompleteService );
	
	within = URLEncoder.encode(within, "UTF-8");
	
	request = mock(HttpServletRequest.class);
	withinRequest = mock(HttpServletRequest.class);
	withinAutocompleteRequest = mock(HttpServletRequest.class);
	
	when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/search/"));
	when(request.getQueryString()).thenReturn("q=test");
	
	when(withinRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/oa/text/search"));
	when(withinRequest.getQueryString()).thenReturn("q=test");
	
	when(withinAutocompleteRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/oa/text/autocomplete"));
	when(withinAutocompleteRequest.getQueryString()).thenReturn("q=test");
	
	
	
    }

    @Test
    public void testSearchGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String page = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/search/?q=test",false, page, false, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchTextOAGet(queryNotFound, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/search/?q=test",false, page, false, null)).thenReturn(foundResponse);
	
	responseEntity = controller.searchTextOAGet(queryFound, page, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchTextOAGet(queryEmpty, page, request);
	}catch (SearchQueryException sqe){
	    assertNotNull(sqe);
	}
    }
    
    @Test
    public void testAutocompleteGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String min = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteTextOAGet(queryNotFound,  min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteTextOAGet(queryFound,  min, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }
    

    @Test
    public void testSearchWithinGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String page = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/"+within+"/oa/text/search?q=test",false, page, false, within)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchTextWithinOAGet(within,queryNotFound, page, withinRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/"+within+"/oa/text/search?q=test",false, page, false, within)).thenReturn(foundResponse);
	
	responseEntity = controller.searchTextWithinOAGet(within, queryFound, page, withinRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchTextWithinOAGet(within, queryEmpty, page, withinRequest);
	}catch (SearchQueryException sqe){
	    assertNotNull(sqe);
	}
    }
    
    @Test
    public void testAutocompleteWithinGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String min = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/"+within+"/oa/text/autocomplete?q=test", false, within)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteTextWithinOAGet(within,queryNotFound,  min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, min,"http://www.example.com/"+within+"/oa/text/autocomplete?q=test", false, within)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteTextWithinOAGet(within,queryFound,  min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }
    
}