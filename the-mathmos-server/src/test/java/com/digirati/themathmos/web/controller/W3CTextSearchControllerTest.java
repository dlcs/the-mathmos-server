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
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.service.impl.TextUtils;
import com.digirati.themathmos.web.controller.W3CAnnotationSearchController;

public class W3CTextSearchControllerTest {

    W3CTextSearchController controller;

    private TextSearchService textSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    HttpServletRequest request;
    TextUtils textUtils;

    HttpServletRequest withinRequest;
    HttpServletRequest withinAutocompleteRequest;

    String within ="http://www.google.com";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

	textSearchService = mock(TextSearchService.class);
	when(textSearchService.getTextUtils()).thenReturn(new TextUtils());
	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	controller = new W3CTextSearchController(textSearchService,annotationAutocompleteService );

	within = URLEncoder.encode(within, "UTF-8");

	request = mock(HttpServletRequest.class);
	withinRequest = mock(HttpServletRequest.class);
	withinAutocompleteRequest = mock(HttpServletRequest.class);

	when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/search/"));
	when(request.getQueryString()).thenReturn("q=test");

	when(withinRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/w3c/text/search"));
	when(withinRequest.getQueryString()).thenReturn("q=test");

	when(withinAutocompleteRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/w3c/text/autocomplete"));
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

	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/search/?q=test",true, page, false, null, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchTextGet(queryNotFound, page, null, null, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/search/?q=test",true, page, false, null, null)).thenReturn(foundResponse);

	responseEntity = controller.searchTextGet(queryFound, page, null, null,request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);

	String queryEmpty = "";
	try{
	responseEntity = controller.searchTextGet(queryEmpty, page,null, null, request);
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

	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/search/?q=test", true, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteTextW3CGet(queryNotFound, min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound,  min,"http://www.example.com/search/?q=test", true, null)).thenReturn(foundResponse);

	responseEntity = controller.autocompleteTextW3CGet(queryFound, min, request);
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

	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/"+within+"/w3c/text/search?q=test",true, page, false, within, null)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.searchTextWithinGet(within, queryNotFound, page, null, null,withinRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/"+within+"/w3c/text/search?q=test",true, page, false, within, null)).thenReturn(foundResponse);

	responseEntity = controller.searchTextWithinGet(within, queryFound, page, null, null,withinRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);

	String queryEmpty = "";
	try{
	responseEntity = controller.searchTextWithinGet(within, queryEmpty, page,null, null, withinRequest);
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

	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/"+within+"/w3c/text/autocomplete?q=test", true, within)).thenReturn(notFoundResponse);

	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteTextWithinW3CGet(within,queryNotFound, min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);

	Map<String, Object> map = new HashMap<>();

	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);

	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound,  min,"http://www.example.com/"+within+"/w3c/text/autocomplete?q=test", true, within)).thenReturn(foundResponse);

	responseEntity = controller.autocompleteTextWithinW3CGet(within,queryFound, min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }


}
