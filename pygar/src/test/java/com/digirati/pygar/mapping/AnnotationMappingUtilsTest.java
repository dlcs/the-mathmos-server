package com.digirati.pygar.mapping;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.digirati.pygar.W3CSearchAnnotation;
import com.digirati.pygar.mapping.AnnotationMappingUtils;
import com.digirati.pygar.mapping.BodyTargetFieldData;

public class AnnotationMappingUtilsTest {
    

      
    
 
    private ResourceLoader resourceLoader;
    
    AnnotationMappingUtils mappingUtils = new AnnotationMappingUtils();
    
    String testmessage1 = "classpath:free/test-message-1.json";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	resourceLoader = new DefaultResourceLoader();
    }

    @Test
    public void testExtractXYWH() {
	String target = "http://www.examples./com/?xywh=848,2588,3505,20";
	String output = mappingUtils.extractXYWH(target);
	assertTrue(output.equals("848,2588,3505,20"));
	target = "xywh=919,2040,92,32";
	output = mappingUtils.extractXYWH(target);
	assertTrue(output.equals("919,2040,92,32"));
	output = mappingUtils.extractXYWH(null);
	assertNull(output);
	target = "http://www.examples./com/?XYWH=848,2588,3505,20";
	output = mappingUtils.extractXYWH(target);
	assertTrue(output.equals("848,2588,3505,20"));
    }
    
    @Test
    public void testExtractURLS() {
	String target = "http://www.examples./com/?xywh=848,2588,3505,20";
	String output = mappingUtils.extractURL(target);
	assertTrue(output.equals("http://www.examples./com/?xywh=848,2588,3505,20"));
	target = "xywh=919,2040,92,32";
	output = mappingUtils.extractURL(target);
	assertNull(output);
	output = mappingUtils.extractURL(null);
	assertNull(output);
	target = "https://www.examples./com/?XYWH=848,2588,3505,20";
	output = mappingUtils.extractURL(target);
	assertTrue(output.equals("https://www.examples./com/?XYWH=848,2588,3505,20"));
	target = "HTTPS://www.examples./com/?XYWH=848,2588,3505,20";
	output = mappingUtils.extractURL(target);
	assertTrue(output.equals("HTTPS://www.examples./com/?XYWH=848,2588,3505,20"));
	target = "ftp://www.examples./com/?XYWH=848,2588,3505,20";
	output = mappingUtils.extractURL(target);
	assertTrue(output.equals("ftp://www.examples./com/?XYWH=848,2588,3505,20"));
    }
    
    @Test
    public void testCleanMotivations(){
	List<String> motivations = new ArrayList<String>();
	motivations.add("http://wwwblahblah/#cleaning");
	motivations.add("Http://wwwblahblah/#cleaning");
	motivations.add("tagging");
	
	List<String> cleanList = mappingUtils.cleanMotivations(motivations);
	assertTrue(cleanList.get(0).equals("cleaning"));
	assertTrue(cleanList.get(1).equals("cleaning"));
	assertTrue(cleanList.get(2).equals("tagging"));

    }
    
    @Test
    public void testDetermineJsonMappingType() throws IOException {
	
	
	System.out.println("test-message-1.json");
	System.out.println("-------------------");
	String content = getFileContents("test-message-1.json");
	System.out.println(content);
	
	BodyTargetFieldData data = mappingUtils.determineJsonMappingType(content);
	Map<String, List<String>> bodyData = data.getFieldData();
	Map<String, List<String>> targetData = data.getTargetFieldData();
	assertEquals(bodyData.get("id").get(0),"http://annotation-local.digtest.co.uk:80/annotation/w3c/64edd991-3f26-4be0-888c-9d6ddc6b97d3/2e8cdaec-8818-4c0a-a671-7b8937ced55e");
	assertEquals(targetData.get("target").get(0), "http://www.example.com/index.html");
	
	content = getFileContents("test-message-2.json");
	System.out.println("test-message-2.json");
	System.out.println("-------------------");
	System.out.println(content);
	data = mappingUtils.determineJsonMappingType(content);
	
	content = getFileContents("test-message-3.json");
	System.out.println("test-message-3.json");
	System.out.println("-------------------");
	System.out.println(content);
	data = mappingUtils.determineJsonMappingType(content);
	targetData = data.getTargetFieldData();
	System.out.println(targetData);
	
	assertNotNull(targetData.get("xywh").get(0));
	
	
	data = mappingUtils.determineJsonMappingType(null);
	assertNull(data);
	
	
	content = getFileContents("test-message-null.json");
	System.out.println("test-message-null.json");
	System.out.println("-------------------");
	System.out.println(content);
	data = mappingUtils.determineJsonMappingType(content);
	assertNull(data);
	
    }
    
    @Test
    public void testAddAnnotations() throws IOException {
	System.out.println("test-message-1.json");
	System.out.println("-------------------");
	String content = getFileContents("test-message-1.json");
	System.out.println(content);
	
	BodyTargetFieldData data = mappingUtils.determineJsonMappingType(content);
	Map<String, List<String>> bodyData = data.getFieldData();
	Map<String, List<String>> targetData = data.getTargetFieldData();
	
	
	W3CSearchAnnotation  annotation =  mappingUtils.addAnnotations(bodyData, targetData);
	assertEquals(annotation.getId(),"http://annotation-local.digtest.co.uk:80/annotation/w3c/64edd991-3f26-4be0-888c-9d6ddc6b97d3/2e8cdaec-8818-4c0a-a671-7b8937ced55e");
	
	assertEquals(annotation.getTarget().get(0), "http://www.example.com/index.html");
	
	
	
	System.out.println("test-message-2.json");
	System.out.println("-------------------");
	content = getFileContents("test-message-2.json");
	System.out.println(content);
	
	data = mappingUtils.determineJsonMappingType(content);
	bodyData = data.getFieldData();
	targetData = data.getTargetFieldData();
	
	
	annotation =  mappingUtils.addAnnotations(bodyData, targetData);
	assertEquals(annotation.getId(),"http://annotation-local.digtest.co.uk:80/annotation/w3c/70fbd357-874c-46c2-8533-04eb33cc6445/c5c8f275-a9c4-499e-96b0-d35a0d2e302b");
	
	assertNotEquals(annotation.getTarget().get(0), "http://www.example.com/index.html");
	
	System.out.println("test-message-3.json");
	System.out.println("-------------------");
	content = getFileContents("test-message-3.json");
	System.out.println(content);
	
	data = mappingUtils.determineJsonMappingType(content);
	bodyData = data.getFieldData();
	targetData = data.getTargetFieldData();
	
	
	annotation =  mappingUtils.addAnnotations(bodyData, targetData);
	assertEquals(annotation.getId(),"http://data.llgc.org.uk/def/cynefin/annotation/20456642545664352432368723075");
	
	assertNotEquals(annotation.getTarget().get(0), "http://www.example.com/index.html");
	
	System.out.println("test-message-4.json");
	System.out.println("-------------------");
	content = getFileContents("test-message-4.json");
	System.out.println(content);
	
	data = mappingUtils.determineJsonMappingType(content);
	bodyData = data.getFieldData();
	targetData = data.getTargetFieldData();
	
	
	annotation =  mappingUtils.addAnnotations(bodyData, targetData);
	assertEquals(annotation.getId(),"https://annotation-dev.digtest.co.uk:443/annotation/w3c/7f548f8cadfd52ca1ce258d222ec908c_a/5464471c-de1a-4e69-8df4-1f4979e5ad32");
	
	assertNotEquals(annotation.getTarget().get(0), "http://www.example.com/index.html");
	 
    }
    
   
    

    static String readFile(String path, Charset encoding) throws IOException {
	byte[] encoded = Files.readAllBytes(Paths.get(path));
	return new String(encoded, encoding);
    }
    
    public String getFileContents(String filename) throws IOException{
	String testmessage = "classpath:free/"+filename;
	Resource resource =  resourceLoader.getResource(testmessage);
	File resourcefile = resource.getFile();
	return readFile(resourcefile.getPath(), StandardCharsets.UTF_8);
    }
    
    
    
    
    
  
  
}
