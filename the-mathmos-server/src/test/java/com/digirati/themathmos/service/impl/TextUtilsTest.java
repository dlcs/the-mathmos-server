package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.action.admin.indices.analyze.RestAnalyzeAction.Fields;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry.Option;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.transport.netty.ChannelBufferStreamInput;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.digirati.themathmos.model.Positions;
import com.digirati.themathmos.model.TermOffsetStart;
import com.digirati.themathmos.model.TermOffsetsWithPosition;
import com.digirati.themathmos.model.TermWithTermOffsets;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.impl.TextUtils;

public class TextUtilsTest {

    private static final Logger LOG = Logger.getLogger(TextUtilsTest.class);
    TextUtils textUtils;
    
    private ResourceLoader resourceLoader;
    
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	resourceLoader = new DefaultResourceLoader();
	textUtils = new TextUtils();
    }
     

    @Test
    public void testSortPositionsForMultiwordPhrase() {
	
	String query = "fox brown";
	List positions = new ArrayList();
	List<TermWithTermOffsets> termWithOffsetsList = new ArrayList<>();
	
	TermWithTermOffsets one =  new TermWithTermOffsets();
	TermWithTermOffsets two =  new TermWithTermOffsets();
	
	one.setTerm("fox");
	
	List <TermOffsetsWithPosition>oneOffsets = new ArrayList<>();
	
	TermOffsetsWithPosition oneStartEndA = new TermOffsetsWithPosition(); 
	oneStartEndA.setStart(0);
	oneStartEndA.setEnd(3);
	oneOffsets.add(oneStartEndA);
	
	TermOffsetsWithPosition oneStartEnd = new TermOffsetsWithPosition(); 
	
	oneStartEnd.setStart(10);
	oneStartEnd.setEnd(13);
	oneOffsets.add(oneStartEnd);
	
	TermOffsetsWithPosition oneStartEnd2 = new TermOffsetsWithPosition(); 
	oneStartEnd2.setStart(110);
	oneStartEnd2.setEnd(113);
	oneOffsets.add(oneStartEnd2);
	
	TermOffsetsWithPosition oneStartEnd3 = new TermOffsetsWithPosition(); 
	oneStartEnd3.setStart(120);
	oneStartEnd3.setEnd(123);
	oneOffsets.add(oneStartEnd3);
	
	
	one.setOffsets(oneOffsets);
	
	two.setTerm("brown");
	
	List <TermOffsetsWithPosition>twoOffsets = new ArrayList<>();
	TermOffsetsWithPosition twoStartEnd = new TermOffsetsWithPosition(); 
	twoStartEnd.setStart(14);
	twoStartEnd.setEnd(20);
	twoOffsets.add(twoStartEnd);
	
	TermOffsetsWithPosition twoStartEndA = new TermOffsetsWithPosition(); 
	twoStartEndA.setStart(100);
	twoStartEndA.setEnd(105);
	twoOffsets.add(twoStartEndA);
	
	TermOffsetsWithPosition twoStartEnd2 = new TermOffsetsWithPosition(); 
	twoStartEnd2.setStart(114);
	twoStartEnd2.setEnd(119);
	twoOffsets.add(twoStartEnd2);
	
	
	TermOffsetsWithPosition twoStartEnd3 = new TermOffsetsWithPosition(); 
	twoStartEnd3.setStart(124);
	twoStartEnd3.setEnd(129);
	twoOffsets.add(twoStartEnd3);
	
	two.setOffsets(twoOffsets);
	
	termWithOffsetsList.add(one);
	termWithOffsetsList.add(two);
	
	textUtils.workThoughOffsets(termWithOffsetsList);
	
	Map<String, String> offsetMap = creatOffsetPositionMap();
	
	List <Positions>positionsList = new ArrayList<>();
	
	positions = textUtils.sortPositionsForMultiwordPhrase(termWithOffsetsList, offsetMap, positionsList);
	assertTrue(positions.size() == 3);
	LOG.info("positions " + positions);
	
	TermWithTermOffsets three =  new TermWithTermOffsets();
	three.setTerm("laughs");
	
	
	List <TermOffsetsWithPosition>threeOffsets = new ArrayList<>();
	TermOffsetsWithPosition threeStartEnd = new TermOffsetsWithPosition(); 
	threeStartEnd.setStart(21);
	threeStartEnd.setEnd(27);
	threeOffsets.add(threeStartEnd);

	TermOffsetsWithPosition threeStartEnd3 = new TermOffsetsWithPosition(); 
	threeStartEnd3.setStart(130);
	threeStartEnd3.setEnd(136);
	threeOffsets.add(threeStartEnd3);
	
	three.setOffsets(threeOffsets);
	
	termWithOffsetsList.add(three);
	
	
	TermWithTermOffsets four =  new TermWithTermOffsets();
	four.setTerm("quickly");
	
	
	List <TermOffsetsWithPosition>fourOffsets = new ArrayList<>();
	TermOffsetsWithPosition fourStartEnd = new TermOffsetsWithPosition(); 
	fourStartEnd.setStart(28);
	fourStartEnd.setEnd(35);
	fourOffsets.add(fourStartEnd);

	TermOffsetsWithPosition fourStartEnd3 = new TermOffsetsWithPosition(); 
	fourStartEnd3.setStart(137);
	fourStartEnd3.setEnd(142);
	fourOffsets.add(fourStartEnd3);
	
	four.setOffsets(fourOffsets);
	
	termWithOffsetsList.add(four);
	
	textUtils.workThoughOffsets(termWithOffsetsList);
	
	positionsList = new ArrayList<>();
	
	positions= textUtils.sortPositionsForMultiwordPhrase(termWithOffsetsList, offsetMap, positionsList);
	assertTrue(positions.size() == 2);
	LOG.info("positions " + positions);
    }
    
    @Test
    public void testCoordinates() throws IOException{
			
	String id1 = "https://dlcs.io/iiif-img/2/1/6b33280a-d28f-4773-be0d-05bd364c745e";		
	String id2 = "https://dlcs.io/iiif-img/50/1/000214ef-74f3-4ec2-9a5f-3b79f50fc500";
	String id3 = "https://dlcs.io/iiif-img/50/1/000214ef-74f3-4ec2-9a5f-3b79f50fc505";

	String query = "test me out for a long";
	
	Map <String, List<Positions>> positionMap = new HashMap<>();
	List<Positions> positionList = new ArrayList<>();
	Positions pos1 = new Positions(10, 10);
	Positions pos2 = new Positions(12, 12);
	Positions pos3 = new Positions(14, 14);
	Positions pos4 = new Positions(16, 16);
	Positions pos5 = new Positions(18, 18);
	Positions pos6 = new Positions(20, 20);
	positionList.add(pos1);
	positionList.add(pos2);
	positionList.add(pos3);
	positionList.add(pos4);
	positionList.add(pos5);
	positionList.add(pos6);
	
	positionMap.put(id1, positionList);
	
	List<Positions> positionList2 = new ArrayList<>();
	Positions pos21 = new Positions(10, 10);
	Positions pos22 = new Positions(12, 12);
	positionList2.add(pos21);
	positionList2.add(pos22);

	positionMap.put(id2, positionList2);
	
	List<Positions> positionList3 = new ArrayList<>();
	Positions pos31 = new Positions(10, 10);
	positionList3.add(pos31);
	positionMap.put(id3, positionList3);
	
	Map<String, Map<String, TermOffsetStart>> termPositionMap = new HashMap<>();
	
	Map<String, TermOffsetStart> termOffsetMap1 = new HashMap<>();
	Map<String, TermOffsetStart> termOffsetMap2 = new HashMap<>();
	
	TermOffsetStart termOffsetStart5 =  new TermOffsetStart();
	termOffsetStart5.setTerm("the");
	TermOffsetStart termOffsetStart6 =  new TermOffsetStart();
	termOffsetStart6.setTerm("big");
	TermOffsetStart termOffsetStart7 =  new TermOffsetStart();
	termOffsetStart7.setTerm("bad");
	TermOffsetStart termOffsetStart8 =  new TermOffsetStart();
	termOffsetStart8.setTerm("baby");
	TermOffsetStart termOffsetStart9 =  new TermOffsetStart();
	termOffsetStart9.setTerm("red");
	TermOffsetStart termOffsetStart10 =  new TermOffsetStart();
	termOffsetStart10.setTerm("fox");
	TermOffsetStart termOffsetStart11 =  new TermOffsetStart();
	termOffsetStart11.setTerm("put");
	TermOffsetStart termOffsetStart12 =  new TermOffsetStart();
	termOffsetStart12.setTerm("a");
	TermOffsetStart termOffsetStart13 =  new TermOffsetStart();
	termOffsetStart13.setTerm("hen");
	TermOffsetStart termOffsetStart14 =  new TermOffsetStart();
	termOffsetStart14.setTerm("into");
	
	termPositionMap.put(id1, termOffsetMap1);
	termPositionMap.put(id2, termOffsetMap1);
	termPositionMap.put(id3, termOffsetMap1);
	
	termOffsetMap1.put("5", termOffsetStart5);
	termOffsetMap1.put("6", termOffsetStart6);
	termOffsetMap1.put("7", termOffsetStart7);
	termOffsetMap1.put("8", termOffsetStart8);
	termOffsetMap1.put("9", termOffsetStart9);
	termOffsetMap1.put("10", termOffsetStart10);
	termOffsetMap1.put("11", termOffsetStart11);
	termOffsetMap1.put("12", termOffsetStart12);
	termOffsetMap1.put("13", termOffsetStart13);
	termOffsetMap1.put("14", termOffsetStart14);
		
	String queryString = "http://www.google.com?q=test";

	String coordinates2 = getFileContents("test_coordinates_2.json");
	
	
	Map<String,Object>  map = textUtils.createCoordinateAnnotation(query, coordinates2, true, positionMap, termPositionMap, queryString, new PageParameters(), true);
	//Map<String,Object>  map =textUtils.createCoordinateAnnotation(params,coordinates2, positionMap, termPositionMap , //10,
	//	new PageParameters());
	LOG.info(map.toString());
	
    }
    
    
    @Test
    public void testsetESSource(){
	
	Map <String, Object> map = textUtils.setESSource(0, 10, "bacon", new String[]{"body", "target", "bodyURI", "targetURI" }, "phrase");
	LOG.info(map.toString());
	
	String within = "http://wellcomelibrary.org/service/collections/collections/digukmhl/";
	map = textUtils.setSource(map,within, "text_index", 1);
   	LOG.info(map.toString());
   	
	
    }
    
    @Test
    public void testGettingSourceString(){
	String test = "search {\"from\" : 0,\"size\" : 10, \"query\" : { \"bool\" : {\"must\" : [ { \"multi_match\" : { \"query\" : \"bacon\",\"fields\" : [ \"body\", \"target\", \"bodyURI\", \"targetURI\" ],\"type\" :\"phrase\"}}, {\"query_string\" : {\"query\" : \"tagging\",\"fields\" : [ \"motivations\" ] }} ]} },\"post_filter\" : {\"bool\" : { }}}";
	String jsonString = textUtils.getQueryString(test);
	LOG.info(jsonString);
	
	Map <String, Object>jsonMap = textUtils.getQueryMap(jsonString);
	
	
	LOG.info(jsonMap.toString());
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
    
   
    @Test
    public void testThis(){
	
	
	CompletionSuggestion.Entry.Option option = new CompletionSuggestion.Entry.Option(new Text("someText"), 1.3f, null);
	
        CompletionSuggestion.Entry entry = new CompletionSuggestion.Entry(new Text("bacon"), 0, 5);
        entry.addOption(option);
        CompletionSuggestion suggestion = new CompletionSuggestion("annotation_suggest", 5);
        suggestion.addTerm(entry);
        List<Suggestion<? extends Entry<? extends Option>>> suggestions = new ArrayList<>();
        suggestions.add(suggestion);
        Suggest suggest = new Suggest(suggestions); 
        
        LOG.info(suggest.toString());
        
        
        
    }
 
    
   
    

}
