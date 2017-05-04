package com.digirati.themathmos.service.impl;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.w3c.SuggestOption;
import com.digirati.themathmos.service.AnnotationAutocompleteService;



@Service(AnnotationAutocompleteServiceImpl.SERVICE_NAME)
public class AnnotationAutocompleteServiceImpl implements AnnotationAutocompleteService{
    
    
    public static final String SERVICE_NAME = "annotationAutocompleteServiceImpl";
    
    private static final String TEXT_INDEX = AnnotationSearchConstants.TEXT_INDEX_NAME;
    private static final String W3C_INDEX = "w3cannotation";
    
    public static final int MAX_NUMBER_OF_HITS_RETURNED = 1000;
    
    private static final Logger LOG = Logger.getLogger(AnnotationAutocompleteServiceImpl.class);
    
  
    private AnnotationUtils annotationUtils;
    
    private Client client;
    
    @Autowired
    public AnnotationAutocompleteServiceImpl(ElasticsearchTemplate template, AnnotationUtils annotationUtils){
	this.client = template.getClient();
	this.annotationUtils = annotationUtils;
    }


    @Override
    @Cacheable(value = "autocompleteCache", key = "#queryString.toString()+#isW3c.toString()" )
    public ServiceResponse<Map<String, Object>> getTerms(String query, String motivation, String date, String user, String min, String queryString, boolean isW3c, String within) {
    
	List<SuggestOption> options = findSuggestionsFor(query, W3C_INDEX, within);
	
	if(options.isEmpty()){
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date,user);
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }
    
    @Override
    @Cacheable(value = "autocompleteCache", key = "#queryString.toString()+#isW3c.toString()" )
    public ServiceResponse<Map<String, Object>> getTerms(Parameters parameters, String min, String queryString, boolean isW3c, String within) {
    
	List<SuggestOption> options = findSuggestionsFor(parameters.getQuery(), W3C_INDEX, within);
	
	if(options.isEmpty()){
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(options, isW3c, queryString, parameters.getMotivation(), parameters.getDate(),parameters.getUser());
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }
    
    @Override
    @Cacheable(value = "autocompleteCache", key = "#queryString.toString()+#isW3c.toString()" )
    public ServiceResponse<Map<String, Object>> getTerms(String query,String min, String queryString, boolean isW3c, String within) {

	List<SuggestOption> options = findSuggestionsFor(query, TEXT_INDEX, within);

	if(options.isEmpty()){
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(options, isW3c, queryString, null, null,null);
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }
    
    @Override
    @Cacheable(value = "mixedAutocompleteCache", key = "#queryString.toString()+#isW3c.toString()" )
    public ServiceResponse<Map<String, Object>> getMixedTerms(String query, String min, String queryString, boolean isW3c, String within){
	
	List<SuggestOption> textOptions = findSuggestionsFor(query, TEXT_INDEX,  within);

	List<SuggestOption> annoOptions = findSuggestionsFor(query, W3C_INDEX,  within);
	
	textOptions.addAll(annoOptions);
	
	if(textOptions.isEmpty()){
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	}else{
	    
	    Map<String, Object> annoTermList =  annotationUtils.createAutocompleteList(textOptions, isW3c, queryString, null, null,null);
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}	
    }
   
    public List <SuggestOption>  findSuggestionsFor(String suggestRequest, String index,  String within)  {
	CompletionSuggestionBuilder  completionSuggestionBuilder = new CompletionSuggestionBuilder("annotation_suggest");
	
	completionSuggestionBuilder.text(suggestRequest);
	completionSuggestionBuilder.field("suggest");
	completionSuggestionBuilder.size(MAX_NUMBER_OF_HITS_RETURNED);
			
	LOG.info(completionSuggestionBuilder.toString());
	
	// need a new SearchRequestBuilder or the source does not change
   	SearchRequestBuilder searchRequestBuilderReal  = client.prepareSearch(index);	
	SearchRequestBuilder searchRequestBuilder  = client.prepareSearch(index);
   	searchRequestBuilder.addSuggestion(completionSuggestionBuilder);
   	searchRequestBuilder.setSize(1);
   	searchRequestBuilder.setFetchSource(false);
   	
   	if(null != within){
   	    String decodedWithinUrl =  annotationUtils.decodeWithinUrl(within); 
   	
   		
   	    Map <String, Object> map = annotationUtils.getQueryMap(searchRequestBuilder.toString());
   	    if(null != decodedWithinUrl && null != map){
   		map = annotationUtils.setSource(map,decodedWithinUrl, index, 1);
   		searchRequestBuilderReal.setSource(map);
   	    }else{
   	   	LOG.error("Unable to find match to within");
   	    }
   	}else{
   	    searchRequestBuilderReal = searchRequestBuilder;
   	}

   	LOG.info("doSearch query "+ searchRequestBuilderReal.toString());
   	
   	SearchResponse searchResponse = searchRequestBuilderReal.execute()
   		.actionGet();
   	
   	
	CompletionSuggestion compSuggestion = searchResponse.getSuggest().getSuggestion("annotation_suggest");

        List<CompletionSuggestion.Entry> entryList = compSuggestion.getEntries();
        
        List <SuggestOption> options = new ArrayList<>();
        if(entryList != null) {
            CompletionSuggestion.Entry entry = entryList.get(0);
            List<CompletionSuggestion.Entry.Option> csEntryOptions =entry.getOptions();
            if(null != csEntryOptions && !csEntryOptions.isEmpty())  {
        	Iterator <? extends  CompletionSuggestion.Entry.Option> iter = csEntryOptions.iterator();
        	while (iter.hasNext()) {
        	    CompletionSuggestion.Entry.Option next = iter.next();
        	    SuggestOption option = new SuggestOption(next.getText().string());
        	    LOG.info("option " + option.getText());
        	    options.add(option); 
        	}  
           }
        }
	
 
       return options;
    }
    
   

}
