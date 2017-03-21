package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest.Flag;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.service.GetPayloadService;
import com.google.common.collect.Iterators;


@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class TextSearchServiceImplTest {
    
    private static final Logger LOG = Logger.getLogger(TextSearchServiceImplTest.class);
    
    TextSearchServiceImpl textSearchServiceImpl;
    private ElasticsearchTemplate template;
    Client client;
    private TextUtils textUtils;
    String url = "";
    private GetPayloadService coordinateService;
    private CacheManager cacheManager;
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
	cacheManager = mock(CacheManager.class);
	when(template.getClient()).thenReturn(client);
	textSearchServiceImpl = new TextSearchServiceImpl(textUtils,template,url, coordinateService, cacheManager );
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
	when(hit.getId()).thenReturn("3");
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
	
	

	
	TermVectorsResponse tvResponse = new TermVectorsResponse("1","2","3");
	tvResponse.setExists(true);
	writeStandardTermVector(tvResponse);
	MultiTermVectorsItemResponse mtviResponse = new MultiTermVectorsItemResponse(tvResponse, null);
	

	
	ActionFuture<MultiTermVectorsResponse> mtvAction = mock(ListenableActionFuture.class);
	
	when(client.multiTermVectors(anyObject())).thenReturn(mtvAction);
	
	
	
	MultiTermVectorsItemResponse[] itemResponseArray = new MultiTermVectorsItemResponse[]{mtviResponse};
	
	MultiTermVectorsResponse mtvr = new MultiTermVectorsResponse(itemResponseArray);
	when(mtvAction.actionGet()).thenReturn(mtvr);
	
	String coordinates = getFileContents("coordinates.json");
	
	when(coordinateService.getJsonPayload(anyString(), anyString())).thenReturn(coordinates);
	
	
	Cache.ValueWrapper obj = mock(Cache.ValueWrapper.class);
	Cache mockCache = mock(Cache.class);
	when(cacheManager.getCache("textSearchCache")).thenReturn(mockCache);
	when(mockCache.get(anyString())).thenReturn(null);
	ServiceResponse<Map<String, Object>> serviceResponse = textSearchServiceImpl.getTextPositions(query, queryString, isW3c, page, isMixedSearch, null);
	assertEquals(serviceResponse.getStatus(), Status.OK);
	
	
	when(coordinateService.getJsonPayload(anyString(), anyString())).thenReturn(null);
	serviceResponse = textSearchServiceImpl.getTextPositions(query, queryString, isW3c, page, isMixedSearch, null);
	assertEquals(serviceResponse.getStatus(), Status.NOT_FOUND);
	
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
    
    private void writeStandardTermVector(TermVectorsResponse outResponse) throws IOException {

        Directory dir = LuceneTestCase.newDirectory();
        IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());

        conf.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, conf);
        FieldType type = new FieldType(TextField.TYPE_STORED);
        type.setStoreTermVectorOffsets(true);
        type.setStoreTermVectorPayloads(false);
        type.setStoreTermVectorPositions(true);
        type.setStoreTermVectors(true);
        type.freeze();
        Document d = new Document();
        d.add(new Field("id", "abc", StringField.TYPE_STORED));
        d.add(new Field("text", "the1 quick brown fox jumps over  the1 lazy dog comment", type));
        d.add(new Field("desc", "the1 quick brown fox jumps over  the1 lazy dog comment", type));

        writer.updateDocument(new Term("id", "abc"), d);
        writer.commit();
        writer.close();
        DirectoryReader dr = DirectoryReader.open(dir);
        IndexSearcher s = new IndexSearcher(dr);
        TopDocs search = s.search(new TermQuery(new Term("id", "abc")), 1);
        ScoreDoc[] scoreDocs = search.scoreDocs;
        int doc = scoreDocs[0].doc;
        Fields termVectors = dr.getTermVectors(doc);
        EnumSet<Flag> flags = EnumSet.of(Flag.Positions, Flag.Offsets);
        outResponse.setFields(termVectors, null, flags, termVectors);
        dr.close();
        dir.close();

    }

}
