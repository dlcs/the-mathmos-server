package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.model.annotation.w3c.SuggestOption;
import com.digirati.themathmos.model.annotation.w3c.W3CAnnotation;
import com.digirati.themathmos.service.impl.AnnotationUtils;
import com.google.gson.internal.LinkedTreeMap;

public class AnnotationUtilsTest {
    
    private AnnotationUtils annotationUtils;
    private static final Logger LOG = Logger.getLogger(AnnotationUtilsTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	annotationUtils = new AnnotationUtils();
	
    }

    @Test
    public void test() {	
	assertNotNull(annotationUtils);	
    }
    
    @Test
    public void testCreateAnnotationPage() {
	
	List<W3CAnnotation> annoList = new ArrayList<W3CAnnotation>();
	annoList.add(createDummyAnnotation("1"));
	
	String query = "http://www.example.com/q=test";
	boolean isW3c = true;
	
	PageParameters pageParams = new PageParameters();
	pageParams.setTotalElements("12");
	pageParams.setFirstPageNumber("1");
	pageParams.setLastPageNumber("2");
	pageParams.setNextPageNumber("2");
	pageParams.setPreviousPageNumber(null);
	pageParams.setStartIndex("0");
	
	int totalHits = 11;
	Map<String,Object> json = annotationUtils.createAnnotationPage(query, annoList, isW3c, pageParams, totalHits, false);
	
	LOG.info(json);
	
	assertTrue("http://iiif.io/api/presentation/2#AnnotationList".equals(json.get("@type")));
	assertTrue("http://www.w3.org/ns/anno.jsonld".equals(json.get("@context")));
	assertTrue("http://www.example.com/q=test".equals(json.get("@id")));
	//assertTrue("2".equals(json.get("next")));
	
	assertFalse("http://www.example.com/q=test".equals(json.get("dcterms:isPartOf")));
	
	
	//test w3c change
	isW3c = false;
	json = annotationUtils.createAnnotationPage(query, annoList, isW3c, pageParams, totalHits, false);
	assertFalse("http://www.w3.org/ns/anno.jsonld".equals(json.get("@context")));
	assertTrue("sc:AnnotationList".equals(json.get("@type")));
	//assertTrue("2".equals(json.get("next")));
	LOG.info(json);
    }
    
    @Test
    public void testCreateAutocompleteList() {
	
	String w3cContext = "http://iiif.io/api/search/1/context.json";
	
	List <SuggestOption> options = new ArrayList<SuggestOption>();
	SuggestOption suggestOption = new SuggestOption("tested");
	options.add(suggestOption);
	boolean isW3c = true;
	String queryString = "http://www.example.com/autocomplete?q=test";
	String motivation = "";
	String date = "";
	String user = "";
	Map<String,Object> json = annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date, user);
	LOG.info(" ");
	LOG.info(json);
	@SuppressWarnings("unchecked")
	List <String>ignoredList = (ArrayList<String>)json.get("ignored");
	assertNull(ignoredList);
	
	String id = (String)json.get("@id");
	assertEquals(id,"http://www.example.com/autocomplete?q=test");
	String context  = (String)json.get("@context");
	assertEquals(context,w3cContext);
	

	motivation = "commenting";
	queryString = "http://www.example.com/autocomplete?q=test&motivation=commenting";
	json = annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date, user);
	LOG.info(" ");
	LOG.info(json);
	ignoredList = (ArrayList<String>)json.get("ignored");
	assertTrue(ignoredList.contains("motivation"));
	id = (String)json.get("@id");
	assertEquals(id,"http://www.example.com/autocomplete?q=test");
	context  = (String)json.get("@context");
	assertEquals(context,w3cContext);
	
	
	SuggestOption suggestOption2 = new SuggestOption("testes");
	options.add(suggestOption2);
	date = "12/04/1970";
	queryString = "http://www.example.com/autocomplete?q=test&motivation=commenting&date=12/04/1970";
	json = annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date, user);	
	ignoredList = (ArrayList<String>)json.get("ignored");
	assertTrue(ignoredList.contains("motivation"));
	assertTrue(ignoredList.contains("date"));
	LOG.info(" ");
	LOG.info(json);
	id = (String)json.get("@id");
	assertEquals(id,"http://www.example.com/autocomplete?q=test");
	context  = (String)json.get("@context");
	assertEquals(context,w3cContext);
	
	//test oa
	isW3c = false;
	json = annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date, user);
	LOG.info(" ");
	LOG.info(json);
	id = (String)json.get("@id");
	assertEquals(id,"http://www.example.com/autocomplete?q=test");
	
	queryString = "http://www.example.com/autocomplete?motivation=commenting&date=12/04/1970&q=test";
	json = annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date, user);
	id = (String)json.get("@id");
	assertEquals(id,"http://www.example.com/autocomplete?q=test");
	context  = (String)json.get("@context");
	assertEquals(context,w3cContext);
	
	motivation = "commenting tagging";
	user = "frank";
	queryString = "http://www.example.com/autocomplete?motivation=commenting tagging&date=12/04/1970&q=test&user=frank";
	json = annotationUtils.createAutocompleteList(options, isW3c, queryString, motivation, date, user);
	LOG.info(" ");
	LOG.info(json);
	id = (String)json.get("@id");
	assertEquals(id,"http://www.example.com/autocomplete?q=test");
	context  = (String)json.get("@context");
	assertEquals(context,w3cContext);
	ignoredList = (ArrayList<String>)json.get("ignored");
	assertTrue(ignoredList.contains("motivation"));
	assertTrue(ignoredList.contains("date"));
	assertTrue(ignoredList.contains("user"));
    }
    
    @Test
    public void testConvertSpecialCharacters() {
	String input = "http://www.emaple.com/ferd/lgg/";
	String output = annotationUtils.convertSpecialCharacters(input);
	LOG.info(input);
	LOG.info(output);
	assertEquals(output, "(\"http://www.emaple.com/ferd/lgg/\")");
	
	input = "trt:ttttt";
	output = annotationUtils.convertSpecialCharacters(input);
	LOG.info(input);
	LOG.info(output);
	
	assertEquals(output, "trt\\:ttttt");
	
	input = "http://www.emaple.com/ferd/lgg/ trt:ttttt";
	output = annotationUtils.convertSpecialCharacters(input);
	LOG.info(input);
	LOG.info(output);
	assertEquals(output, "(\"http://www.emaple.com/ferd/lgg/\") trt\\:ttttt");
	
	
	input = "https://omeka.dlcs-ida.org/s/ida/page/topics//virtual:person/ros+king";
	output = annotationUtils.convertSpecialCharacters(input);
	LOG.info(input);
	LOG.info(output);
    }
    
    
    
    
    private W3CAnnotation createDummyAnnotation(String id){
	W3CAnnotation anno = new W3CAnnotation();
	anno.setId(id);
	
	Map<String, Object> root = new HashMap<String, Object>();
	List resources = new ArrayList();
	LinkedTreeMap jsonMap = (LinkedTreeMap)root.put("http://iiif.io/api/presentation/2#hasAnnotations", resources);
	anno.setJsonMap(jsonMap);
	return anno;
	
    }

}
