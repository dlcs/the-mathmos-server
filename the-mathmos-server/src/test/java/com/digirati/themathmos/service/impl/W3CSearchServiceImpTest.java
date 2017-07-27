package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.TextSearchService;
import com.google.common.collect.Iterators;

public class W3CSearchServiceImpTest {
    
    private static final Logger LOG = Logger.getLogger(W3CSearchServiceImpTest.class);
    
    private W3CSearchServiceImpl impl;
    
    String queryString = "http://www.example.com/search?q=finger";
    String queryStringWithPage = "http://www.example.com/search?q=finger&page=2";
    
    AnnotationUtils annotationUtils;
    ElasticsearchTemplate template;
    Client client;
    SearchQueryUtils searchQueryUtils;
    private TextSearchService textSearchService;
    private CacheManager cacheManager;
    Cache mixedCache;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	annotationUtils = new AnnotationUtils();
	searchQueryUtils = new SearchQueryUtils();
	template = mock(ElasticsearchTemplate.class);
	client = mock(Client.class);
	when(template.getClient()).thenReturn(client);
	textSearchService = mock(TextSearchService.class);
	mixedCache = mock(Cache.class);
	cacheManager = mock(CacheManager.class);
	when(cacheManager.getCache(anyString())).thenReturn(mixedCache);
	when(mixedCache.get(queryString)).thenReturn(null);
	Map map = new HashMap();
	mixedCache.put(queryStringWithPage, map);
	//when(mixedCache.get(queryStringWithPage)).thenReturn(new ValueWrapper())
	impl = new W3CSearchServiceImpl(annotationUtils, template, textSearchService, cacheManager);
    }

    @Test
    public void testGetAnnotationPage() {
	String query= "finger";
	
	String page =null;
	String within = null;
	String type = null;
	String widthHeight=null;
	
	long totalHits = 10;
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
	when(builder.setQuery(anyObject())).thenReturn(builder);
	when(builder.setPostFilter(anyObject())).thenReturn(builder);
	when(builder.setFrom(anyInt())).thenReturn(builder);
	when(builder.setSize(anyInt())).thenReturn(builder);
	when(builder.execute()).thenReturn(action);

	when(client.prepareSearch("w3cannotation")).thenReturn(builder);
	
	PageParameters textPagingParamters = new PageParameters();
	when(textSearchService.getPageParameters()).thenReturn(textPagingParamters);
	
	
	ServiceResponse<Map<String, Object>> serviceResponse = impl.getAnnotationPage(query, queryString, page, within, type, widthHeight);
	assertNotNull(serviceResponse);
	assertNotNull(serviceResponse.getObj());
	
	page = "2";
	serviceResponse = impl.getAnnotationPage(query, queryString, page, within, type, widthHeight);
	assertNotNull(serviceResponse);
	assertNotNull(serviceResponse.getObj());
	
	page = "2";
	serviceResponse = impl.getAnnotationPage(query, queryStringWithPage, page, within, type, widthHeight);
	assertNotNull(serviceResponse);
	assertNotNull(serviceResponse.getObj());
	
    }

}
