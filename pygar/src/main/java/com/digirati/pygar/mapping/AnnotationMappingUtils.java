package com.digirati.pygar.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.digirati.pygar.W3CSearchAnnotation;
import com.digirati.pygar.W3CSearchAnnotationCompletionBuilder;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;




public class AnnotationMappingUtils {
    
    private static final Logger LOG = Logger.getLogger(AnnotationMappingUtils.class);
    
    private static final String LOOK_IN_ALL_JSONLD = "all";
    private static final String LOOK_IN_ANNOTATION_JSONLD = "top";
    private static final String LOOK_IN_RESOURCE_JSONLD = "bottom";
    
    
    private static final String BODY = "body";
    private static final String TARGET = "target";
    private static final String ID = "id";
    private static final String MOTIVATION = "motivation";
    private static final String PURPOSE = "purpose";
    private static final String CREATED = "created";
    private static final String CREATOR = "creator";
    private static final String XYWH = "xywh";
    private static final String TARGETURL = "targetUri";
    private static final String TARGETSOURCEURL = "targetSourceUri";
    private static final String BODYURL = "bodyUri";
    private static final String W3CJSONLD = "w3cJsonLd";
    private static final String OAJSONLD = "oaJsonLd";
    private static final String URL = "URL";
    private static final String SOURCE = "source";
    
    private static final String AWS_BODY_W3C_FIELD = "w3c";
    private static final String AWS_BODY_OA_FIELD = "oa";
    
    private static final String[] SCHEMES = new String[]{"http", "https", "ftp", "mailto", "file", "data"};
 
    
    public BodyTargetFieldData determineJsonMappingType(String rawJson) {
	Map<String, Object> javaRootBodyMapObject = null;
	
	Map<String, List<String>> fieldData = new HashMap<>();
	Map<String, List<String>> targetFieldData = new HashMap<>();
	Map<String, List<String>> targetURIFieldData = new HashMap<>();
	List<String> bodyValueData = new ArrayList<>();
	List<String> targetValueData = new ArrayList<>();

	 try {
	    javaRootBodyMapObject = new Gson().fromJson(rawJson, Map.class);
	    if(null == rawJson ){
		return null;
	    }
	    Map w3c = (Map)javaRootBodyMapObject.get(AWS_BODY_W3C_FIELD);
	    Map oa =  (Map)javaRootBodyMapObject.get(AWS_BODY_OA_FIELD);
	    
	    if(null == w3c || null == oa){
		return null;
	    }
	
	    String id = (String)w3c.get(ID);
	    List <String>idList = new ArrayList<>();
	    idList.add(id);
	    fieldData.put(ID, idList);
	    
	    Object body = w3c.get(BODY);
	    if(body instanceof String){
		List <String>fieldList = new ArrayList<>();
		fieldList.add(body.toString());
		fieldData.put(BODY, fieldList);
		 
		extractHelper(fieldList, URL, fieldData, true); 
	    }else{
		List<String> bodyList = returnGSonValueData(bodyValueData,body);
		fieldData.put(BODY, bodyList);
		extractHelper(bodyList, URL, fieldData, true); 
	    }
	    //We could have an empty body with some bodyValue
	    if(null == body){
		Object bodyValue = w3c.get("bodyValue");
		if(bodyValue instanceof String){
		    List <String>bodyValueFieldList = new ArrayList<>();
		    bodyValueFieldList.add(bodyValue.toString());
		    fieldData.put(BODY, bodyValueFieldList);
		    extractHelper(bodyValueFieldList, URL, fieldData, true);
		}
	    }
	   
	    // date searches
	    getField(w3c,fieldData, body, CREATED, LOOK_IN_ALL_JSONLD);
	    getField(w3c,fieldData, body, "generated", LOOK_IN_ANNOTATION_JSONLD);
	    getField(w3c,fieldData, body, "modified", LOOK_IN_ANNOTATION_JSONLD);
	    
	    // user search
	    getField(w3c,fieldData, body, CREATOR,  LOOK_IN_ANNOTATION_JSONLD);
	    getField(w3c,fieldData, body, "generator", LOOK_IN_ALL_JSONLD);
	    
	    // motivation searches probably want to merge these two fields.
	    getField(w3c,fieldData, body, MOTIVATION, LOOK_IN_ANNOTATION_JSONLD);
	    getField(w3c,fieldData, body, PURPOSE, LOOK_IN_RESOURCE_JSONLD);
	    
	    //This will only pull back types from the body resource
	    getField(w3c,fieldData, body, "type", LOOK_IN_RESOURCE_JSONLD);
	    
	    //This will only pull back values from the body resource
	    getField(w3c,fieldData, body, "value", LOOK_IN_RESOURCE_JSONLD);
	    
	    getField(w3c,fieldData, body, "canonical", LOOK_IN_ANNOTATION_JSONLD);
	    
	   
	    List <String>w3cJsonLdList = new ArrayList<>();
	    w3cJsonLdList.add(JsonUtils.toString(w3c));
	    fieldData.put(W3CJSONLD, w3cJsonLdList);

	    List <String>oaJsonLdList = new ArrayList<>();
	    oaJsonLdList.add(JsonUtils.toString(oa));
	    fieldData.put(OAJSONLD, oaJsonLdList);


	    Object target = w3c.get(TARGET);
	    if(target instanceof String){
		List <String>targetFieldList = new ArrayList<>();

		targetFieldList.add(target.toString());
		targetFieldData.put(TARGET, targetFieldList);
		
		extractHelper(target.toString(), XYWH, targetFieldData, false); 		
		extractHelper(target.toString(), URL, targetFieldData, false); 
		
		List <String>targetSourceFieldList = new ArrayList<>();
		targetSourceFieldList.add(target.toString());
		targetFieldData.put(TARGETSOURCEURL, targetSourceFieldList);

	    } else{
		List<String> targetList = returnGSonValueData(targetValueData,target);
		
		extractHelper(targetList, XYWH, targetFieldData, false); 
		extractHelper(targetList, URL, targetFieldData, false); 

		targetFieldData.put(TARGET, targetList);
	    }
	        
	    getSourceField(w3c,targetURIFieldData, target, SOURCE);	   
	    if(!targetURIFieldData.isEmpty() && !targetFieldData.containsKey(TARGETSOURCEURL)){
		targetFieldData.put(TARGETSOURCEURL, targetURIFieldData.get(SOURCE));
	    }
	    	    
	    getSourceField(w3c,targetURIFieldData, target, ID);
	    if(!targetURIFieldData.isEmpty() && !targetFieldData.containsKey(TARGETSOURCEURL)){
		targetFieldData.put(TARGETSOURCEURL, targetURIFieldData.get(ID));
	    }
	    
	    if(!targetFieldData.containsKey(TARGETSOURCEURL) && targetFieldData.containsKey(TARGETURL)){
		targetFieldData.put(TARGETSOURCEURL, targetFieldData.get(TARGETURL));
	    }
	    
	    getField(w3c,targetFieldData, target, "source", LOOK_IN_RESOURCE_JSONLD);
	    
	    getField(w3c,targetFieldData, target, "type", LOOK_IN_RESOURCE_JSONLD);
	    
	   if(LOG.isDebugEnabled()){
		createJsonforMapping(fieldData, BODY);
		createJsonforMapping(targetFieldData, TARGET);
		createJsonforMapping(targetURIFieldData, TARGETSOURCEURL);
	    }
  
	    
	} catch (JsonGenerationException e) {
	   LOG.error("JsonGenerationException  in determineJsonMappingType: " , e);
	    return null;
	} catch (IOException e) {
	    LOG.error("IOException in determineJsonMappingType: " , e);
	    return null;
	}
	 
	 BodyTargetFieldData data  = new BodyTargetFieldData();
	 data.setFieldData(fieldData);
	 data.setTargetFieldData(targetFieldData);
	 return data;
	 
    }
    
    public String createJsonforMapping(Map<String, List<String>> fieldData, String type) throws JsonProcessingException{
	 String mapAsJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(fieldData);
	 LOG.info(type+ " mapAsJson:" +mapAsJson);
	 return mapAsJson;
   }
    
    
    /**
     * Method to look up fields of interest within a json-ld annotation.
     * @param javaRootMapObject
     * @param fieldData
     * @param body <code>Object</code> the json object we want to find the value within, e.g. body or target
     * @param field <code>String</code> the field we want to get a value for
     * @param location <code>String</code> indicating where in the json-ld to find the field e.g. motivation is a top annotation level field, but purpose can only be found in a resource, 
     * and type can be found in both, but we only want the resource level ones, e.g. not type Annotation.
     */
    public void getField(Map<String, Object> javaRootMapObject ,Map<String, List<String>> fieldData, Object body, String field, String location){
	
	Object fieldOnject = javaRootMapObject.get(field);
	if(null != fieldOnject && (LOOK_IN_ANNOTATION_JSONLD.equals(location) || LOOK_IN_ALL_JSONLD.equals(location))){
	    List <String>fieldList = new ArrayList<String>();
	    
	    if(fieldOnject instanceof String){
		fieldList.add(fieldOnject.toString());
	    }else if(fieldOnject instanceof LinkedTreeMap){
        	LinkedTreeMap map = (LinkedTreeMap)fieldOnject;
        	fieldList.addAll(map.values());
	    }else{
		fieldList.add(fieldOnject.toString());
	    }
	    
	    fieldData.put(field, fieldList);
	}
	if(LOOK_IN_RESOURCE_JSONLD.equals(location) || LOOK_IN_ALL_JSONLD.equals(location)){
	    returnGSonData(field,fieldData,body, false);
	}	
    }
    
    /**
     * Method to look up fields of interest within a json-ld annotation.
     * @param javaRootMapObject
     * @param fieldData
     * @param body <code>Object</code> the json object we want to find the value within, e.g. body or target
     * @param field <code>String</code> the field we want to get a value for
     * @param location <code>String</code> indicating where in the json-ld to find the field e.g. motivation is a top annotation level field, but purpose can only be found in a resource, 
     * and type can be found in both, but we only want the resource level ones, e.g. not type Annotation.
     */
    public void getSourceField(Map<String, Object> javaRootMapObject ,Map<String, List<String>> fieldData, Object body, String field){	
	
	returnGSonData(field,fieldData,body, false);
		
    }
  
    /**
     * Recursive method to get back the values for specific fields in specific objects such as field 'purpose' in the body
     * @param fieldName <code>String</code> the fieldname we are looking for e.g. 'purpose' 
     * @param fieldData <code>Map</code> with key = fieldname and value = list of values e.g. key=purpose, value={[tagging, describing]}
     * @param field <code>Object</code> what json we wish to look at
     * @param isId <code>boolean</code> true when we match the fieldname in a LinkedTreeMap (JsonObject), or if we already know we are looking at a String. 
     * @return <code>Map</code> fieldData
     */
    private Map<String, List<String>>returnGSonData(String fieldName, Map<String, List<String>> fieldData, Object field, boolean isId){
	
	if (isId && (field instanceof String || field instanceof LinkedTreeMap || field instanceof Number)){
	    List idList;
	    if(fieldData.containsKey(fieldName)){
		idList =fieldData.get(fieldName);
	    }else{
		idList = new ArrayList<String>();
	    }
	    if(field instanceof String || field instanceof Number){
    		idList.add(field.toString());
	    }else{
        	LinkedTreeMap map = (LinkedTreeMap)field;
        	idList.addAll(map.values());
	    }
            fieldData.put(fieldName,idList);
      
        }
	if (field instanceof ArrayList){
	    for (Object object:(ArrayList)field){
		   returnGSonData(fieldName,fieldData, object, false);
	    }
	}
	
	if (field instanceof LinkedTreeMap){
	    LinkedTreeMap map = (LinkedTreeMap)field;
	    Set <String>keySet = map.keySet();
	    for(String key:keySet){
		if (fieldName.equals(key)){
		    returnGSonData(fieldName,fieldData, map.get(key), true);
		}else{
		    returnGSonData(fieldName,fieldData, map.get(key), false); 
		}
	    }
	}
	return fieldData;
    }
    
    
    
   
    
    
    /**
     * Recursive method to get all of the values back from a json object, so passing in the body will get back all the values for the body.
     * @param fieldData <code>List</code> of Strings to store field values 
     * @param field <code>Object</code> the json object whose fields you wish to investigate.
     * @return <code>List</code> fieldData 
     */
    private static List<String>returnGSonValueData(List<String> fieldData, Object field){
	
	if (field instanceof String ){
	   fieldData.add(field.toString());
	     
        }else if (field instanceof ArrayList){
	    for (Object object:(ArrayList)field){
		returnGSonValueData(fieldData, object);
	    }
	}else if (field instanceof LinkedTreeMap){
	    LinkedTreeMap map = (LinkedTreeMap)field;
	    Set <String>keySet = map.keySet();
	    for(String key:keySet){		
		returnGSonValueData(fieldData, map.get(key)); 
	    }
	}
	//TODO investigate why e.g. 27 is coming out as 27.0
	else if(field instanceof Number){
	    fieldData.add(field.toString());
	}

	
	return fieldData;
    }
    
    public W3CSearchAnnotation addAnnotations(Map<String, List<String>> fieldList, Map<String, List<String>> targetFieldList){
	
   	List<String> idList = fieldList.get(ID); 
   	
   	List <String>targetSourceUrlList = targetFieldList.get(TARGETSOURCEURL);
   	Object payloadObject = null;
   	if(null != targetSourceUrlList){
   	    Map <String, List<String>>payloadMap = new HashMap<>();
   	    payloadMap.put("uri", targetSourceUrlList); 
   	    payloadObject = (Object)payloadMap;
   	}
   	
   	W3CSearchAnnotationCompletionBuilder builder = new W3CSearchAnnotationCompletionBuilder(idList.get(0));
   	W3CSearchAnnotation anno = builder.build();
   	
   	anno.setId(idList.get(0));
   	
   	List <String>motivationList = fieldList.get(MOTIVATION);
   	List <String>purposeList = fieldList.get(PURPOSE);
   	
   	List <String>createdList = fieldList.get(CREATED);
   	
   	if(null !=  createdList){  
   	   anno.setCreated(createdList);
   	}
   	
   	List <String>combinedMotivationPurposeList = new ArrayList<>();
   	List <String> combinedbodyTargetList = new ArrayList<>();
   	
   	if(null !=  motivationList){
   	    motivationList = cleanMotivations(motivationList);
   	    combinedMotivationPurposeList.addAll(motivationList);
   	}
   	if(null !=  purposeList){
   	    combinedMotivationPurposeList.addAll(purposeList);
   	}
   	if(!combinedMotivationPurposeList.isEmpty()){
   	    anno.setMotivations(combinedMotivationPurposeList);
   	}
   	List <String>bodyList = fieldList.get(BODY);
   	if(null !=  bodyList){
   	    combinedbodyTargetList.addAll(bodyList);
   	    anno.setBody(bodyList);
   	}
   	
   	List <String>targetList = targetFieldList.get(TARGET);
   	if(null !=  targetList){
   	    combinedbodyTargetList.addAll(targetList);
   	    anno.setTarget(targetList);
   	}
   	
   	List <String>bodyUrlList = fieldList.get(BODYURL);
   	if(null !=  bodyUrlList){
   	    anno.setBodyURI(bodyUrlList);
   	}
   	
   	List <String>targetUrlList = targetFieldList.get(TARGETURL);
   	if(null !=  targetUrlList){
   	    anno.setTargetURI(targetUrlList);
   	}
   	
   	if(null !=  targetSourceUrlList){
   	    anno.setURI(targetSourceUrlList);
   	}
   	String[] bodyTargetArray = null;
   	if(!combinedbodyTargetList.isEmpty() || null != payloadObject){
   	    bodyTargetArray = combinedbodyTargetList.toArray(new String[0]);
   	    
   	    anno = builder.suggest(bodyTargetArray, null,payloadObject).build();
   	} 	
   	
   	List <String>creatorList = fieldList.get(CREATOR);
   	if(null !=  creatorList){
   	    anno.setCreators(creatorList);
   	}
   	
   	List <String>xywhList = targetFieldList.get(XYWH);
   	if(null !=  xywhList){
   	    anno.setXywh(xywhList);
   	}
   	
   	List<String> w3cList = fieldList.get(W3CJSONLD); 	
   	anno.setW3cJsonLd(w3cList.get(0));
   	
   	List<String> oaList = fieldList.get(OAJSONLD); 	
   	anno.setOaJsonLd(oaList.get(0));
   	
   	return anno;
       }
       
    
    public List<String> cleanMotivations(List<String> motivations) {

	List<String> cleansedMotivations = new ArrayList<>();
	for (String motivation : motivations) {
	    String lowerCaseMotivation = motivation.toLowerCase();
	    if (lowerCaseMotivation.startsWith("http")) {
		int indexOfHash = motivation.lastIndexOf("#");
		String cleanedMotivation = motivation.substring(indexOfHash + 1, motivation.length());
		cleansedMotivations.add(cleanedMotivation);
	    }else{
		cleansedMotivations.add(motivation);
	    }
	}
	return cleansedMotivations;
    }
    
    public String extractXYWH(String target){
	if(null != target){
	    String targetLowerCase = target.toLowerCase();
	    if(targetLowerCase.contains("xywh=")){
		
		int xywhIndex = targetLowerCase.indexOf("xywh=");
		return targetLowerCase.substring(xywhIndex+5, targetLowerCase.length());
	    }
	}
	return null;
    }
    
    public String extractURL(String target) {
	if (null != target) {
	    String targetLowerCase = target.toLowerCase();
	    if (targetLowerCase.contains(":")) {

		int colon = targetLowerCase.indexOf(":");
		String protocol = targetLowerCase.substring(0, colon);
		if (ArrayUtils.contains(SCHEMES, protocol)) {
		    return target;
		}
	    }
	}
	return null;
    }
    
    private void extractHelper(String value, String method, Map<String, List<String>> fieldData, boolean isBody) {
	String test;
	if (XYWH.equals(method)) {
	    test = extractXYWH(value.toString());
	    if (null != test) {
		List<String> xywhList = new ArrayList<>();
		xywhList.add(test);
		fieldData.put(XYWH, xywhList);
	    }
	} else {
	    test = extractURL(value.toString());
	    if (null != test) {
		List<String> urlList = new ArrayList<>();
		urlList.add(test);
		if(!isBody){
		    fieldData.put(TARGETURL, urlList);
		}else{
		    fieldData.put(BODYURL, urlList); 
		}
	    }
	}
    }
    
    
    
    
    private void extractHelper(List<String> targetList, String method, Map<String, List<String>> fieldData,
	    boolean isBody) {

	List<String> valueList = new ArrayList<>();
	if (XYWH.equals(method)) {
	    for (String targetField : targetList) {
		String xywh = extractXYWH(targetField);
		if (null != xywh) {
		    valueList.add(xywh);
		}
	    }
	    if (!valueList.isEmpty()) {
		fieldData.put(XYWH, valueList);
	    }

	} else {
	    for (String targetField : targetList) {
		String url = extractURL(targetField);
		if (null != url) {
		    valueList.add(url);
		}
	    }
	    if (!valueList.isEmpty()) {
		if(!isBody){
		    fieldData.put(TARGETURL, valueList);
		}else{
		    fieldData.put(BODYURL, valueList); 
		}
	    }
	}
    }

}
