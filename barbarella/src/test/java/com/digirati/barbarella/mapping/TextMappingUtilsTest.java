package com.digirati.barbarella.mapping;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


import com.digirati.barbarella.TextAnnotation;
import com.digirati.barbarella.mapping.TextFieldData;
import com.digirati.barbarella.mapping.TextMappingUtils;

public class TextMappingUtilsTest {
    

    private final static Logger LOG = Logger.getLogger(TextMappingUtilsTest.class); 
    
 
    private ResourceLoader resourceLoader;
    
    TextMappingUtils mappingUtils = new TextMappingUtils();
    
    String testmessage1 = "classpath:text/test-message-1.json";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	resourceLoader = new DefaultResourceLoader();
    }

 
    
    @Test
    public void testDetermineJsonMappingType() throws IOException {
	
	
	LOG.info("test-message-1.json");
	LOG.info("-------------------");
	String content = getFileContents("test-message-1.json");
	LOG.info(content);
	
	TextFieldData data = mappingUtils.determineJsonMappingType(content);
	Map<String, String> bodyData = data.getFieldData();
	
	assertEquals(bodyData.get("id"),"https://dlcs.io/iiif-img/50/1/000214ef-74f3-4ec2-9a5f-3b79f50fc500");
	
	
	content = getFileContents("test-message-2.json");
	LOG.info("test-message-2.json");
	LOG.info("-------------------");
	LOG.info(content);
	data = mappingUtils.determineJsonMappingType(content);
	
	content = getFileContents("test-message-3.json");
	LOG.info("test-message-3.json");
	LOG.info("-------------------");
	LOG.info(content);
	data = mappingUtils.determineJsonMappingType(content);
	
	
	
	data = mappingUtils.determineJsonMappingType(null);
	assertNull(data);
	
	
	content = getFileContents("test-message-null.json");
	LOG.info("test-message-null.json");
	LOG.info("-------------------");
	LOG.info(content);
	data = mappingUtils.determineJsonMappingType(content);
	assertNull(data);
	
    }
    
    @Test
    public void testAddAnnotations() throws IOException {
	LOG.info("test-message-1.json");
	LOG.info("-------------------");
	String content = getFileContents("test-message-1.json");
	LOG.info(content);
	
	TextFieldData data = mappingUtils.determineJsonMappingType(content);
	Map<String, String> bodyData = data.getFieldData();
	
	
	
	TextAnnotation  annotation =  mappingUtils.addAnnotations(bodyData);
	assertEquals(annotation.getId(),"https://dlcs.io/iiif-img/50/1/000214ef-74f3-4ec2-9a5f-3b79f50fc500");
	
	
	
	
	
	LOG.info("test-message-2.json");
	LOG.info("-------------------");
	content = getFileContents("test-message-2.json");
	LOG.info(content);
	
	data = mappingUtils.determineJsonMappingType(content);
	bodyData = data.getFieldData();

	
	
	annotation =  mappingUtils.addAnnotations(bodyData);
	assertEquals(annotation.getId(),"https://dlcs.io/iiif-img/50/1/987f4c4f-b586-41f7-8b77-1a050b6e4590");
	
	
	
	LOG.info("test-message-3.json");
	LOG.info("-------------------");
	content = getFileContents("test-message-3.json");
	LOG.info(content);
	
	data = mappingUtils.determineJsonMappingType(content);
	bodyData = data.getFieldData();

	
	
	annotation =  mappingUtils.addAnnotations(bodyData);
	assertEquals(annotation.getId(),"https://dlcs.io/iiif-img/50/1/2d307ada-a9f7-4a23-adbc-73fed990013f");
	

	 
    }
    
    
    @Test
    public void testExceptions() throws IOException   {
	
	LOG.info("test-message-1.json");
	LOG.info("-------------------");
	String content = getFileContents("test-message-null.json");
	LOG.info(content);
	
	TextFieldData data = mappingUtils.determineJsonMappingType(content);
	
	assertNull(data);

	
	data = mappingUtils.determineJsonMappingType(null);
	
	assertNull(data);
	
    }
    
    
   
    

    static String readFile(String path, Charset encoding) throws IOException {
	byte[] encoded = Files.readAllBytes(Paths.get(path));
	return new String(encoded, encoding);
    }
    
    public String getFileContents(String filename) throws IOException{
	String testmessage = "classpath:text/"+filename;
	Resource resource =  resourceLoader.getResource(testmessage);
	File resourcefile = resource.getFile();
	return readFile(resourcefile.getPath(), StandardCharsets.UTF_8);
    }
    
    
    
    
    
  
  
}
