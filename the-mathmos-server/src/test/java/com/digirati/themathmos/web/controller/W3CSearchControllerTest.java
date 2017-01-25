package com.digirati.themathmos.web.controller;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
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
import com.digirati.themathmos.service.OASearchService;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.service.W3CSearchService;
import com.digirati.themathmos.web.controller.OAAnnotationSearchController;

public class W3CSearchControllerTest {

    W3CSearchController controller;
    
    private W3CAnnotationSearchService w3cAnnotationSearchService;
    private TextSearchService textSearchService;
    private W3CSearchService w3cSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    HttpServletRequest request;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	w3cAnnotationSearchService = mock(W3CAnnotationSearchService.class);
	textSearchService = mock(TextSearchService.class);
	w3cSearchService = mock(W3CSearchService.class);
	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	controller = new W3CSearchController(w3cAnnotationSearchService,annotationAutocompleteService,textSearchService, w3cSearchService );
	
	request = mock(HttpServletRequest.class);
	when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/search/"));
	when(request.getQueryString()).thenReturn("q=test");
	
	
	
    }

    @Test
    public void testSearchGet() throws UnsupportedEncodingException {
	String queryNotFound = "not found";
	String motivation = null;
	String date = null;
	String user = null;
	String page = null;
	ServiceResponse notFoundResponse = new ServiceResponse(Status.NOT_FOUND, null);
	
	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/search/?q=test",true, page, false)).thenReturn(notFoundResponse);
	when(w3cAnnotationSearchService.getAnnotationPage(queryNotFound, motivation,date, user, "http://www.example.com/search/?q=test",page)).thenReturn(notFoundResponse);
	when(w3cSearchService.getAnnotationPage(queryNotFound, "http://www.example.com/search/?q=test",page)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.searchW3CGet(queryNotFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/search/?q=test",true, page, false)).thenReturn(foundResponse);	
	when(w3cAnnotationSearchService.getAnnotationPage(queryFound, motivation,date, user, "http://www.example.com/search/?q=test",page)).thenReturn(foundResponse);
	when(w3cSearchService.getAnnotationPage(queryFound, "http://www.example.com/search/?q=test",page)).thenReturn(foundResponse);
	
	responseEntity = controller.searchW3CGet(queryFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchW3CGet(queryEmpty,motivation, date, user,  page, request);
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
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/search/?q=test", true)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryNotFound, min,"http://www.example.com/search/?q=test", true)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/search/?q=test", true)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteW3CMixedGet(queryNotFound, motivation, date, user,  min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/search/?q=test", true)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryFound, min,"http://www.example.com/search/?q=test", true)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getTerms(queryFound, min,"http://www.example.com/search/?q=test", true)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteW3CMixedGet(queryFound, motivation, date, user,  min, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }
    

}