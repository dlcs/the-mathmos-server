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
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.digirati.themathmos.mapper.TextSearchAnnotationMapper;
import com.digirati.themathmos.model.Positions;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.TermOffsetStart;
import com.digirati.themathmos.model.TermOffsetsWithPosition;
import com.digirati.themathmos.model.TermWithTermOffsets;
import com.digirati.themathmos.model.TextAnnotation;
import com.digirati.themathmos.model.W3CSearchAnnotation;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.GetPayloadService;
import com.digirati.themathmos.service.TextSearchService;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

@Service(TextSearchServiceImpl.SERVICE_NAME)
public class TextSearchServiceImpl implements TextSearchService {

    private final static Logger LOG = Logger.getLogger(TextSearchServiceImpl.class);

    public static final String SERVICE_NAME = "TextSearchServiceImpl";

    // protected static final int DEFAULT_PAGING_NUMBER = 10;
    protected static final int DEFAULT_PAGING_NUMBER = 3;
    private static final int DEFAULT_STARTING_PAGING_NUMBER = 0;

    private TextUtils textUtils;
    private String coordinateServerUrl;
    
    private static final String TEXT_FIELD_NAME = "text";
    private static final String FIELD_TYPE_NAME = "text";
    private static final String INDEX_FIELD_NAME = "text_index";
    
    private PageParameters pagingParameters = null;
    

    private Client client;
    
    private GetPayloadService coordinateService;

    private long totalHits = 0;

    @Autowired
    public TextSearchServiceImpl(TextUtils textUtils, ElasticsearchTemplate template, @Value("${text.server.coordinate.url}") String coordinateServerUrl,  GetPayloadService coordinateService) {
	this.textUtils = textUtils;
	this.client = template.getClient();
	this.coordinateServerUrl = coordinateServerUrl;
	this.coordinateService = coordinateService;
    }
    
    public PageParameters getPageParameters(){
	return pagingParameters;
    }
    

    public long getTotalHits() {
	return totalHits;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
    public ServiceResponse<Map<String, Object>> getTextPositions(String query, String queryString, boolean isW3c, String page) {

	totalHits = 0;
	QueryBuilder queryBuilder = buildQuery(query);
	
	int pagingSize = DEFAULT_PAGING_NUMBER;
	int pageNumber = DEFAULT_STARTING_PAGING_NUMBER;
	
	//TODO validate that pagenumber is int and is in expected range.
	if(!StringUtils.isEmpty(page)){
	    Integer pagingInteger =  Integer.parseInt(page);
	    pageNumber = pagingInteger.intValue() - 1;
	}
	
	//Map <String, Text[]>hitsMapper = new HashMap<>();
	
	List<String> queryTerms = textUtils.getListFromSpaceSeparatedTerms(query);
	boolean isOneWordSearch = queryTerms.size() == 1 ? true:false;
	Page<TextAnnotation> annotationPage = formQuery(queryBuilder, pageNumber, pagingSize, 
		//hitsMapper, 
		isOneWordSearch);

	int totalPages = annotationPage.getTotalPages();
	
	LOG.info(annotationPage.getTotalPages());

	Map<String, List<TermWithTermOffsets>> termWithOffsetsMap = new HashMap<>();
	Map<String, Map<String, TermOffsetStart>> termPositionsMap = new HashMap<>();
	Map<String,Map <String,String>> offsetPositionMap = new HashMap<>();
	
	extractTermOffsetsFromPage(termWithOffsetsMap, annotationPage,  query, termPositionsMap, offsetPositionMap);
	
	/*
	if(totalPages > 1){
	    for (int x = 2; x < totalPages + 1; x++){
		
		int from = DEFAULT_PAGING_NUMBER *(x-1);
		annotationPage = formQuery(queryBuilder,from, DEFAULT_PAGING_NUMBER, 
			//hitsMapper, 
			isOneWordSearch);
		
		LOG.info(annotationPage.getTotalPages());
		extractTermOffsetsFromPage(termWithOffsetsMap, annotationPage,  query, termPositionsMap, offsetPositionMap);
	    }
	}*/
	
	
	LOG.info(termWithOffsetsMap.toString());
	Map <String, Object> offsetPayloadMap = textUtils.createOffsetPayload(query,termWithOffsetsMap, "1024", "768", offsetPositionMap);
	
	Map <String, List<Positions>> positionMap = textUtils.getPositionsMap();
	LOG.info("PositionMap " + positionMap.toString());
	String payload = new Gson().toJson(offsetPayloadMap);
	
	pagingParameters = textUtils.getAnnotationPageParameters(annotationPage, queryString, DEFAULT_PAGING_NUMBER, totalHits);
	
	if (StringUtils.isEmpty(payload)) {
	    return new ServiceResponse<>(Status.NOT_FOUND, null);
	}else{
	    // now call another service to get the actual coordinates
	    String coordinatePayload = coordinateService.getJsonPayload(coordinateServerUrl, payload);
	    
	    Map<String, Object> textMap = textUtils.createCoordinateAnnotation(query,coordinatePayload,
		   // hitsMapper, 
		    isW3c, positionMap, termPositionsMap, queryString, this.getTotalHits(), pagingParameters);
	    
	    
	    if (null != textMap && !textMap.isEmpty()) {
		   return new ServiceResponse<>(Status.OK, textMap);
	    } else {
		    return new ServiceResponse<>(Status.NOT_FOUND, null);
	    }
	}
		
    }

    
    
    
    private MultiTermVectorsResponse getMultiTermVectorResponse(Page<TextAnnotation> page) {

	List<TextAnnotation> textPage = page.getContent();
	
	String[] idArray = new String[textPage.size()];
	int count = 0;
	for (TextAnnotation textResult : textPage) {
	    String id = textResult.getId();
	    LOG.info("id of textAnotation is " + id);
	    if(null != id){
		idArray[count] = id;
		count++;
	    }else{
		LOG.error("no id associated with this text");
	    }
	}

	MultiTermVectorsResponse response = getMultiTermVectors(INDEX_FIELD_NAME, FIELD_TYPE_NAME, idArray, TEXT_FIELD_NAME);
	return response;

    }

    
    
    
    private Page<TextAnnotation> formQuery(QueryBuilder queryBuilder, int pageNumber, int pagingSize, 
	    //Map <String, Text[]>hitsMapper, 
	    boolean isOneWordSearch) {
	LOG.info("Page stats are pageNumber:" +pageNumber + " pagingSize:" +  pagingSize);
	Pageable pageable = new PageRequest(pageNumber, pagingSize);

	TextSearchAnnotationMapper resultsMapper = new TextSearchAnnotationMapper();

	SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX_FIELD_NAME);
	searchRequestBuilder.setQuery(queryBuilder);
	searchRequestBuilder.setFrom(pageNumber).setSize(pagingSize);
	//searchRequestBuilder.addHighlightedField(TEXT_FIELD_NAME, 150, 1000);
	searchRequestBuilder.setFetchSource(false);
	//only use fvh highlighting if we are searching > 1 word in a phrase.
	//if(!isOneWordSearch){
	//    searchRequestBuilder.setHighlighterType("fvh");
	//}
	
	
	LOG.info("doSearch query " + searchRequestBuilder.toString());
	SearchResponse response = searchRequestBuilder.execute().actionGet();
	
	//examineHitsForHighlights(response,hitsMapper);
	
	totalHits = response.getHits().totalHits();
	
	
	LOG.info("Total hits are: " + totalHits);
	LOG.info("response: " + response.toString());
	
	return resultsMapper.mapResults(response, TextAnnotation.class, pageable);
    }

    private QueryBuilder buildQuery(String query) {
	QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery(TEXT_FIELD_NAME, query);
	return queryBuilder;
    }

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
	    MultiTermVectorsResponse response = client.multiTermVectors(multiTermVectorsRequest).actionGet();
	    LOG.info(response.toString());
	    return response;
	}
	return null;

    }

    private int removeDotZero(Double input) {
	return input.intValue();

    }

    
    private void findOffsetsForQuery(String query, XContentBuilder builder, TermWithTermOffsets termWithOffsets){
	try {
	    Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(builder.string(), Map.class);

	    Map termVectors = (Map) javaRootBodyMapObject.get("term_vectors");
	    LinkedTreeMap text = (LinkedTreeMap) termVectors.get(TEXT_FIELD_NAME);
	    LinkedTreeMap terms = (LinkedTreeMap) text.get("terms");
	    LinkedTreeMap queryTerm = (LinkedTreeMap) terms.get(query.toLowerCase());

	    termWithOffsets.setTerm(query.toLowerCase());

	    List<TermOffsetsWithPosition> termOffsets = new ArrayList<>();
	    termWithOffsets.setOffsets(termOffsets);
	    ArrayList tokens = (ArrayList) queryTerm.get("tokens");
	    for (Object token : tokens) {
		LinkedTreeMap tokenObject = (LinkedTreeMap) token;

		TermOffsetsWithPosition offsets = new TermOffsetsWithPosition();
		offsets.setPosition(removeDotZero((Double) tokenObject.get("position")));
		offsets.setEnd(removeDotZero((Double) tokenObject.get("end_offset")));
		offsets.setStart(removeDotZero((Double) tokenObject.get("start_offset")));
		termOffsets.add(offsets);
	    }

	} catch (Exception e) {
	    LOG.error("Error getting json from builderString" + e);
	}
    }
    
    private Map<String, TermOffsetStart> findPositions(XContentBuilder builder) {

	Map<String, TermOffsetStart> positionMap = new HashMap<>();
	try {
	    Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(builder.string(), Map.class);

	    Map termVectors = (Map) javaRootBodyMapObject.get("term_vectors");
	    LinkedTreeMap text = (LinkedTreeMap) termVectors.get(TEXT_FIELD_NAME);
	    LinkedTreeMap terms = (LinkedTreeMap) text.get("terms");

	    Set<String> querySet = (Set) terms.keySet();

	    for (String term : querySet) {
		LinkedTreeMap queryTerm = (LinkedTreeMap) terms.get(term);

		ArrayList tokens = (ArrayList) queryTerm.get("tokens");
		for (Object token : tokens) {
		    LinkedTreeMap tokenObject = (LinkedTreeMap) token;

		    TermOffsetStart termStart = new TermOffsetStart();

		    positionMap.put(removeDotZero((Double) tokenObject.get("position")) + "", termStart);

		    termStart.setTerm(term);
		    termStart.setStart(removeDotZero((Double) tokenObject.get("start_offset")));
		}
	    }

	} catch (Exception e) {
	    LOG.error("Error getting json from builderString" + e);
	}
	LOG.info(positionMap.toString());

	return positionMap;
    }
    
    private void extractTermOffsetsFromPage(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
	    Page<TextAnnotation> page, String query, Map<String, Map<String, TermOffsetStart>> termPositionsMap, Map<String,Map <String,String>> offsetPositionMap) {

	MultiTermVectorsResponse response = getMultiTermVectorResponse(page);

	if (null != response) {
	    MultiTermVectorsItemResponse[] itemResponseArray = response.getResponses();
	    
	    
	    for (MultiTermVectorsItemResponse itemReponse : itemResponseArray) {

		String id = itemReponse.getId();
		LOG.info("itemResponse id is " + id);
		
		

		XContentBuilder builder;
		try {
		    builder = XContentFactory.jsonBuilder().startObject();
		    itemReponse.getResponse().toXContent(builder, ToXContent.EMPTY_PARAMS);
		    builder.endObject();
		    
		    
		    Map<String, TermOffsetStart> positions = findPositions(builder);
		    Map <String,String> startMap = new HashMap<>();
		    for(String key: positions.keySet()){
			TermOffsetStart termOffsetStart = positions.get(key);
			int start  = termOffsetStart.getStart();
			startMap.put(start+"", key);
		    }
		    offsetPositionMap.put(id, startMap);
		    termPositionsMap.put(id, positions);

		    // get offsets for all query terms
		    List<String> queryTerms = textUtils.getListFromSpaceSeparatedTerms(query);
		    for (String queryTerm : queryTerms) {
			TermWithTermOffsets termWithOffsets = new TermWithTermOffsets();
			findOffsetsForQuery(queryTerm, builder, termWithOffsets);
			if (termWithOffsetsMap.containsKey(id)) {
			    List<TermWithTermOffsets> termList = termWithOffsetsMap.get(id);
			    termList.add(termWithOffsets);

			} else {
			    List<TermWithTermOffsets> termList = new ArrayList<>();
			    termList.add(termWithOffsets);
			    termWithOffsetsMap.put(id, termList);
			}
		    }
		    LOG.info(id + " " + builder.string());
		} catch (IOException e) {
		    LOG.error("Error with XContentFactory.jsonBuilder().startObject()" + e);
		}

	    }

	}

    }
    
    public void examineHitsForHighlights(SearchResponse response,Map <String, Text[]>hitsMapper ){
	
         for (SearchHit searchHit : response.getHits()) {
             if (response.getHits().getHits().length <= 0) {
                 break;
             }
                     
             HighlightField hightlightField = searchHit.getHighlightFields().get(TEXT_FIELD_NAME);
            
             Text[] fragments = hightlightField.getFragments();
            // for(Text fragment:fragments){
        	// getBeforeAndAfterFromText(fragment);
            // }
             hitsMapper.put(searchHit.getId(), fragments);

         }  
    }
    
   

}
