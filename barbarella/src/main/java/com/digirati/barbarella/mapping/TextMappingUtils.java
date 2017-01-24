package com.digirati.barbarella.mapping;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.digirati.barbarella.TextAnnotation;
import com.digirati.barbarella.TextCompletionBuilder;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;





public class TextMappingUtils {
    
    private static final Logger LOG = Logger.getLogger(TextMappingUtils.class);
    
    private static final String BODY = "body";
  
    private static final String ID = "id";
    
    
    public TextFieldData determineJsonMappingType(String rawJson) {
	Map<String, Object> javaRootBodyMapObject = null;
	
	Map<String,String> fieldData = new HashMap<>();
	


	 try {
	    javaRootBodyMapObject = new Gson().fromJson(rawJson, Map.class);
	    if(null == rawJson ){
		return null;
	    }	   
	    
	    if(null == javaRootBodyMapObject){
		return null;
	    }
	
	    Set <String>keySet = javaRootBodyMapObject.keySet();
	    String id = null;
	    if(1 != keySet.size()){
		return null;
	    }else{
		Iterator <String>iter = keySet.iterator();

		id = (String)iter.next();		
	    }
	 
	    fieldData.put(ID, id);
	    
	    String body = (String)javaRootBodyMapObject.get(id);
	    if(null == body){
		return null;
	    }
	    
	    fieldData.put(BODY, body);
	    
	  if(LOG.isDebugEnabled()){
		createJsonforMapping(fieldData, BODY);
	  }
  
	    
	} catch (JsonGenerationException e) {
	   LOG.error("JsonGenerationException  in determineJsonMappingType: " , e);
	    return null;
	} catch (IOException e) {
	    LOG.error("IOException in determineJsonMappingType: " , e);
	    return null;
    	} catch (Exception e) {
	    LOG.error("Exception in determineJsonMappingType: " , e);
	    return null;
	}
	 
	 TextFieldData data  = new TextFieldData();
	 data.setFieldData(fieldData);
	 return data;
	 
    }
    
    public String createJsonforMapping(Map<String, String> fieldData, String type) throws JsonProcessingException{
	 String mapAsJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(fieldData);
	 LOG.info(type+ " mapAsJson:" +mapAsJson);
	 return mapAsJson;
   }
    

    public TextAnnotation addAnnotations(Map<String, String> fieldList){
	
   	String id = fieldList.get(ID); 
   	
   	TextCompletionBuilder builder = new TextCompletionBuilder(id);
   	TextAnnotation text = builder.build();
   	
   	text.setId(id);
   	
   	
   	String body = fieldList.get(BODY);
   	if(null !=  body){ 
   	    text.setText(body);
   	    String[] bodyArray = new String[]{body};
   	    text = builder.suggest(bodyArray).build();
   	    LOG.info(text.toString());
   	}

   	return text;
       }
       
    
   

}
