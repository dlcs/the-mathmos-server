package com.digirati.themathmos.service.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.w3c.SuggestOption;
import com.digirati.themathmos.service.AnnotationAutocompleteService;



@Service(AnnotationAutocompleteServiceImpl.SERVICE_NAME)
public class AnnotationAutocompleteServiceImpl implements AnnotationAutocompleteService{
    
    
    public static final String SERVICE_NAME = "annotationAutocompleteServiceImpl";
    
    public static final int MAX_NUMBER_OF_HITS_RETURNED = 1000;
    
    private final static Logger LOG = Logger.getLogger(AnnotationAutocompleteServiceImpl.class);
    
  
    private AnnotationUtils annotationUtils;
    
    private Client client;
    
    @Autowired
    public AnnotationAutocompleteServiceImpl(ElasticsearchTemplate template, AnnotationUtils annotationUtils){
	this.client = template.getClient();
	this.annotationUtils = annotationUtils;
    }


    @Override
    public ServiceResponse<Map<String, Object>> getTerms(String query, String motivation, String date, String user, String min, String queryString, boolean isW3c) {
	

	List <SuggestOption> options = findSuggestionsFor(query, "w3cannotation") ;
	
	if(options.isEmpty()){
	    return new ServiceResponse<Map<String, Object>>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date,user);
	    return new ServiceResponse<Map<String, Object>>(Status.OK, annoTermList);
	}
    }
    
    @Override
    public ServiceResponse<Map<String, Object>> getTerms(String query,String min, String queryString, boolean isW3c) {
	

	List <SuggestOption> options = findSuggestionsFor(query, "text_index") ;
	
	if(options.isEmpty()){
	    return new ServiceResponse<Map<String, Object>>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(options, isW3c, queryString, null, null,null);
	    return new ServiceResponse<Map<String, Object>>(Status.OK, annoTermList);
	}
    }
    
    @Override
    public ServiceResponse<Map<String, Object>> getMixedTerms(String query, String min, String queryString, boolean isW3c){
	
	List <SuggestOption> textOptions = findSuggestionsFor(query, "text_index") ;
	List <SuggestOption> annoOptions = findSuggestionsFor(query, "w3cannotation") ;
	
	textOptions.addAll(annoOptions);
	
	if(textOptions.isEmpty()){
	    return new ServiceResponse<Map<String, Object>>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(textOptions, isW3c, queryString, null, null,null);
	    return new ServiceResponse<Map<String, Object>>(Status.OK, annoTermList);
	}	
    }
   
    public List <SuggestOption>  findSuggestionsFor(String suggestRequest, String index) {
	CompletionSuggestionBuilder  completionSuggestionBuilder = new CompletionSuggestionBuilder("annotation_suggest");
	
	completionSuggestionBuilder.text(suggestRequest);
	completionSuggestionBuilder.field("suggest");
	completionSuggestionBuilder.size(MAX_NUMBER_OF_HITS_RETURNED);
			
	LOG.info(completionSuggestionBuilder.toString());
	
        SuggestRequestBuilder suggestRequestBuilder =
                client.prepareSuggest(index).addSuggestion(completionSuggestionBuilder);

        SuggestResponse suggestResponse = suggestRequestBuilder.execute().actionGet();

        Iterator<? extends Suggest.Suggestion.Entry.Option> iterator =
                suggestResponse.getSuggest().getSuggestion("annotation_suggest").iterator().next().getOptions().iterator();

        List <SuggestOption> options = new ArrayList<SuggestOption>();
        
        while (iterator.hasNext()) {
            Suggest.Suggestion.Entry.Option next = iterator.next();
            SuggestOption option = new SuggestOption(next.getText().string());
            options.add(option);
        }
       return options;
    }

}
