package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.service.impl.AnnotationAutocompleteServiceImpl;
import com.digirati.themathmos.service.impl.AnnotationUtils;



@RunWith(PowerMockRunner.class)
@PrepareForTest(SuggestResponse.class)
public class AnnotationAutocompleteServiceImplTest {
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

    @Test
    public void testGetTerms() {
	String query = null;
	String motivation = null;
	String date = null;
	String user = null;
	String min = null;
	String queryString = null;
	
	SuggestResponse response = PowerMock.createMock(SuggestResponse.class);
	SuggestRequestBuilder suggestRequestBuilder = mock(SuggestRequestBuilder.class);
	
	when(client.prepareSuggest(anyString())).thenReturn(suggestRequestBuilder);
	when(suggestRequestBuilder.addSuggestion(anyObject())).thenReturn(suggestRequestBuilder);
	
	ListenableActionFuture<SuggestResponse> action = mock(ListenableActionFuture.class);
	

        when(suggestRequestBuilder.execute()).thenReturn(action);

	when(action.actionGet()).thenReturn(response);
	
	
	List<Suggestion<? extends Entry<? extends Option>>> suggestions = new ArrayList<>();
	Suggestion suggestion = new Suggestion();
	Text text = new Text("fingers");
	
	Entry entry = new Entry(text, 1,2);
	Option option = new Option();
	entry.addOption(option);
	suggestion.addTerm(entry);
	suggestions.add(suggestion);
	//Suggest suggest = new Suggest(suggestions);
	
	Suggest suggest = mock(Suggest.class);

	//when(reponse.getSuggest()).thenReturn(suggest);
	EasyMock.expect(response.getSuggest()).andReturn(suggest);

	//when(response.getSuggest()).thenReturn(suggest);
	//ServiceResponse<Map<String, Object>> serviceResponse = impl.getTerms(query, motivation, date, user, min, queryString, true);
    }

   

}
