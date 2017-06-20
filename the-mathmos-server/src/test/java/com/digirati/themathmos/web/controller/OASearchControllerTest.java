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
import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.OAAnnotationSearchService;
import com.digirati.themathmos.service.OASearchService;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.service.impl.TextUtils;
import com.digirati.themathmos.web.controller.OAAnnotationSearchController;

public class OASearchControllerTest {

    OASearchController controller;
    
    private OAAnnotationSearchService oaAnnotationSearchService;
    private TextSearchService textSearchService;
    private OASearchService oaSearchService;
    private AnnotationAutocompleteService annotationAutocompleteService;
    HttpServletRequest request;
    HttpServletRequest withinRequest;
    HttpServletRequest withinAutocompleteRequest;
    TextUtils textUtils;
    String within ="http://www.google.com";
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	oaAnnotationSearchService = mock(OAAnnotationSearchService.class);
	textSearchService = mock(TextSearchService.class);
	when(textSearchService.getTextUtils()).thenReturn(new TextUtils());
	oaSearchService = mock(OASearchService.class);
	annotationAutocompleteService = mock(AnnotationAutocompleteService.class);
	controller = new OASearchController(oaAnnotationSearchService,annotationAutocompleteService,textSearchService, oaSearchService );
	
	within = URLEncoder.encode(within, "UTF-8");
	
	request = mock(HttpServletRequest.class);
	withinRequest = mock(HttpServletRequest.class);
	withinAutocompleteRequest = mock(HttpServletRequest.class);
	
	when(request.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/search/"));
	when(request.getQueryString()).thenReturn("q=test");
	
	when(withinRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/search/oa"));
	when(withinRequest.getQueryString()).thenReturn("q=test");
	
	when(withinAutocompleteRequest.getRequestURL()).thenReturn(new StringBuffer("http://www.example.com/"+within+"/autocomplete/oa"));
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
	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/search/?q=test",false, page, false, null, null)).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/search/?q=test",page, null, null)).thenReturn(notFoundResponse);
	when(oaSearchService.getAnnotationPage(queryNotFound, "http://www.example.com/search/?q=test",page, null, null,null)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.searchOAMixedGet(queryNotFound, motivation, date, user, page, null, null,request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	params.setQuery(queryFound);
	when(textSearchService.getTextPositions(queryFound, "http://www.example.com/search/?q=test",false, page, false, null, null)).thenReturn(foundResponse);	
	when(oaAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/search/?q=test",page, null, null)).thenReturn(foundResponse);
	when(oaSearchService.getAnnotationPage(queryFound, "http://www.example.com/search/?q=test",page, null, null, null)).thenReturn(foundResponse);
	
	responseEntity = controller.searchOAMixedGet(queryFound, motivation, date, user, page, null, null, request);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchOAMixedGet(queryEmpty,motivation, date, user,  page, null, null,request);
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
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryNotFound, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteOAMixedGet(queryNotFound, motivation, date, user,  min, request);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryFound, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getTerms(queryFound, min,"http://www.example.com/search/?q=test", false, null)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteOAMixedGet(queryFound, motivation, date, user,  min, request);
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
	
	when(textSearchService.getTextPositions(queryNotFound, "http://www.example.com/"+within+"/search/oa?q=test",false, page, false, within, null)).thenReturn(notFoundResponse);
	when(oaAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/"+within+"/search/oa?q=test",page, within, null)).thenReturn(notFoundResponse);
	when(oaSearchService.getAnnotationPage(queryNotFound, "http://www.example.com/"+within+"/search/oa?q=test",page, within, null, null)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.searchOAWithinMixedGet(within,queryNotFound, motivation, date, user, page, null, null, withinRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	params.setQuery(queryFound);
	when(textSearchService.getTextPositions(queryFound,"http://www.example.com/"+within+"/search/oa?q=test",false, page, false, within, null)).thenReturn(foundResponse);	
	when(oaAnnotationSearchService.getAnnotationPage(params, "http://www.example.com/"+within+"/search/oa?q=test",page, within, null)).thenReturn(foundResponse);
	when(oaSearchService.getAnnotationPage(queryFound, "http://www.example.com/"+within+"/search/oa?q=test",page, within, null, null)).thenReturn(foundResponse);
	
	responseEntity = controller.searchOAWithinMixedGet(within,queryFound, motivation, date, user, page, null, null,withinRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
	
	String queryEmpty = "";
	try{
	responseEntity = controller.searchOAWithinMixedGet(within,queryEmpty,motivation, date, user,  page, null, null, withinRequest);
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
	
	when(annotationAutocompleteService.getTerms(queryNotFound, motivation,date, user, min,"http://www.example.com/"+within+"/autocomplete/oa?q=test", false, within)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryNotFound, min,"http://www.example.com/"+within+"/autocomplete/oa?q=test", false, within)).thenReturn(notFoundResponse);
	when(annotationAutocompleteService.getTerms(queryNotFound, min,"http://www.example.com/"+within+"/autocomplete/oa?q=test", false, within)).thenReturn(notFoundResponse);
	
	ResponseEntity<Map<String, Object>> responseEntity = controller.autocompleteOAWithinMixedGet(within,queryNotFound, motivation, date, user,  min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().NOT_FOUND, HttpStatus.NOT_FOUND);
	
	Map<String, Object> map = new HashMap<>();
	
	ServiceResponse foundResponse = new ServiceResponse(Status.OK, map);
	
	String queryFound = "found";
	when(annotationAutocompleteService.getTerms(queryFound, motivation,date, user, min,"http://www.example.com/"+within+"/autocomplete/oa?q=test", false, within)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getMixedTerms(queryFound, min,"http://www.example.com/"+within+"/autocomplete/oa?q=test", false, within)).thenReturn(foundResponse);
	when(annotationAutocompleteService.getTerms(queryFound, min,"http://www.example.com/"+within+"/autocomplete/oa?q=test", false, within)).thenReturn(foundResponse);
	
	responseEntity = controller.autocompleteOAWithinMixedGet(within,queryFound, motivation, date, user,  min, withinAutocompleteRequest);
	assertEquals(responseEntity.getStatusCode().OK, HttpStatus.OK);
    }
    
}