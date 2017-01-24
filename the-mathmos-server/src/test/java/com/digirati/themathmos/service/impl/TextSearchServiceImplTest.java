package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.service.GetPayloadService;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;

public class TextSearchServiceImplTest {
    
    TextSearchServiceImpl textSearchServiceImpl;
    private ElasticsearchTemplate template;
    Client client;
    private TextUtils textUtils;
    String url = "";
    private GetPayloadService coordinateService;
    
    private ResourceLoader resourceLoader;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	resourceLoader = new DefaultResourceLoader();
	textUtils = new TextUtils();
	template = mock(ElasticsearchTemplate.class);
	client = mock(Client.class);
	coordinateService = mock(GetPayloadService.class);
	when(template.getClient()).thenReturn(client);
	textSearchServiceImpl = new TextSearchServiceImpl(textUtils,template,url, coordinateService );
    }

    @Test
    public void testTextSearchServiceImpl() {	
	assertNotNull(textSearchServiceImpl);
	
    }

    @Test
    public void testGetPageParameters() {
	assertNull(textSearchServiceImpl.getPageParameters());
    }

    @Test
    public void testGetTotalHits() {
	assertEquals(0, textSearchServiceImpl.getTotalHits());
    }

    @Test
    public void testGetTextPositions() throws IOException {
	
	String query = "comment";	
	String queryString = "http://www.example.com/search?q=comment";
	boolean isW3c = true;
	String page = null;
	boolean isMixedSearch = false;
	long totalHits = 10;
	
	
	
	SearchHits searchHits = mock(SearchHits.class);
	when(searchHits.getTotalHits()).thenReturn(totalHits);
	
	SearchHit[] hits = new SearchHit[1];
	SearchHit hit = mock(SearchHit.class);
	hits[0] = hit;
	when(hit.getId()).thenReturn("1");
	when(searchHits.iterator()).thenReturn(Iterators.forArray(hits));
	
	when(hit.getSourceAsString()).thenReturn(null);

	SearchResponse response = mock(SearchResponse.class);
	when(response.getHits()).thenReturn(searchHits);

	ListenableActionFuture<SearchResponse> action = mock(ListenableActionFuture.class);
	when(action.actionGet()).thenReturn(response);

	
	SearchRequestBuilder builder = mock(SearchRequestBuilder.class);
	when(builder.setQuery(anyString())).thenReturn(builder);
	when(builder.setFrom(anyInt())).thenReturn(builder);
	when(builder.setSize(anyInt())).thenReturn(builder);
	when(builder.setFetchSource(anyBoolean())).thenReturn(builder);
	when(builder.execute()).thenReturn(action);
	
	when(client.prepareSearch(anyString())).thenReturn(builder);
	
	MultiTermVectorsResponse mtvResponse = mock(MultiTermVectorsResponse.class);
	ActionFuture<MultiTermVectorsResponse> mtvAction = mock(ListenableActionFuture.class);
	when(mtvAction.actionGet()).thenReturn(mtvResponse);
	when(client.multiTermVectors(anyObject())).thenReturn(mtvAction);
	
	MultiTermVectorsItemResponse mtviResponse = mock(MultiTermVectorsItemResponse.class); 
	MultiTermVectorsItemResponse[] itemResponseArray = new MultiTermVectorsItemResponse[]{mtviResponse};
	when(mtviResponse.getId()).thenReturn("1");
	TermVectorsResponse tvResponse = mock(TermVectorsResponse.class);
	when(mtviResponse.getResponse()).thenReturn(tvResponse);
	
	XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
	String termVectors = getFileContents("termvector.json");
	XContent content = XContentFactory.xContent(termVectors.getBytes());
	xContentBuilder = XContentBuilder.builder(content);
	when(tvResponse.toXContent(anyObject(),anyObject())).thenReturn(xContentBuilder);


	when(mtvResponse.getResponses()).thenReturn(itemResponseArray);
	
	//String coordinates = getFileContents("termvector.json");
	
	//when(coordinateService.getJsonPayload(anyString(), anyString())).thenReturn(coordinates);
	
	
	
	
	textSearchServiceImpl.getTextPositions(query, queryString, isW3c, page, isMixedSearch);
    }

    static String readFile(String path, Charset encoding) throws IOException {
	byte[] encoded = Files.readAllBytes(Paths.get(path));
	return new String(encoded, encoding);
    }
    
    public String getFileContents(String filename) throws IOException{
	String testmessage = "classpath:"+filename;
	Resource resource =  resourceLoader.getResource(testmessage);
	File resourcefile = resource.getFile();
	return readFile(resourcefile.getPath(), StandardCharsets.UTF_8);
    }
    
    private Map<String, String>  creatOffsetPositionMap(){
   	Map<String, String> offsetPositionMap = new HashMap<>();
   	offsetPositionMap.put("0", "1");
   	offsetPositionMap.put("10", "3");
   	offsetPositionMap.put("110","22");
   	offsetPositionMap.put("120", "26");
   	offsetPositionMap.put("14", "4");
   	offsetPositionMap.put("100", "20");
   	
   	offsetPositionMap.put("114", "23");
   	offsetPositionMap.put("124", "27");
   	
   	
   	offsetPositionMap.put("21", "5");
   	offsetPositionMap.put("130", "28");
   	
   	offsetPositionMap.put("28", "6");
   	offsetPositionMap.put("137", "29");
   	
   	return offsetPositionMap;
       }

}
