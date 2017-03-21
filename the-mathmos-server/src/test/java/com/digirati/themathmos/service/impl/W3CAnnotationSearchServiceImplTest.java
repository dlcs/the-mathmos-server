package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;


import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.impl.AnnotationUtils;
import com.digirati.themathmos.service.impl.W3CAnnotationSearchServiceImpl;

public class W3CAnnotationSearchServiceImplTest {
    
    private W3CAnnotationSearchServiceImpl impl;
    AnnotationUtils annotationUtils;
    ElasticsearchTemplate template;
    Client client;
    SearchQueryUtils searchQueryUtils;
    private TextSearchService textSearchService;
    private CacheManager cacheManager;

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
	cacheManager = mock(CacheManager.class);
	impl = new W3CAnnotationSearchServiceImpl(annotationUtils, template, textSearchService, cacheManager);
    }

    @Test
    public void testGetAnnotationPage() {
	String query = "comment";
	String motivation = null;
	String date = null;
	String user = null;
	String queryString = "http://www.example.com/search?q=comment";
	String page = null;
	
	long totalHits = 20;
	searchQueryUtils.setUpBuilder(totalHits, client);
	
	
	
	
	ServiceResponse<Map<String, Object>> response = impl.getAnnotationPage(query, motivation, date, user, queryString, page, null, null);
	assertEquals(ServiceResponse.Status.NOT_FOUND,response.getStatus());
    }

}
