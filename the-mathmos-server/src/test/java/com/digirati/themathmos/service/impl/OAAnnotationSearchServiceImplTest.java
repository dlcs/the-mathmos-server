package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.service.TextSearchService;
import com.digirati.themathmos.service.impl.AnnotationUtils;
import com.digirati.themathmos.service.impl.OAAnnotationSearchServiceImpl;

public class OAAnnotationSearchServiceImplTest {
    
    private OAAnnotationSearchServiceImpl impl;
    AnnotationUtils annotationUtils;
    ElasticsearchTemplate template;
    Client client;
    SearchQueryUtils searchQueryUtils;
    private TextSearchService textSearchService;

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
	impl = new OAAnnotationSearchServiceImpl(annotationUtils, template, textSearchService);
    }

    @Test
    public void testGetAnnotationPage() {
	
	String query = "comment";
	String motivation = null;
	String date = null;
	String user = null;
	String queryString = "http://www.example.com/search?q=comment";
	String page = null;
	when(template.getClient()).thenReturn(client);
	long totalHits = 10;
	searchQueryUtils.setUpBuilder(totalHits, client);
	ServiceResponse<Map<String, Object>> response = impl.getAnnotationPage(query, motivation, date, user, queryString, page);
	assertEquals(ServiceResponse.Status.NOT_FOUND,response.getStatus());
	
	
    }

}
