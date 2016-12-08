package com.digirati.themathmos.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.model.Positions;
import com.digirati.themathmos.model.TermOffsetStart;
import com.digirati.themathmos.model.TermOffsetsWithPosition;
import com.digirati.themathmos.model.TermWithTermOffsets;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;



@Service(TextUtils.SERVICE_NAME)
public class TextUtils extends CommonUtils {
    
    private final static Logger LOG = Logger.getLogger(TextUtils.class);
   
    public static final String SERVICE_NAME = "TextUtils";  
    
    private static final String IMAGESLIST = "images";
    
    private static final int BEFORE_AFTER_WORDS = 10;
    
    private Map <String, List<Positions>> positionsMap;
    
    public Map <String, List<Positions>> getPositionsMap() {
	return positionsMap;
    }
    
    public void setPositionsMap(Map <String, List<Positions>> positionsMap) {
	this.positionsMap = positionsMap;
    }
    
    public Map<String,Object> createOffsetPayload(String query, Map <String, List<TermWithTermOffsets>> termWithOffsetsMap, String width, String height, 
	    //Map<String, Map<String, TermOffsetStart>> termPositionsMap,
	    Map<String,Map <String,String>> offsetPositionMap){
	    
	if(null == termWithOffsetsMap || termWithOffsetsMap.isEmpty()){
	    return null;
	}
	
	 Map<String, Object> root = null; 
	 root = buildImageListHead();
	 
	 List imageList = createImages(termWithOffsetsMap,width, height, query, offsetPositionMap);
		
	 List images = (List)root.get(IMAGESLIST);
	 
	 images.addAll(imageList);
	 

	 return root;
	
    }

    public Map<String, Object> createCoordinateAnnotationTest(String query, String coordinatePayload,
	    boolean isW3c, Map<String, List<Positions>> positionMap,
	    Map<String, Map<String, TermOffsetStart>> termPositionMap, String queryString, long totalHits, PageParameters pageParams) {

	if (null == coordinatePayload) {
	    return null;
	}
	Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(coordinatePayload, Map.class);

	if (null == javaRootBodyMapObject) {
	    return null;
	}

	Map<String, Object> root = null;
	// TODO logic for paging in here..
	
	if(TextSearchServiceImpl.DEFAULT_PAGING_NUMBER <= totalHits){
	    root = this.buildAnnotationPageHead(query, isW3c, pageParams);
	}else{
	    root = this.buildAnnotationListHead(query, isW3c);
	}
	

	this.setHits(root, isW3c);
	List resources = this.getResources(root, isW3c);

	List images = (List) javaRootBodyMapObject.get(IMAGESLIST);

	for (Object object : (List) images) {
	    Map<String, Object> image = (Map) object;
	    String id = (String) image.get("image_uri");

	    Map <String, String>annoURLMap = new HashMap<>();
	    //LOG.info(id);
	    List<Positions> positionList = positionMap.get(id);
	    Map<String, TermOffsetStart> sourcePositionMap = termPositionMap.get(id);
	    Object xywhObject = image.get("xywh");
	    if (xywhObject instanceof String) {
		LOG.info("String instance " + xywhObject.toString());
		LOG.info("Position " + positionList.get(0));
		String annoUrl = createMadeUpResource(queryString,xywhObject.toString());
		annoURLMap.put(xywhObject.toString(), annoUrl);
		resources.add(createResource(id, query, isW3c, xywhObject.toString(), annoUrl));
	    }
	    if (xywhObject instanceof ArrayList) {
		List xywhList = (ArrayList) image.get("xywh");
		for (Object xywhObjectFromList : xywhList) {
		    String annoUrl = createMadeUpResource(queryString,xywhObjectFromList.toString());
		    annoURLMap.put(xywhObjectFromList.toString(), annoUrl);
		    resources.add(createResource(id, query, isW3c, xywhObjectFromList.toString(),annoUrl));
		}
	    }

	    List hitList = this.getHits(root, isW3c);

	    if (positionList.size() == 1) {
		Map<String, Object> hitMap = new HashMap<>();
		List annotationsList = new ArrayList();
		
		String annoUrl = annoURLMap.get(xywhObject.toString());
		annotationsList.add(annoUrl);
		int start = positionList.get(0).getStartPosition();
		int end = positionList.get(0).getEndPosition();
		
		String[] beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);

		setHits(isW3c, hitMap, annotationsList, query, beforeAfter);

		hitList.add(hitMap);
		LOG.info(hitMap.toString());

	    } else {
		if (xywhObject instanceof ArrayList) {
		    List xywhList = (ArrayList) image.get("xywh");
		    int count = 0;
		    for (Object xywhObjectFromList : xywhList) {
			LOG.info("xywh " + xywhObjectFromList.toString());
			LOG.info("Position " + positionList.get(count));
			int start = positionList.get(count).getStartPosition();
			int end = positionList.get(count).getEndPosition();
			String[] beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);

			Map<String, Object> hitMap = new HashMap<>();
			List annotationsList = new ArrayList();
			
			annotationsList.add(annoURLMap.get(xywhObjectFromList.toString()));
			setHits(isW3c, hitMap, annotationsList, query, beforeAfter);

			LOG.info(hitMap.toString());
			hitList.add(hitMap);
			count++;
		    }
		   
		}
	    }
	}

	return root;

    }
    
    
    
    public Map<String, Object> createCoordinateAnnotation(String query, String coordinatePayload,
	    boolean isW3c, Map<String, List<Positions>> positionMap,
	    Map<String, Map<String, TermOffsetStart>> termPositionMap, String queryString, long totalHits, PageParameters pageParams) {

	if (null == coordinatePayload) {
	    return null;
	}
	Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(coordinatePayload, Map.class);

	if (null == javaRootBodyMapObject) {
	    return null;
	}
	
	String[] queryArray = query.split(" ");
	int  queryArrayLength = queryArray.length;

	Map<String, Object> root = null;
	// TODO logic for paging in here..
	
	//if(TextSearchServiceImpl.DEFAULT_PAGING_NUMBER <= totalHits){
	//    root = this.buildAnnotationPageHead(queryString, isW3c, pageParams);
	//}else{
	    root = this.buildAnnotationListHead(queryString, isW3c);
	//}
	

	
	List resources = this.getResources(root, isW3c);
	this.setHits(root, isW3c);
	List images = (List) javaRootBodyMapObject.get(IMAGESLIST);

	for (Object object : (List) images) {
	    Map<String, Object> image = (Map) object;
	    String id = (String) image.get("image_uri");

	    Map <String, String>annoURLMap = new HashMap<>();
	    List<Positions> positionList = positionMap.get(id);
	    Map<String, TermOffsetStart> sourcePositionMap = termPositionMap.get(id);
	    Object phraseObject = image.get("phrases");
	   
	    
	    List hitList = this.getHits(root, isW3c);
	    
	    if (phraseObject instanceof ArrayList) {
		List phraseObjectList = (ArrayList) image.get("phrases");
		
		int count = 0;
		for(Object innerPhraseArray:phraseObjectList){
		    if (innerPhraseArray instanceof ArrayList) {
			List innerObjectList = (ArrayList) innerPhraseArray;

			Map<String,String> xywhMap = new HashMap<>();
			String xywh =  null;
			String termCount = null;
			Map hitIdMap = new HashMap();
			double queryCount = 0;	
			 String[] beforeAfter = null;
			for(Object phraseArray:innerObjectList){
			    LinkedTreeMap map = (LinkedTreeMap)phraseArray;  
			    xywh = (String)map.get("xywh");			    
			    termCount =  removeDotZero((Double)map.get("count")) +"";
			    int start = positionList.get(count).getStartPosition();
			    int end = positionList.get(count).getEndPosition();
			    beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);
			    Map<String, Object> hitMap = new HashMap<>();
			    String queryForResource = "";
			    //LOG.info("termCount " + termCount);
			    int countInt = Integer.parseInt(termCount);
			    if(queryArrayLength > countInt){
				if(queryCount == 0){
				   for(int r= 0; r <countInt;r++){
				       queryForResource += queryArray[r] + " "; 
				   }
				   queryForResource = queryForResource.substring(0,queryForResource.length()-1);
				   xywhMap.put(xywh, queryForResource);
				}else{
				    Collection<String> stringList = xywhMap.values();
				    int countOfElements = 0;
				    
				    for(String value:stringList){					
					String[] valueArray = value.split(" ");
					countOfElements += valueArray.length;
				    }
				    for(int r= countOfElements; r<countInt+countOfElements;r++){
					 queryForResource += queryArray[r] + " "; 
				    }
				    queryForResource = queryForResource.substring(0,queryForResource.length()-1);
				    xywhMap.put(xywh, queryForResource);
				}
				queryCount++;
			    }else{
				xywhMap.put(xywh, query);
			    } 
			    String annoUrl = createMadeUpResource(queryString,xywh);
			    annoURLMap.put(xywh, annoUrl);
			}
			LOG.info("xywhMap " + xywhMap);
			Map<String, Object> hitMap = new LinkedHashMap<>();
			if(xywhMap.size() == 1){
			    List annotationsList = new ArrayList();
			    annotationsList.add(annoURLMap.get(xywh.toString()));
			    
			    setHits(isW3c, hitMap, annotationsList, query, beforeAfter);
			    resources.add(createResource(id, query, isW3c, xywh,annoURLMap.get(xywh))); 
			}else{
			    List <String>list = new ArrayList<>(xywhMap.keySet());
			    Collections.sort(list, Collections.reverseOrder());
			    Set <String>resultSet = new LinkedHashSet<>(list);
			    List annotationsList = new ArrayList();
			    for(String xywhKey:resultSet){
				String partQuery = xywhMap.get(xywhKey);
				annotationsList.add(annoURLMap.get(xywhKey));
				resources.add(createResource(id, partQuery, isW3c, xywhKey,annoURLMap.get(xywhKey))); 
			    }
			    setHits(isW3c, hitMap, annotationsList, query, beforeAfter);
			    LOG.info("Hit:" +hitMap.toString());
			}
			hitList.add(hitMap);
		    }
		    count++;
		}
		/*
		int count2 = 0;
		for(Object innerPhraseArray:phraseObjectList){
		    
		    if (innerPhraseArray instanceof ArrayList) {
			List innerObjectList = (ArrayList) innerPhraseArray;
			LOG.info("innerObjectList " + innerObjectList.toString());
			
			for(Object phraseArray:innerObjectList){
			    LinkedTreeMap map = (LinkedTreeMap)phraseArray;  
			    String xywh =  (String)map.get("xywh");
			    
			    String termCount =  removeDotZero((Double)map.get("count")) +"";
			    LOG.info("xywh " + xywh);
			    LOG.info("termCount " + termCount);
			    LOG.info("Position " + positionList.get(count2));
			   
			    int start = positionList.get(count2).getStartPosition();
			    int end = positionList.get(count2).getEndPosition();
			    String[] beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);
			    
			    Map<String, Object> hitMap = new HashMap<>();
			    List annotationsList = new ArrayList();
				
			    annotationsList.add(annoURLMap.get(xywh.toString()));
			    setHits(isW3c, hitMap, annotationsList, query, beforeAfter);

			    LOG.info("Hit:" +hitMap.toString());
			    hitList.add(hitMap);
			    
			}			
		    }
		    count2++;
		}
		*/
		
		/*for (Object xywhObjectFromList : xywhList) {
		    String annoUrl = "madeUpResource"+xywhObjectFromList.toString();
		    annoURLMap.put(xywhObjectFromList.toString(), annoUrl);
		    resources.add(createResource(id, query, isW3c, xywhObjectFromList.toString(),annoUrl));
		}*/
	    }

	    /*
	   

	    if (positionList.size() == 1) {
		Map<String, Object> hitMap = new HashMap<>();
		List annotationsList = new ArrayList();
		String annoUrl = "madeUpResource"+xywhObject.toString();
		annotationsList.add(annoUrl);
		int start = positionList.get(0).getStartPosition();
		int end = positionList.get(0).getEndPosition();
		
		String[] beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);

		setHits(isW3c, hitMap, annotationsList, query, beforeAfter);

		hitList.add(hitMap);
		LOG.info(hitMap.toString());

	    } else {
		if (xywhObject instanceof ArrayList) {
		    List xywhList = (ArrayList) image.get("xywh");
		    int count = 0;
		    for (Object xywhObjectFromList : xywhList) {
			LOG.info("xywh " + xywhObjectFromList.toString());
			LOG.info("Position " + positionList.get(count));
			int start = positionList.get(count).getStartPosition();
			int end = positionList.get(count).getEndPosition();
			String[] beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);

			Map<String, Object> hitMap = new HashMap<>();
			List annotationsList = new ArrayList();
			
			annotationsList.add(annoURLMap.get(xywhObjectFromList.toString()));
			setHits(isW3c, hitMap, annotationsList, query, beforeAfter);

			LOG.info(hitMap.toString());
			hitList.add(hitMap);
			count++;
		    }
		   
		}
	    }*/
	}

	return root;

    }
    
    
  
    
    
    @SuppressWarnings("unchecked") 
    protected Map<String, Object> buildImageListHead() {
	Map<String, Object> root = new HashMap<>();
	
	List images = new ArrayList();
	root.put("images", images);

	return root;
    }
    
   
  
    public List createImages(Map <String,List<TermWithTermOffsets>> termWithOffsetsMap, String width, String height, String query,
	    //Map<String, Map<String, TermOffsetStart>> termPositionsMap, 
	    Map<String,Map <String,String>> offsetPositionMap){
	
	
	List realRoot = new ArrayList();
	
	Map <String, List<Positions>> positionsMap = new HashMap<>();
	
	Set <String>keySet = termWithOffsetsMap.keySet();
	for(String imageId:keySet){
	    Map<String, Object> root = new HashMap<>();
	    List<TermWithTermOffsets> termWithOffsetsList = termWithOffsetsMap.get(imageId);
	    
	    List positions = new ArrayList();
	    
	    //Map<String, TermOffsetStart> positionMap =  termPositionsMap.get(imageId);
	    Map <String,String> startMap = offsetPositionMap.get(imageId);
	    
	    List <Positions>positionsList = new ArrayList<>();
	    
	    
	    if(termWithOffsetsList.size() == 1){
		 List <TermOffsetsWithPosition>offsets = termWithOffsetsList.get(0).getOffsets();
		 for(TermOffsetsWithPosition offset:offsets){
		     positions.add(offset.getStart());
		    
		     LOG.info(offset.getPosition());
		     Positions positionObject = new Positions(offset.getPosition(),offset.getPosition());
		     positionsList.add(positionObject);
		     //TermOffsetStart termOffsetStart =  positionMap.get(position);
		     //LOG.info(termOffsetStart.toString());    
		 }
		 LOG.info("position "+positions);
	    }else{
		positions = sortPositionsForMultiwordPhrase(termWithOffsetsList, 
			//positionMap, 
			startMap, positionsList);
		
	    }
	    positionsMap.put(imageId, positionsList);
	    
 
	    root.put("imageURI", imageId);
	    root.put("positions", positions);
	    root.put("width", width);
	    root.put("height", height); 
	    
	    realRoot.add(root);
	}
	
	setPositionsMap(positionsMap);
	
	return realRoot;
    }
    
    
    
    
    
    
    public List<String>  workThoughOffsets(List<TermWithTermOffsets> termWithOffsetsList) {
	int size = termWithOffsetsList.size();

	List<String> intList = new ArrayList<>();

	for (int y = size - 1; y > 0; y--) {
	    TermWithTermOffsets termMatching = termWithOffsetsList.get(y);
	    TermWithTermOffsets termMatchingPrevious = termWithOffsetsList.get(y - 1);
	    //LOG.info("getting " + termMatching.getTerm() + " and " + termMatchingPrevious.getTerm());
	    //LOG.info(y);

	    //LOG.info(y - 1);

	    for (TermOffsetsWithPosition termOffset : termMatching.getOffsets()) {
		int start = termOffset.getStart();

		for (TermOffsetsWithPosition previousTermOffset : termMatchingPrevious.getOffsets()) {
		    int end = previousTermOffset.getEnd() + 1;
		    if (start == end) {
			//LOG.info(previousTermOffset.getStart() + "|" + end);
			intList.add(previousTermOffset.getStart() + "|" + end);
			
		    }
		}
	    }
	}

	
	//LOG.info(intList);
	
	
	if (size > 2) {

	    List<String> mergedIntList = new ArrayList<>();

	    for (String listItem : intList) {
		String[] testFirst = listItem.split("[|]");
		//LOG.info("first "+ testFirst[0]);
		//LOG.info("second "+testFirst[1]);
		
		for (String innerListItem : intList) {
		  
		    String[] testsecond = innerListItem.split("[|]");
		    if (testFirst[0].equals(testsecond[1])) {
			//LOG.info(testsecond[1]);
			//LOG.info("innerListItem "+innerListItem);
			boolean isFound = false;
			int count = 0;
			for(String mergedInt:mergedIntList){
			    
			    if(mergedInt.startsWith(testsecond[1]+"|")){
				isFound = true;
				mergedInt = testsecond[0]+"|" + mergedInt;
				mergedIntList.set(count, mergedInt);
				break;
			    }
			    count++;
			}
			if(!isFound){
    		    		mergedIntList.add(innerListItem + "|" + testFirst[1]);
			}
    		    			   			
    		    	//LOG.info(mergedIntList);
		    }
		}
	    }	    
	    intList = mergedIntList;	    
	}
		
	//LOG.info("intList is " + intList);
	
	
	return intList;

    }
    
    public List sortPositionsForMultiwordPhrase( List<TermWithTermOffsets> termWithOffsetsList,
	    //Map<String, TermOffsetStart> positionMap, 
	    Map <String,String> offsetPositionMap ,
	    List <Positions>positionsList) {

	 List<String>  stringSets = workThoughOffsets(termWithOffsetsList);
	 List <String>templist = null;
	 List position = new ArrayList();
	 for(String item:stringSets){
	     templist = new ArrayList();
	     String[] stringArray = item.split("[|]");
	     for(String numberInArray:stringArray){
		 templist.add(numberInArray); 
		 //LOG.info("numberInArray " + numberInArray);
		 //LOG.info("position " +offsetPositionMap.get(numberInArray));
		 

		 //TermOffsetStart termOffsetStart = positionMap.get(numberInArray);
		 //LOG.info(termOffsetStart.toString());
	     }
	     
	     int start = Integer.parseInt(offsetPositionMap.get(templist.get(0)));
	     int end =  Integer.parseInt(offsetPositionMap.get(templist.get(templist.size() - 1))); 
		
	     Positions positions = new Positions(start, end);
	     positionsList.add(positions);
	     
	     //LOG.info("positions are "+ start + ":" + end);
	     position.add(templist);
	     
	     
	     
	 }
	 LOG.info("position "+position);
	 return position;
    }
    
    
    public Map<String, Object> createResource(String imageId, String query, boolean isW3c, String xywh, String annoUrl){
	 
	
	Map<String, Object> resource = new LinkedHashMap<>();
	resource.put(ROOT_ID, annoUrl);
	
	Map<String, Object> body = new LinkedHashMap<>();
	if(isW3c){
	   body.put(ROOT_TYPE, "http://www.w3.org/2011/content#ContentAsText");
	   body.put("http://www.w3.org/2011/content#chars", query);
	   resource.put(ROOT_TYPE, "Annotation");
	   resource.put("motivation", "http://iiif.io/api/presentation/2#painting");
	   resource.put("body", body);
	   resource.put("target", imageId+"#xywh="+xywh);
   	}else{
   	  body.put(ROOT_TYPE, "cnt:ContentAsText");
   	  body.put("chars", query);
   	  resource.put(ROOT_TYPE, "oa:Annotation");
   	  resource.put("motivation","sc:painting");
   	  resource.put("resource", body);
   	  resource.put("on", imageId+"#xywh="+xywh);
   	}
	LOG.info(resource);
	return resource;
    }
    
    public void setHits(boolean isW3c, Map <String, Object>hitMap, List annotationsList, String query, String[] beforeAfter){
	
        if(isW3c){
         	hitMap.put("type", "http://iiif.io/api/search/1#Hit");
         	hitMap.put("http://iiif.io/api/search/1#refines", annotationsList);
         	hitMap.put("http://iiif.io/api/search/1#match", query);
         	hitMap.put("http://iiif.io/api/search/1#before", beforeAfter[0]);
         	hitMap.put("http://iiif.io/api/search/1#after", beforeAfter[1]);
        }else{
         	hitMap.put("@type", "search:Hit");
         	hitMap.put("annotations", annotationsList);
         	hitMap.put("match", query);
         	hitMap.put("before", beforeAfter[0]);
         	hitMap.put("after", beforeAfter[1]);
        }
 }
    
    private int removeDotZero(Double input) {
	return input.intValue();

    }
   
    public String createMadeUpResource (String queryString,String xywh) {
   	
	String query = queryString.substring(0, queryString.indexOf("?"));
	
	String searchResultRandom = RandomStringUtils.randomAlphabetic(8);
	
	query = query + "/searchResult"+searchResultRandom+xywh;
	return query;
    }
}
