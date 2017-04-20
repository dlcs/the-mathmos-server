package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.log4j.Logger;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.impl.AnnotationSearchServiceImpl;
import com.digirati.themathmos.service.impl.AnnotationUtils;
import com.google.common.collect.Iterators;


public class AnnotationSearchServiceImplTest {

    private static final Logger LOG = Logger.getLogger(AnnotationSearchServiceImplTest.class);
    AnnotationSearchServiceImpl annotationSearchServiceImpl;
    
    private ElasticsearchTemplate template;
    Client client;
    
    private TextSearchService textSearchService;
    
    private CacheManager cacheManager;

    protected AnnotationUtils annotationUtils;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

	annotationUtils = new AnnotationUtils();
	template = mock(ElasticsearchTemplate.class);
	client = mock(Client.class);
	when(template.getClient()).thenReturn(client);
	textSearchService = mock(TextSearchService.class);
	cacheManager = mock(CacheManager.class);
	annotationSearchServiceImpl = new AnnotationSearchServiceImpl(annotationUtils,template, textSearchService, cacheManager);
    }


    @Test
    public void testGetPageParameters() {
	assertNull(annotationSearchServiceImpl.getPageParameters());
    }

    @Test
    public void testGetTotalHits() {
	assertEquals(0, annotationSearchServiceImpl.getTotalHits());
    }

   
    @Test
    public void testGetAnnotationsPage() {
	String query = "comment";
	String motivation = null;
	String date = null;
	String user = null;
	String queryString = "http://www.example.com/search?q=comment";
	boolean isW3c = false;
	String page = null;
	long totalHits = 10;
	String type = null;
	String within = null;
	
	when(template.getClient()).thenReturn(client);
	
	SearchHits searchHits = mock(SearchHits.class);
	when(searchHits.getTotalHits()).thenReturn(totalHits);
	
	SearchHit[] hits = new SearchHit[1];
	SearchHit hit = mock(SearchHit.class);
	hits[0] = hit;
	when(searchHits.iterator()).thenReturn(Iterators.forArray(hits));
	
	when(hit.getSourceAsString()).thenReturn(null);

	

	SearchResponse response = mock(SearchResponse.class);
	when(response.getHits()).thenReturn(searchHits);

	ListenableActionFuture<SearchResponse> action = mock(ListenableActionFuture.class);
	when(action.actionGet()).thenReturn(response);

	SearchRequestBuilder builder = mock(SearchRequestBuilder.class);
	when(builder.setQuery(anyString())).thenReturn(builder);
	when(builder.setPostFilter(anyString())).thenReturn(builder);
	when(builder.setFrom(anyInt())).thenReturn(builder);
	when(builder.setSize(anyInt())).thenReturn(builder);
	when(builder.execute()).thenReturn(action);

	when(client.prepareSearch("w3cannotation")).thenReturn(builder);

	
	Parameters params = new Parameters(query, motivation, date, user);
	String[] results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results[0]);
	
	motivation = "non-painting";
	params.setMotivation(motivation);
	
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	motivation = "non-painting non-tagging";
	params.setMotivation(motivation);
	try{
	    results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	}catch (Exception e){
	    assertNotNull(e.getMessage());
	}
	
	
	motivation = "painting";
	params.setMotivation(motivation);
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	date = "1970-06-13T12:09:56+01:00/1970-06-13T16:09:56+01:00";
	
	params.setDate(date);
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	date = "1970-06-13T12:09:56+01:00";
	params.setDate(date);
	try{
	    results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	}catch (Exception e){
	    assertNotNull(e.getMessage());
	}
	
	date = "1970-06-13T12:09:5601:00/1970-06-13T16:09:56+01:00";
	params.setDate(date);
	try{
	    results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	}catch (Exception e){
	    assertNotNull(e.getMessage());
	}
	
	date = null;
	motivation = null;
	type = "topic";
	params.setDate(date);
	params.setMotivation(motivation);
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	type = null;
	page ="2";
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	

	page = null;
	user = "Frank";
	params.setUser(user);
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	
	user = null;
	query = null;
	motivation = "painting";
	params.setUser(user);
	params.setQuery(query);
	params.setMotivation(motivation);
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	isW3c = false;
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	within = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));
	LOG.info(within);
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	query = "comment";
	motivation = "painting";
	date = "1970-06-13T12:09:56+01:00/1970-06-13T16:09:56+01:00";
	type = "topic";
	page ="2";
	user = "Frank";
	isW3c = true;
	params.setUser(user);
	params.setQuery(query);
	params.setMotivation(motivation);
	params.setDate(date);
	within = Base64.getEncoder().encodeToString("test".getBytes(StandardCharsets.UTF_8));
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
	type = null;
	results = annotationSearchServiceImpl.getAnnotationsPage(params, queryString, isW3c, page, within, type);
	LOG.info(results);
	
    }
    
    
  

    

}
