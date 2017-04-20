package com.digirati.themathmos.web.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.OAAnnotationSearchService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.web.controller.OAAnnotationSearchController;

public class OAAnnotationSearchControllerTest {

    OAAnnotationSearchController controller;
    
    private OAAnnotationSearchService oaAnnotationSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    HttpServletRequest request;
    HttpServletRequest withinRequest;
    HttpServletRequest withinAutocompleteRequest;
    
    String encodedUrl ="http://www.google.com";
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	oaAnnotationSearchService = mock(OAAnnotationSearchService.class);
	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	controller = new OAAnnotationSearchController(oaAnnotationSearchService,annotationAutocompleteService );
	
	request = mock(HttpServletRequest.class);
	withinRequest = mock(HttpServletRequest.class) ;
	withinAutocompleteRequest = mock(HttpServletRequest.class) ;
	
	when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/search/"));
	when(request.getQueryString()).thenReturn("q=test");
	
	encodedUrl = Base64.getEncoder().encodeToString(encodedUrl.getBytes(StandardCharsets.UTF_8));
	
	
	when(withinRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+encodedUrl+"/oa/search"));
	when(withinRequest.getQueryString()).thenReturn("q=test");
	
	when(withinAutocompleteRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+encodedUrl+"/oa/autocomplete"));
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
	String queryString = "http://www.example.com/search/?q=test";
	
	Parameters params = new Parameters(queryNotFound, motivation, date, user);
	
	//when(oaAnnotationSearchService.getAnnotationPage(queryNotFound, motivation, date, user, queryString, page, null, null)).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, queryString, page, null, null)).thenReturn(notFoundResponse);
	ResponseEntity<Map<String, Object>> responseEntity = controller.searchOAGet(queryNotFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	params.setQuery(queryFound);
	
	//when(oaAnnotationSearchService.getAnnotationPage(queryFound, motivation, date, user, queryString, page, null, null)).thenReturn(foundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, queryString, page, null, null)).thenReturn(foundResponse);
	
	
	responseEntity = controller.searchOAGet(queryFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchOAGet(queryEmpty, motivation, date, user, page, request);
	}catch (SearchQueryException sqe){
	    assertNotNull(sqe);
	}
	params.setQuery(queryNotFound);
	//when(oaAnnotationSearchService.getAnnotationPage(queryNotFound, motivation, date, user, queryString, page, "within", null)).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, queryString, page, "within", null)).thenReturn(notFoundResponse);
	responseEntity = controller.searchOAGet(queryNotFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	//when(oaAnnotationSearchService.getAnnotationPage(queryNotFound, motivation, date, user, queryString, page, "within", "topic")).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, queryString, page, "within", "topic")).thenReturn(notFoundResponse);
	responseEntity = controller.searchOAGet(queryNotFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    
    @Test
    public void testAutocompleteGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String min = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteGet(queryNotFound, motivation, date, user, min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteGet(queryFound, motivation, date, user, min, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false, "within")).thenReturn(notFoundResponse);

	responseEntity = controller.autocompleteGet(queryNotFound, motivation, date, user, min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
    }
    
    @Test
    public void testSearchWithinGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String page = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	String queryString = "http://www.example.com/"+encodedUrl+"/oa/search?q=test";
	Parameters params = new Parameters(queryNotFound, motivation, date, user);
	
	//when(oaAnnotationSearchService.getAnnotationPage(queryNotFound, motivation, date, user, queryString, page, encodedUrl, null)).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, queryString, page, encodedUrl, null)).thenReturn(notFoundResponse);
	

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchWithinOAGet(encodedUrl,queryNotFound, motivation, date, user, page, withinRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	params.setQuery(queryFound);
	
	//when(oaAnnotationSearchService.getAnnotationPage(queryFound, motivation, date, user, queryString, page, encodedUrl, null)).thenReturn(foundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, queryString, page, encodedUrl, null)).thenReturn(foundResponse);
	
	
	responseEntity = controller.searchWithinOAGet(encodedUrl,queryFound, motivation, date, user, page, withinRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchWithinOAGet(encodedUrl,queryEmpty, motivation, date, user, page, withinRequest);
	}catch (SearchQueryException sqe){
	    assertNotNull(sqe);
	}
    }
    
    @Test
    public void testAutocompletewithinGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String min = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/"+encodedUrl+"/oa/autocomplete?q=test", false, encodedUrl)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteWithinGet(encodedUrl,queryNotFound, motivation, date, user, min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/"+encodedUrl+"/oa/autocomplete?q=test", false, encodedUrl)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteWithinGet(encodedUrl,queryFound, motivation, date, user, min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }

}