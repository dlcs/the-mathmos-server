package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;


import com.digirati.themathmos.service.impl.AnnotationSearchServiceImpl;
import com.digirati.themathmos.service.impl.AnnotationUtils;
import com.google.common.collect.Iterators;


public class AnnotationSearchServiceImplTest {

    private final static Logger LOG = Logger.getLogger(AnnotationSearchServiceImplTest.class);
    AnnotationSearchServiceImpl annotationSearchServiceImpl;
    
    private ElasticsearchTemplate template;
    Client client;

    protected AnnotationUtils annotationUtils;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

	annotationUtils = new AnnotationUtils();
	template = mock(ElasticsearchTemplate.class);
	client = mock(Client.class);
	annotationSearchServiceImpl = new AnnotationSearchServiceImpl(annotationUtils,template );
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

	String[] results = annotationSearchServiceImpl.getAnnotationsPage(query, motivation, date, user, queryString, isW3c, page);
	LOG.info(results[0]);
	
	//motivation = "commenting";
	//results = annotationSearchServiceImpl.getAnnotationsPage(query, motivation, date, user, queryString, isW3c, page);

    }
    
    
  

    

}
