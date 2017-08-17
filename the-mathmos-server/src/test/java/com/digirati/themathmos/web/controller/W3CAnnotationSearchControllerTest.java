package com.digirati.themathmos.web.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.service.impl.AnnotationUtils;
import com.digirati.themathmos.web.controller.W3CAnnotationSearchController;

public class W3CAnnotationSearchControllerTest {

    W3CAnnotationSearchController controller;

    private W3CAnnotationSearchService w3cAnnotationSearchService;
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

	w3cAnnotationSearchService = mock(W3CAnnotationSearchService.class);

	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	when(annotationAutocompleteService.getAnnotationUtils()).thenReturn(new AnnotationUtils());
	controller = new W3CAnnotationSearchController(w3cAnnotationSearchService,annotationAutocompleteService );

	within = URLEncoder.encode(within, "UTF-8");

	request = mock(HttpServletRequest.class);
	withinRequest = mock(HttpServletRequest.class);
	withinAutocompleteRequest = mock(HttpServletRequest.class);

	when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/search/"));
	when(request.getQueryString()).thenReturn("q=test");

	when(withinRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/w3c/search"));
	when(withinRequest.getQueryString()).thenReturn("q=test");

	when(withinAutocompleteRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/w3c/autocomplete"));
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
	Parameters params = new Parameters(queryNotFound, motivation, date, user);
	//when(w3cAnnotationSearchService.getAnnotationPage(queryNotFound, motivation,date, user, "http://www.example.com/search/?q=test",page, null, null)).thenReturn(notFoundResponse);
	when(w3cAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/search/?q=test",page, null, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchGet(queryNotFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	params.setQuery(queryFound);
	//when(w3cAnnotationSearchService.getAnnotationPage(queryFound, motivation,date, user, "http://www.example.com/search/?q=test",page, null, null)).thenReturn(foundResponse);
	when(w3cAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/search/?q=test",page, null, null)).thenReturn(foundResponse);

	responseEntity = controller.searchGet(queryFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);

	String queryEmpty = "";
	try{
	responseEntity = controller.searchGet(queryEmpty, motivation, date, user, page, request);
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

	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/search/?q=test", true, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteGet(queryNotFound, motivation, date, user, min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/search/?q=test", true, null)).thenReturn(foundResponse);

	responseEntity = controller.autocompleteGet(queryFound, motivation, date, user, min, request);
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
	Parameters params = new Parameters(queryNotFound, motivation, date, user);

	//when(w3cAnnotationSearchService.getAnnotationPage(queryNotFound, motivation,date, user, "http://www.example.com/"+within+"/w3c/search?q=test",page, within, null)).thenReturn(notFoundResponse);
	when(w3cAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/"+within+"/w3c/search?q=test",page, within, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchWithinGet(within,queryNotFound, motivation, date, user, page, withinRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	params.setQuery(queryFound);
	//when(w3cAnnotationSearchService.getAnnotationPage(queryFound, motivation,date, user, "http://www.example.com/"+within+"/w3c/search?q=test",page, within, null)).thenReturn(foundResponse);
	when(w3cAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/"+within+"/w3c/search?q=test",page, within, null)).thenReturn(foundResponse);

	responseEntity = controller.searchWithinGet(within,queryFound, motivation, date, user, page, withinRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);

	String queryEmpty = "";
	try{
	responseEntity = controller.searchWithinGet(within,queryEmpty, motivation, date, user, page, withinRequest);
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

	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/"+within+"/w3c/autocomplete?q=test", true, within)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteWithinGet(within,queryNotFound, motivation, date, user, min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/"+within+"/w3c/autocomplete?q=test", true, within)).thenReturn(foundResponse);

	responseEntity = controller.autocompleteWithinGet(within,queryFound, motivation, date, user, min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }

}
