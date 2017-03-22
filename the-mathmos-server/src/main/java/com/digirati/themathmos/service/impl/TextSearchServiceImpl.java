package com.digirati.themathmos.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.mapper.TextSearchAnnotationMapper;
import com.digirati.themathmos.model.Positions;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.TermOffsetStart;
import com.digirati.themathmos.model.TermOffsetsWithPosition;
import com.digirati.themathmos.model.TermWithTermOffsets;
import com.digirati.themathmos.model.TextAnnotation;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.GetPayloadService;
import com.digirati.themathmos.service.TextSearchService;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;


@Service(TextSearchServiceImpl.SERVICE_NAME)
public class TextSearchServiceImpl implements TextSearchService {

    private static final Logger LOG = Logger.getLogger(TextSearchServiceImpl.class);

    public static final String SERVICE_NAME = "TextSearchServiceImpl";

    protected static final int DEFAULT_TEXT_PAGING_NUMBER = AnnotationSearchConstants.DEFAULT_PAGING_NUMBER;
    private static final int DEFAULT_STARTING_PAGING_NUMBER = 0;

    private TextUtils textUtils;
    private String coordinateServerUrl;
    private CacheManager cacheManager;
    
    private static final String TEXT_FIELD_NAME = "text";
    private static final String FIELD_TYPE_NAME = "text";
    private static final String TERM_VECTORS_FIELD_NAME = "term_vectors";
    private static final String TERMS_FIELD_NAME = "terms";
    private static final String POSITION_FIELD_NAME = "position";
    private static final String TOKENS_FIELD_NAME = "tokens";
    private static final String START_OFFSET_FIELD_NAME = "start_offset";
    
    private static final String INDEX_FIELD_NAME = "text_index";
    
    private PageParameters pagingParameters = null;
    
    private Client client;
    
    private GetPayloadService coordinateService;

    private long totalHits = 0;

    @Autowired
    public TextSearchServiceImpl(TextUtils textUtils, ElasticsearchTemplate template, @Value("${text.server.coordinate.url}") String coordinateServerUrl,  GetPayloadService coordinateService,
	    CacheManager cacheManager) {
	this.textUtils = textUtils;
	this.client = template.getClient();
	this.coordinateServerUrl = coordinateServerUrl;
	this.coordinateService = coordinateService;
	this.cacheManager = cacheManager;
    }
    
    @Override
    public PageParameters getPageParameters(){
	return pagingParameters;
    }
    
    @Override
    public long getTotalHits() {
	return totalHits;
    }


    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
   public ServiceResponse<Map<String, Object>> getTextPositions(String query, String queryString, boolean isW3c,
   String page, boolean isMixedSearch, String within) {
	

	totalHits = 0;
	String pageTest;
	int pageNumber = 1;
	Cache cache = cacheManager.getCache("textSearchCache");
	if ("1".equals(page) || null == page) {
	    pageTest = "";
	} else {
	    pageTest = page;
	    pageNumber = Integer.parseInt(page);
	}
	String queryWithNoPageParamter = textUtils.removeParametersAutocompleteQuery(queryString,
		new String[] { "page" });
	String queryWithAmendedPageParamter = queryWithNoPageParamter + pageTest;
	String queryWithPageParamter;
	// Call to get query from cache treat no page parameter as 1 page parameter as they are the same call.
	Cache.ValueWrapper obj = cache.get(queryWithAmendedPageParamter);

	if (null != obj) {
	    Map<String, Object> textMap = (Map) obj.get();
	    return new ServiceResponse<>(Status.OK, textMap);
	} else {
	    if (pageNumber > 1) {
		//Cache or create and cache the first page 
		Cache.ValueWrapper firstObj = cache.get(queryWithNoPageParamter);
		LOG.info("getting "+queryWithNoPageParamter + "from the cache");
		boolean isInitialSearch = false;
		if (null == firstObj) {
		    Map<String, Object> firstTextMap = getTextMap(query, queryWithNoPageParamter, isW3c, null, isMixedSearch, within);
		    if (null != firstTextMap) {
			cache.put(queryWithNoPageParamter, firstTextMap);
			firstObj = cache.get(queryWithNoPageParamter);
			isInitialSearch = true;
		    }
		}
		//We should now have the first page, so pull back the total elements and start index
		if (null != firstObj) {
		    Map<String, Object> textMap = (Map) firstObj.get();
		    int[] totalElements = textUtils.tallyPagingParameters(textMap, isW3c, 0, 0);
		    // iterate through the pages getting back 
		    for (int y = 2; y <= pageNumber; y++) {
			Map<String, Object> otherTextMaps = getTextMap(query, queryString,isW3c, Integer.toString(y), isMixedSearch, within);
			if (null != otherTextMaps) {
			    totalElements = textUtils.tallyPagingParameters(otherTextMaps,isW3c, totalElements[0],
				    totalElements[1]);
			    queryWithPageParamter = queryWithNoPageParamter + (y);
			    cache.put(queryWithPageParamter, otherTextMaps);
			} else {
			    LOG.error("No results for page "+ y + " need to get back map with no resources plus numbers ");
			    if(isInitialSearch){				
			    	textUtils.removeResources(textMap, isW3c);
			    	queryWithPageParamter = queryWithNoPageParamter + (y);
			    	cache.put(queryWithPageParamter, textMap);
			    	return new ServiceResponse<>(Status.OK, textMap);
			    }			    
			}
		    }
		    obj = cache.get(queryWithAmendedPageParamter);
		    if (null != obj) {
			Map<String, Object> requestedTextMap = (Map) obj.get();
			return new ServiceResponse<>(Status.OK, requestedTextMap);
		    }
		}else{
		    LOG.error("Error with the cache - cannot create the first non paged search results");
		}

	    } else {
		Map<String, Object> textMap = getTextMap(query, queryString, isW3c, page, isMixedSearch, within);
		if (null != textMap) {
		    cache.put(queryWithNoPageParamter, textMap);
		    return new ServiceResponse<>(Status.OK, textMap);
		} else {
		    return new ServiceResponse<>(Status.NOT_FOUND, null);
		}
	    }
	}
	return new ServiceResponse<>(Status.NOT_FOUND, null);

    }
    
    
    private Map<String, Object> getTextMap(String query, String queryString, boolean isW3c, String page, boolean isMixedSearch, String within) {
   
	totalHits = 0;
	QueryBuilder queryBuilder = buildQuery(query);

	int pagingSize = DEFAULT_TEXT_PAGING_NUMBER;
	int from = DEFAULT_STARTING_PAGING_NUMBER;

	// TODO validate that pagenumber is int and is in expected range.
	if (!StringUtils.isEmpty(page)) {
	    Integer pagingInteger = Integer.parseInt(page);
	    from = (pagingInteger.intValue() - 1) * pagingSize;
	}


	Page<TextAnnotation> annotationPage = formQuery(queryBuilder, from, pagingSize, within);

	LOG.info("total pages "+annotationPage.getTotalPages());
	
	Map<String, List<TermWithTermOffsets>> termWithOffsetsMap = new HashMap<>();
	Map<String, Map<String, TermOffsetStart>> termPositionsMap = new HashMap<>();
	Map<String, Map<String, String>> offsetPositionMap = new HashMap<>();

	extractTermOffsetsFromPage(termWithOffsetsMap, annotationPage, query, termPositionsMap, offsetPositionMap);

	LOG.info("termWithOffsetsMap "+ termWithOffsetsMap.toString());
	LOG.info("termPositionsMap "+ termPositionsMap.toString());
	Map<String, Object> offsetPayloadMap = textUtils.createOffsetPayload(termWithOffsetsMap, "1024", "768",
		offsetPositionMap);

	String payload = new Gson().toJson(offsetPayloadMap);
	LOG.info("payload "+ payload);
	pagingParameters = textUtils.getAnnotationPageParameters(annotationPage, queryString,
		DEFAULT_TEXT_PAGING_NUMBER, totalHits);

	if (null == payload || StringUtils.isEmpty(payload) || "null".equals(payload)) {
	    return null;
	} else {
	    // now call another service to get the actual coordinates
	    String coordinatePayload = coordinateService.getJsonPayload(coordinateServerUrl, payload);

	    Map<String, List<Positions>> positionMap = textUtils.getPositionsMap();
	    LOG.info("PositionMap " + positionMap.toString());

	   
	    Map<String, Object> textMap = textUtils.createCoordinateAnnotation(query, coordinatePayload,
	  isW3c, positionMap, termPositionsMap, queryString, 
		    pagingParameters,
		    isMixedSearch);
	  
	    if (null != textMap && !textMap.isEmpty()) {
		textUtils.amendPagingParameters(textMap, pagingParameters, isW3c);
		return  textMap;
	    } else {
		return null;
	    }
	}
    }
    
    
    
    /**
     * Method to get all the _id fields returned in our search and pass these to getMultiTermVectors to get a {@code MultiTermVectorsResponse}.
     * @param page {@code Page} whose content is a {@code List} of {@code TextAnnotation} objects, from which we get the ids.
     * @return {@code MultiTermVectorsResponse} containing all the term vectors in the matched text.
     */
    private MultiTermVectorsResponse getMultiTermVectorResponse(Page<TextAnnotation> page) {

	List<TextAnnotation> textPage = page.getContent();
	
	String[] idArray = new String[textPage.size()];
	int count = 0;
	for (TextAnnotation textResult : textPage) {
	    String id = textResult.getId();
	    LOG.info("getMultiTermVectorResponse id of textAnotation is " + id);
	    if(null != id){
		idArray[count] = id;
		count++;
	    }else{
		LOG.error("Error in getMultiTermVectorResponse, no id associated with this text");
	    }
	}

	return getMultiTermVectors(INDEX_FIELD_NAME, FIELD_TYPE_NAME, idArray, TEXT_FIELD_NAME);

    }

    
    
    /**
     * Method to query elasticsearch. We don't fetch the source. 
     * @param queryBuilder {@code QueryBuilder} containing the query
     * @param from {@code int} - where we want our page to start from
     * @param pagingSize {@code int} - maximum  hits we want in any page
     * @param isOneWordSearch - not required..
     * @return {@code Page} - of {@code TextAnnotation} objects.
     */
    private Page<TextAnnotation> formQuery(QueryBuilder queryBuilder, int from, int pagingSize, String within) {
	LOG.info("Page stats are from:" +from + " pagingSize:" +  pagingSize);
	Pageable pageable = new PageRequest(from, pagingSize);

	TextSearchAnnotationMapper resultsMapper = new TextSearchAnnotationMapper();
	SearchRequestBuilder searchRequestBuilderReal  = client.prepareSearch(INDEX_FIELD_NAME);

	SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX_FIELD_NAME);
	searchRequestBuilder.setQuery(queryBuilder);
	searchRequestBuilder.setFrom(from);
	searchRequestBuilder.setSize(pagingSize);
	searchRequestBuilder.setFetchSource(false);
	
	if(null != within){
   	    String decodedWithinUrl =  textUtils.decodeWithinUrl(within); 
   	
   		
   	    Map <String, Object> map = textUtils.getQueryMap(searchRequestBuilder.toString());
   	    if(null != decodedWithinUrl){
   		map = textUtils.setSource(map,decodedWithinUrl, INDEX_FIELD_NAME, pagingSize);
   		searchRequestBuilderReal.setSource(map);
   	    }else{
   	   	LOG.error("Unable to find match to within");
   	    }
   	  
   	}else{
   	    searchRequestBuilderReal = searchRequestBuilder;
   	}

		
	LOG.info("doSearch query " + searchRequestBuilder.toString());
	SearchResponse response = searchRequestBuilder.execute().actionGet();
	
	totalHits = response.getHits().totalHits();
	
	
	LOG.info("Total hits are: " + totalHits);
	LOG.info("response: " + response.toString());
	
	return resultsMapper.mapResults(response, TextAnnotation.class, pageable);
    }

    
    /**
     * Method to build a matchPhraseQuery {@code QueryBuilder}
     * @param query - The {@code String} query e.g. turnips
     * @return {@code QueryBuilder}
     */
    private QueryBuilder buildQuery(String query) {
	return QueryBuilders.matchPhraseQuery(TEXT_FIELD_NAME, query);
    }

    
    /**
     * Method to create a {@code MultiTermVectorsRequest} by building up a series of {@code TermVectorsRequest} and adding them to a {@code MultiTermVectorsRequest}. 
     * Send this request to elasticsearch and return either null or a {@code MultiTermVectorsResponse}
     * @param index - The {@code String} index we are querying.
     * @param type - The {@code String} type we are querying.
     * @param ids - The {@code String[]} of text ids we wish to get termvectors for
     * @param field - The {@code String} name of the field we wish to get termvectors for e.g. the text field.
     * @return the {@code MultiTermVectorsResponse} or null if we have an empty{@code MultiTermVectorsRequest}
     */
    private MultiTermVectorsResponse getMultiTermVectors(String index, String type, String[] ids, String field) {
	MultiTermVectorsRequest multiTermVectorsRequest = new MultiTermVectorsRequest();

	for (String id : ids) {
	    if(null != id){
		TermVectorsRequest termVectorsRequest = new TermVectorsRequest(index, type, id).selectedFields(field)
		    .termStatistics(true);
		multiTermVectorsRequest.add(termVectorsRequest);
	    }
	}

	if(!multiTermVectorsRequest.isEmpty()){
	    return client.multiTermVectors(multiTermVectorsRequest).actionGet();
	}
	return null;

    }

    private int removeDotZero(Double input) {
	return input.intValue();
    }

    private String removeDotZeroString(Double input) {
	return Integer.toString(input.intValue());
    }
    
    /**
     * Find the offsets for each query term
     * @param query - The {@code String} query e.g. turnips
     * @param builder - The {@code XContentBuilder} representing the json of the {@code MultiTermVectorsResponse}. 
     * @param termWithOffsets - Populated with the key = the lowercase query term and value a {@code List} of {@code TermOffsetsWithPosition} which are the position, and start and end offsets
     */
    private void findOffsetsForQuery(String query, XContentBuilder builder, TermWithTermOffsets termWithOffsets) {
	try {
	    Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(builder.string(), Map.class);

	    Map termVectors = (Map) javaRootBodyMapObject.get(TERM_VECTORS_FIELD_NAME);
	    LinkedTreeMap text = (LinkedTreeMap) termVectors.get(TEXT_FIELD_NAME);
	    LinkedTreeMap terms = (LinkedTreeMap) text.get(TERMS_FIELD_NAME);
	    LinkedTreeMap queryTerm = (LinkedTreeMap) terms.get(query.toLowerCase());

	    termWithOffsets.setTerm(query.toLowerCase());

	    List<TermOffsetsWithPosition> termOffsets = new ArrayList<>();
	    termWithOffsets.setOffsets(termOffsets);
	    ArrayList tokens = (ArrayList) queryTerm.get(TOKENS_FIELD_NAME);
	    for (Object token : tokens) {
		LinkedTreeMap tokenObject = (LinkedTreeMap) token;

		TermOffsetsWithPosition offsets = new TermOffsetsWithPosition();
		offsets.setPosition(removeDotZero((Double) tokenObject.get(POSITION_FIELD_NAME)));
		offsets.setEnd(removeDotZero((Double) tokenObject.get("end_offset")));
		offsets.setStart(removeDotZero((Double) tokenObject.get(START_OFFSET_FIELD_NAME)));
		termOffsets.add(offsets);
	    }

	} catch (Exception e) {
	    LOG.error("Error getting json from builderString" + e);
	}
    }
    
    
    
    
    /**
     * Method to populate a Map whose key is the position in the text and whose value is the {@code TermOffsetStart}. This contains a term and its start offset. e.g. key = 13, value = {"turnips", 34}
     * This is done for the entire text. 
     * @param builder {@code XContentBuilder} representing the json from the {@code MultiTermVectorsResponse}
     * @return {@code Map} <String, TermOffsetStart>
     */
    private Map<String, TermOffsetStart> findPositions(XContentBuilder builder) {

	Map<String, TermOffsetStart> positionMap = new HashMap<>();
	try {
	    Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(builder.string(), Map.class);
	    LOG.info(javaRootBodyMapObject.toString());
	    Map termVectors = (Map) javaRootBodyMapObject.get(TERM_VECTORS_FIELD_NAME);
	    LinkedTreeMap text = (LinkedTreeMap) termVectors.get(TEXT_FIELD_NAME);	    
	    LinkedTreeMap terms = (LinkedTreeMap) text.get(TERMS_FIELD_NAME);
	    Set<String> querySet = (Set) terms.keySet();

	    for (String term : querySet) {
		LinkedTreeMap queryTerm = (LinkedTreeMap) terms.get(term);

		ArrayList tokens = (ArrayList) queryTerm.get(TOKENS_FIELD_NAME);
		for (Object token : tokens) {
		    LinkedTreeMap tokenObject = (LinkedTreeMap) token;
		    TermOffsetStart termStart = new TermOffsetStart(term, removeDotZero((Double) tokenObject.get(START_OFFSET_FIELD_NAME)));
		    positionMap.put(removeDotZeroString((Double) tokenObject.get(POSITION_FIELD_NAME)), termStart);
		}
	    }

	} catch (Exception e) {
	    LOG.error("findPositions - Error getting json from builderString " + e);
	}
	LOG.info("positionMap is "+ positionMap.toString());

	return positionMap;
    }
    
    
    
    
    
    /**
     * Method to get the termvectors for each text item.
     * @param termWithOffsetsMap. Populated in this method with the 
     * @param page = {@code Page} containing the results of our {code
     * @param query - The {@code String} query e.g. turnips
     * @param termPositionsMap {@code Map} containing key = position of a term in text with value = {@code TermOffsetStart}
     * @param offsetPositionMap {@code Map} containing key = start offset of a term in text with value = {@code String} position.
     */
    private void extractTermOffsetsFromPage(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
	    Page<TextAnnotation> page, String query, Map<String, Map<String, TermOffsetStart>> termPositionsMap, Map<String,Map <String,String>> offsetPositionMap) {

	MultiTermVectorsResponse response = getMultiTermVectorResponse(page);

	if (null != response) {
	    MultiTermVectorsItemResponse[] itemResponseArray = response.getResponses();
	    
	    
	    for (MultiTermVectorsItemResponse itemReponse : itemResponseArray) {
		
		String imageId = itemReponse.getId();
		LOG.info("itemResponse id is " + imageId);

		XContentBuilder builder;
		try {
		    builder = XContentFactory.jsonBuilder().startObject();
		    itemReponse.getResponse().toXContent(builder, ToXContent.EMPTY_PARAMS);
		    builder.endObject();
		    LOG.info(builder.string());
		    
		    Map<String, TermOffsetStart> positions = findPositions(builder);
		    Map <String,String> startMap = new HashMap<>();
		    for(String key: positions.keySet()){
			TermOffsetStart termOffsetStart = positions.get(key);

			startMap.put(Integer.toString(termOffsetStart.getStart()), key);
		    }
		    offsetPositionMap.put(imageId, startMap);
		    termPositionsMap.put(imageId, positions);

		    // get offsets for all query terms
		    List<String> queryTerms = textUtils.getListFromSpaceSeparatedTerms(query);
		    for (String queryTerm : queryTerms) {
			TermWithTermOffsets termWithOffsets = new TermWithTermOffsets();
			findOffsetsForQuery(queryTerm, builder, termWithOffsets);
			if (termWithOffsetsMap.containsKey(imageId)) {
			    List<TermWithTermOffsets> termList = termWithOffsetsMap.get(imageId);
			    termList.add(termWithOffsets);

			} else {
			    List<TermWithTermOffsets> termList = new ArrayList<>();
			    termList.add(termWithOffsets);
			    termWithOffsetsMap.put(imageId, termList);
			}
		    }
		    LOG.info(imageId + " " + builder.string());
		} catch (IOException e) {
		    LOG.error("Error with XContentFactory.jsonBuilder().startObject()" + e);
		}
	    }
	}
    }
      

}
