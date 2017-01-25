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
import com.digirati.themathmos.web.controller.OAAnnotationSearchController;

public class OASearchControllerTest {

    OASearchController controller;
    
    private OAAnnotationSearchService oaAnnotationSearchService;
    private TextSearchService textSearchService;
    private OASearchService oaSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    HttpServletRequest request;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	oaAnnotationSearchService = mock(OAAnnotationSearchService.class);
	textSearchService = mock(TextSearchService.class);
	oaSearchService = mock(OASearchService.class);
	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	controller = new OASearchController(oaAnnotationSearchService,annotationAutocompleteService,textSearchService, oaSearchService );
	
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
	
	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/search/?q=test",false, page, false)).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(queryNotFound, motivation,date, user, "http://www.example.com/search/?q=test",page)).thenReturn(notFoundResponse);
	when(oaSearchService.getAnnotationPage(queryNotFound, "http://www.example.com/search/?q=test",page)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.searchOAMixedGet(queryNotFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/search/?q=test",false, page, false)).thenReturn(foundResponse);	
	when(oaAnnotationSearchService.getAnnotationPage(queryFound, motivation,date, user, "http://www.example.com/search/?q=test",page)).thenReturn(foundResponse);
	when(oaSearchService.getAnnotationPage(queryFound, "http://www.example.com/search/?q=test",page)).thenReturn(foundResponse);
	
	responseEntity = controller.searchOAMixedGet(queryFound, motivation, date, user, page, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchOAMixedGet(queryEmpty,motivation, date, user,  page, request);
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
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryNotFound, min,"http://www.example.com/search/?q=test", false)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/search/?q=test", false)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteOAMixedGet(queryNotFound, motivation, date, user,  min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryFound, min,"http://www.example.com/search/?q=test", false)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getTerms(queryFound, min,"http://www.example.com/search/?q=test", false)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteOAMixedGet(queryFound, motivation, date, user,  min, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }
    

}