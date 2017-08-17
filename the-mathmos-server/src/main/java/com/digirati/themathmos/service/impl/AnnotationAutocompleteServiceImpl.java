package com.digirati.themathmos.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
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
public class AnnotationAutocompleteServiceImpl implements AnnotationAutocompleteService {

    public static final String SERVICE_NAME = "annotationAutocompleteServiceImpl";

    private static final String TEXT_INDEX = AnnotationSearchConstants.TEXT_INDEX_NAME;
    private static final String W3C_INDEX = AnnotationSearchConstants.W3C_INDEX_NAME;

    private static final Logger LOG = Logger.getLogger(AnnotationAutocompleteServiceImpl.class);

    private AnnotationUtils annotationUtils;

    private Client client;

    @Autowired
    public AnnotationAutocompleteServiceImpl(ElasticsearchTemplate template, AnnotationUtils annotationUtils) {
	this.client = template.getClient();
	this.annotationUtils = annotationUtils;
    }

    @Override
    public AnnotationUtils getAnnotationUtils() {
	return this.annotationUtils;
    }

    @Override
    @Cacheable(value = "autocompleteCache", key = "#queryString.toString()+#isW3c.toString()")
    public ServiceResponse<Map<String, Object>> getTerms(String query, String motivation, String date, String user,
	    String min, String queryString, boolean isW3c, String within) {

	List<SuggestOption> options = findSuggestionsFor(query, W3C_INDEX, within);

	if (options.isEmpty()) {
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	} else {

	    Map<String, Object> annoTermList = annotationUtils.createAutocompleteList(options, isW3c, queryString,
		    motivation, date, user);
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }

    @Override
    @Cacheable(value = "autocompleteCache", key = "#queryString.toString()+#isW3c.toString()")
    public ServiceResponse<Map<String, Object>> getTerms(Parameters parameters, String min, String queryString,
	    boolean isW3c, String within) {

	List<SuggestOption> options = findSuggestionsFor(parameters.getQuery(), W3C_INDEX, within);

	if (options.isEmpty()) {
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	} else {

	    Map<String, Object> annoTermList = annotationUtils.createAutocompleteList(options, isW3c, queryString,
		    parameters.getMotivation(), parameters.getDate(), parameters.getUser());
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }

    @Override
    @Cacheable(value = "autocompleteCache", key = "#queryString.toString()+#isW3c.toString()")
    public ServiceResponse<Map<String, Object>> getTerms(String query, String min, String queryString, boolean isW3c,
	    String within) {

	List<SuggestOption> options = findSuggestionsFor(query, TEXT_INDEX, within);

	if (options.isEmpty()) {
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	} else {

	    Map<String, Object> annoTermList = annotationUtils.createAutocompleteList(options, isW3c, queryString, null,
		    null, null);
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }

    @Override
    @Cacheable(value = "mixedAutocompleteCache", key = "#queryString.toString()+#isW3c.toString()")
    public ServiceResponse<Map<String, Object>> getMixedTerms(String query, String min, String queryString,
	    boolean isW3c, String within) {

	List<SuggestOption> textOptions = findSuggestionsFor(query, TEXT_INDEX, within);

	List<SuggestOption> annoOptions = findSuggestionsFor(query, W3C_INDEX, within);

	textOptions.addAll(annoOptions);

	if (textOptions.isEmpty()) {
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	} else {

	    Map<String, Object> annoTermList = annotationUtils.createAutocompleteList(textOptions, isW3c, queryString,
		    null, null, null);
	    return new ServiceResponse<>(Status.OK, annoTermList);
	}
    }

    public List<SuggestOption> findSuggestionsFor(String suggestRequest, String index, String within) {
	CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion("suggest")
		.prefix(suggestRequest).size(AnnotationSearchConstants.MAX_NUMBER_OF_HITS_RETURNED);

	String decodedWithinUrl = null;
	if (null != within) {
	    decodedWithinUrl = annotationUtils.decodeWithinUrl(within);
	    if (null != decodedWithinUrl) {
		LOG.info("decodedWithinUrl :" + decodedWithinUrl);

		Map<String, List<? extends ToXContent>> contextsMap = new HashMap<>();
		List<CategoryQueryContext> contexts = new ArrayList<>(1);
		contexts.add(CategoryQueryContext.builder().setCategory(decodedWithinUrl).build());
		contextsMap.put(AnnotationSearchConstants.CONTEXT_MANIFEST_NAME, contexts);
		completionSuggestionBuilder.contexts(contextsMap);
	    }
	}

	completionSuggestionBuilder.size(AnnotationSearchConstants.MAX_NUMBER_OF_HITS_RETURNED);

	LOG.info(completionSuggestionBuilder.toString());

	SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);

	searchRequestBuilder
		.suggest(new SuggestBuilder().addSuggestion("annotation_suggest", completionSuggestionBuilder));
	searchRequestBuilder.setSize(0);
	searchRequestBuilder.setFetchSource(false);

	LOG.info("doSearch query " + searchRequestBuilder.toString());

	SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

	CompletionSuggestion compSuggestion = searchResponse.getSuggest().getSuggestion("annotation_suggest");

	List<SuggestOption> options = new ArrayList<>();
	Set<String> suggestOptionSet = new TreeSet<>();
	if (null != compSuggestion) {
	    List<CompletionSuggestion.Entry> entryList = compSuggestion.getEntries();

	    if (entryList != null) {
		CompletionSuggestion.Entry entry = entryList.get(0);
		List<CompletionSuggestion.Entry.Option> csEntryOptions = entry.getOptions();
		if (null != csEntryOptions && !csEntryOptions.isEmpty()) {
		    Iterator<? extends CompletionSuggestion.Entry.Option> iter = csEntryOptions.iterator();
		    while (iter.hasNext()) {
			CompletionSuggestion.Entry.Option next = iter.next();
			suggestOptionSet.add(next.getText().string());
		    }
		}
	    }
	}

	if (null != suggestOptionSet) {
	    for (String setOption : suggestOptionSet) {
		SuggestOption option = new SuggestOption(setOption);
		options.add(option);
		LOG.info("option " + option.getText());
	    }
	}
	return options;
    }

}
