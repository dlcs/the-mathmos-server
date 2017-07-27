package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.suggest.Suggest;


import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion.Entry;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion.Entry.Option;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.service.impl.AnnotationAutocompleteServiceImpl;
import com.digirati.themathmos.service.impl.AnnotationUtils;




public class AnnotationAutocompleteServiceImplTest {
    
    private static final Logger LOG = Logger.getLogger(AnnotationAutocompleteServiceImplTest.class);
    AnnotationAutocompleteServiceImpl impl;
    
    private ElasticsearchTemplate template;
    private AnnotationUtils annotationUtils;
    Client client;

    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	template = mock(ElasticsearchTemplate.class);
	annotationUtils = new AnnotationUtils();
	client = mock(Client.class);
	when(template.getClient()).thenReturn(client);
	impl = new AnnotationAutocompleteServiceImpl(template, annotationUtils);
    }

    /*  @Test
   public void testGetTerms() {
	String query = "fingers";
	String motivation = null;
	String date = null;
	String user = null;
	String min = null;
	String queryString = "http://www.example.com/search?q=fingers";
	
	SearchResponse response = mock(SearchResponse.class);
	SearchRequestBuilder suggestRequestBuilder = mock(SearchRequestBuilder.class);
	
	when(client.prepareSearch(anyString())).thenReturn(suggestRequestBuilder);
	when(suggestRequestBuilder.addSuggestion(anyObject())).thenReturn(suggestRequestBuilder);
	
	ListenableActionFuture<SearchResponse> action = mock(ListenableActionFuture.class);
	

        when(suggestRequestBuilder.execute()).thenReturn(action);

	when(action.actionGet()).thenReturn(response);
	
	
	List<CompletionSuggestion> suggestions = new ArrayList<>();
	CompletionSuggestion suggestion = mock(CompletionSuggestion.class);
	Text text = new Text(query);
	
	Entry entry = new Entry(text, 1,2);
	Option option = new Option(text,(float)1, null);
	
	entry.addOption(option);
	suggestion.addTerm(entry);

	List<Entry> entries = new ArrayList<>();
	entries.add(entry);
	when(suggestion.getEntries()).thenReturn(entries);
	suggestions.add(suggestion);
	
	
	Suggest suggest = mock(Suggest.class);
	when(response.getSuggest()).thenReturn(suggest);
	when(suggest.getSuggestion("annotation_suggest")).thenReturn(suggestion);


	ServiceResponse<Map<String, Object>> serviceResponse = impl.getTerms(query, motivation, date, user, min, queryString, true, null);
	assertNotNull(serviceResponse.getObj());
    }
*/
   

}
