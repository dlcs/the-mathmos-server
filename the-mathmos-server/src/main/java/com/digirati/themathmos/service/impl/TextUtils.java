package com.digirati.themathmos.service.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
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
    
    
  
   
    
    private static final Logger LOG = Logger.getLogger(TextUtils.class);
   
    public static final String SERVICE_NAME = "TextUtils";  
    
    private static final String IMAGESLIST = "images";
    
    private static final String PHRASES = "phrases";
    
    //We are getting 10 words before and 10 words after our query for the Hits.
    private static final int BEFORE_AFTER_WORDS = 10;
    
    
    //Map containing keys of image ids and a List of the Positions (start end) where we have found matches to our query within them.
    private Map <String, List<Positions>> positionsMap;
    
    public Map <String, List<Positions>> getPositionsMap() {
	return positionsMap;
    }
    
    public void setPositionsMap(Map <String, List<Positions>> positionsMap) {
	this.positionsMap = positionsMap;
    }
    
    
    
 
    public Map<String, Object> createOffsetPayload(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
	    String width, String height, Map<String, Map<String, String>> offsetPositionMap) {

	if (null == termWithOffsetsMap || termWithOffsetsMap.isEmpty()) {
	    return null;
	}

	Map<String, Object> root = buildImageListHead();

	List<Map<String, Object>> imageList = createImages(termWithOffsetsMap, width, height, offsetPositionMap);

	List<Map<String, Object>> images = (List<Map<String, Object>>) root.get(IMAGESLIST);

	images.addAll(imageList);

	return root;

    }

    public Map<String, Object> createOffsetPayload(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
	    String width, String height, Map<String, Map<String, String>> offsetPositionMap, Map<String, String> canvasImageMap) {

	if (null == termWithOffsetsMap || termWithOffsetsMap.isEmpty()) {
	    return null;
	}

	Map<String, Object> root = buildImageListHead();

	List<Map<String, Object>> imageList = createImages(termWithOffsetsMap, width, height, offsetPositionMap, canvasImageMap);

	List<Map<String, Object>> images = (List<Map<String, Object>>) root.get(IMAGESLIST);

	images.addAll(imageList);

	return root;

    }
    
   
   
    /**
     * Method to create annotations from a json payload of coordinates. We are assuming that the order that we requested positions for is maintained by Starsky.
     * @param query {@code String} The actual query, e.g. http://searchme/search/oa?q=turnips, then query is turnips
     * @param coordinatePayload {@code String} The json payload of coordinates returned when we send off our term vector positions for query matches
     * @param isW3c - {@code boolean} true if we want W3C Annotations returned
     * @param positionMap - {@code Map}
     * @param termPositionMap - {@code Map}
     * @param queryString - {@code String} The entire query String e.g. http://searchme/search/oa?q=turnips
     * @param pageParams - {@code PageParameters} An object that holds the page parameters for our annotation
     * @param isMixedSearch - {@code boolean} true if we are searching both text and annotations 
     * @return {@code Map} a Map representing the json for an text-derived annotation. 
     */
    public Map<String, Object> createCoordinateAnnotation(String query, String coordinatePayload,
	   boolean isW3c, Map<String, List<Positions>> positionMap,
	    Map<String, Map<String, TermOffsetStart>> termPositionMap, String queryString,
	    PageParameters pageParams, boolean isMixedSearch, Map<String,String> imageCanvasMap) {

	if (null == coordinatePayload) {
	    return null;
	}
	Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(coordinatePayload, Map.class);

	if (null == javaRootBodyMapObject) {
	    return null;
	}
	
	String[] queryArray = query.split(" ");
	int  queryArrayLength = queryArray.length;

	Map<String, Object> root;

	
	root = this.buildAnnotationPageHead(queryString, isW3c, pageParams, true);	   
	

	List<Map> resources = this.getResources(root,isW3c);
	
	this.setHits(root);
	
	List images = (List) javaRootBodyMapObject.get(IMAGESLIST);
	
	//iterate through the images
	for (Object object : (List) images) {
	    Map<String, Object> image = (Map<String, Object>) object;
	    
	    //pull out the image id 
	    
	    
	    
	    String imageId = (String) image.get("image_uri");
	    String id = imageCanvasMap.get(imageId);

	    //map for storing xywh keys against resource urls
	    Map <String, String>annoURLMap = new HashMap<>();
	    List<Positions> positionList = positionMap.get(id);
	    Map<String, TermOffsetStart> sourcePositionMap = termPositionMap.get(id);
	    
	    Object phraseObject = image.get(PHRASES);
	   
	    
	    List<Map<String, Object>> hitList = this.getHits(root);
	    
	    if (phraseObject instanceof ArrayList) {
		List phraseObjectList = (ArrayList) image.get(PHRASES);
		
		int count = 0;
		for(Object innerPhraseArray:phraseObjectList){
		    if (innerPhraseArray instanceof ArrayList) {
			List innerObjectList = (ArrayList) innerPhraseArray;

			Map<String,String> xywhMap = new HashMap<>();
			String xywh =  null;
			String termCount;

			int queryCount = 0;	
			String[] beforeAfter = null;
			for(Object phraseArray:innerObjectList){
			    
			    LinkedTreeMap map = (LinkedTreeMap)phraseArray;  
			    xywh = (String)map.get("xywh");			    
			    termCount =  removeDotZero((Double)map.get("count")) +"";
			    
			    int start = positionList.get(count).getStartPosition();
			    int end = positionList.get(count).getEndPosition();
			    beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);

			    StringBuilder queryForResource = new StringBuilder();

			    int countInt = Integer.parseInt(termCount);
			    if(queryArrayLength > countInt){
				if(queryCount == 0){
				   for(int r= 0; r <countInt;r++){
				       queryForResource.append(queryArray[r] + " "); 
				   }
				   
				   xywhMap.put(xywh, queryForResource.substring(0, queryForResource.length()-1));
				}else{
				    Collection<String> stringList = xywhMap.values();
				    int countOfElements = 0;
				    
				    for(String value:stringList){					
					String[] valueArray = value.split(" ");
					countOfElements += valueArray.length;
				    }
				    for(int r= countOfElements; r<countInt+countOfElements;r++){
					queryForResource.append(queryArray[r] + " "); 
				    }
				    xywhMap.put(xywh, queryForResource.substring(0, queryForResource.length()-1));
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
			    List<String> annotationsList = new ArrayList<>();
			    annotationsList.add(annoURLMap.get(xywh));
			    
			    setHits(isW3c, hitMap, annotationsList, query, beforeAfter);
			    resources.add(createResource(id, query,isW3c, xywh,annoURLMap.get(xywh))); 
			}else{
			    List <String>list = new ArrayList<>(xywhMap.keySet());
			    Collections.sort(list, Collections.reverseOrder());
			    Set <String>resultSet = new LinkedHashSet<>(list);
			    List<String> annotationsList = new ArrayList<>();
			    for(String xywhKey:resultSet){
				String partQuery = xywhMap.get(xywhKey);
				annotationsList.add(annoURLMap.get(xywhKey));
				resources.add(createResource(id, partQuery, isW3c, xywhKey,annoURLMap.get(xywhKey))); 
			    }
			    setHits(isW3c, hitMap, annotationsList,query, beforeAfter);
			    LOG.info("Hit:" +hitMap.toString());
			}
			hitList.add(hitMap);
		    }
		    count++;
		}
	    }   
	}
	
	
	
	if(TextSearchServiceImpl.DEFAULT_TEXT_PAGING_NUMBER > resources.size()&& !isMixedSearch){
	    if (isW3c) {
		root.remove(W3C_WITHIN_IS_PART_OF);
		root.remove(W3C_STARTINDEX);
	    }else{
		root.remove(OA_WITHIN);
		root.remove(OA_STARTINDEX);
	    }
	    root.remove(NEXT);
	    root.remove(PREV);
	}
	
	
	return root;

    }
    
  
    /**
     * Method to set up an empty root Map for the ImageList 
     * @return {@code Map} representing a map containing an images key with an empty {@code List} 
     */
    protected Map<String, Object> buildImageListHead() {
	Map<String, Object> root = new HashMap<>();
	
	List <Object>images = new ArrayList<>();
	root.put(IMAGESLIST, images);

	return root;
    }
    
   
  
    /**
     * Method to populate the positionMap,(key is the image id {@code String} and value is a list of {@code Position} objects) and create a 
     * json payload to send to the text server (Starsky).
     * This is of the form:
     *<pre>
     * 
     * {@code
     *{
     *    'images': [
     *    {
     *	    'imageURI' : <uri>,
     *	    'positions' : [ [25, 31], 100,110], // list of integers or integer arrays * representing ordinal character positions within text for image
     *	    'width' : 1024, // height and width of image to be presented
     *	    'height' : 768 // text server scales from stored boxes
     *    } 
     *  ]
     *}
     *}
     *</pre>
     * @param termWithOffsetsMap {@code Map} containing image ids as keys and a List of their {@code TermWithTermOffsets} as values.
     * @param width {@code String} The width to scale the coordinates from Starsky.
     * @param height {@code String}  The height to scale the coordinates from Starsky.
     * @param offsetPositionMap {@code Map} containing {@code String}  keys with image id and a {@code Map}
     * @return {@code List} - representing the json payload to send to Starsky. 
     * 
     */
    public List<Map<String, Object>> createImages(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
	    String width, String height, Map<String, Map<String, String>> offsetPositionMap) {

	List<Map<String, Object>> realRoot = new ArrayList<>();

	Map<String, List<Positions>> imagePositionsMap = new HashMap<>();

	Set<String> keySet = termWithOffsetsMap.keySet();
	for (String imageId : keySet) {
	    Map<String, Object> root = new HashMap<>();
	    List<TermWithTermOffsets> termWithOffsetsList = termWithOffsetsMap.get(imageId);

	    List<Object> positions = new ArrayList<>();

	    Map<String, String> startMap = offsetPositionMap.get(imageId);

	    List<Positions> positionsList = new ArrayList<>();

	    if (termWithOffsetsList.size() == 1) {
		List<TermOffsetsWithPosition> offsets = termWithOffsetsList.get(0).getOffsets();
		for (TermOffsetsWithPosition offset : offsets) {
		    positions.add(offset.getStart());
		    Positions positionObject = new Positions(offset.getPosition(), offset.getPosition());
		    positionsList.add(positionObject);
		}

	    } else {
		positions = sortPositionsForMultiwordPhrase(termWithOffsetsList, startMap, positionsList);

	    }
	    imagePositionsMap.put(imageId, positionsList);

	    root.put("imageURI", imageId);
	    root.put("positions", positions);
	    if(!StringUtils.isEmpty(width)){
		root.put("width", width);
	    }
	    if(!StringUtils.isEmpty(height)){
		root.put("height", height);
	    }

	    realRoot.add(root);
	}

	setPositionsMap(imagePositionsMap);

	return realRoot;
    }
    
    
    /**
     * Method to populate the positionMap,(key is the image id {@code String} and value is a list of {@code Position} objects) and create a 
     * json payload to send to the text server (Starsky).
     * This is of the form:
     *<pre>
     * 
     * {@code
     *{
     *    'images': [
     *    {
     *	    'imageURI' : <uri>,
     *	    'positions' : [ [25, 31], 100,110], // list of integers or integer arrays * representing ordinal character positions within text for image
     *	    'width' : 1024, // height and width of image to be presented
     *	    'height' : 768 // text server scales from stored boxes
     *    } 
     *  ]
     *}
     *}
     *</pre>
     * @param termWithOffsetsMap {@code Map} containing image ids as keys and a List of their {@code TermWithTermOffsets} as values.
     * @param width {@code String} The width to scale the coordinates from Starsky.
     * @param height {@code String}  The height to scale the coordinates from Starsky.
     * @param offsetPositionMap {@code Map} containing {@code String}  keys with image id and a {@code Map}
     * @return {@code List} - representing the json payload to send to Starsky. 
     * 
     */
    public List<Map<String, Object>> createImages(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
	    String width, String height, Map<String, Map<String, String>> offsetPositionMap, Map<String,String> canvasImageMap) {

	List<Map<String, Object>> realRoot = new ArrayList<>();

	Map<String, List<Positions>> imagePositionsMap = new HashMap<>();

	Set<String> keySet = termWithOffsetsMap.keySet();
	for (String canvasId : keySet) {
	    Map<String, Object> root = new HashMap<>();
	    List<TermWithTermOffsets> termWithOffsetsList = termWithOffsetsMap.get(canvasId);

	    List<Object> positions = new ArrayList<>();

	    Map<String, String> startMap = offsetPositionMap.get(canvasId);

	    List<Positions> positionsList = new ArrayList<>();

	    if (termWithOffsetsList.size() == 1) {
		List<TermOffsetsWithPosition> offsets = termWithOffsetsList.get(0).getOffsets();
		for (TermOffsetsWithPosition offset : offsets) {
		    positions.add(offset.getStart());
		    Positions positionObject = new Positions(offset.getPosition(), offset.getPosition());
		    positionsList.add(positionObject);
		}

	    } else {
		positions = sortPositionsForMultiwordPhrase(termWithOffsetsList, startMap, positionsList);

	    }
	    
	    String imageURL = canvasImageMap.get(canvasId);
	    
	    imagePositionsMap.put(canvasId, positionsList);
	   
	    root.put("imageURI", imageURL);
	    root.put("positions", positions);
	    if(!StringUtils.isEmpty(width)){
		root.put("width", width);
	    }
	    if(!StringUtils.isEmpty(height)){
		root.put("height", height);
	    }

	    realRoot.add(root);
	}

	setPositionsMap(imagePositionsMap);

	return realRoot;
    }
    
    
    
    /**
     * Our term with offsets is just a List right now, so say our query is fox brown, we loop through for the offsets of brown 
     * capturing the start int and then do an inner loop through the offsets of fox to see if we can find the end of fox plus 1(the space).
     * If these match (the start of brown and the end of fox plus 1) then we add these to our intList. This will work for phrases of 2 words. 
     * If the phrase contains more that 2 words then we do:
     * We still have an intList containing start and ends for pairs e.g.
     * fox brown laughs
     * we have the list so that we know brown is before laughs and another with fox is before laughs. So we basically loop through them and 
     * find where the start of one is the end of another.
     * 
     *   
     * We are assured that all terms are separated by a space from Starsky.  
     * @param termWithOffsetsList. A {@code List} of the terms with their offsets
     * @return {@code List<String>}
     */
    public List<String> workThoughOffsets(List<TermWithTermOffsets> termWithOffsetsList) {
	int size = termWithOffsetsList.size();

	List<String> intList = new ArrayList<>();

	for (int y = size - 1; y > 0; y--) {
	    TermWithTermOffsets termMatching = termWithOffsetsList.get(y);
	    TermWithTermOffsets termMatchingPrevious = termWithOffsetsList.get(y - 1);

	    for (TermOffsetsWithPosition termOffset : termMatching.getOffsets()) {
		int start = termOffset.getStart();

		for (TermOffsetsWithPosition previousTermOffset : termMatchingPrevious.getOffsets()) {
		    int end = previousTermOffset.getEnd() + 1;
		    if (start == end) {
			intList.add(previousTermOffset.getStart() + "|" + end);
		    }
		}
	    }
	}

	if (size > 2) {

	    List<String> mergedIntList = new ArrayList<>();

	    for (String listItem : intList) {
		String[] testFirst = listItem.split("[|]");

		for (String innerListItem : intList) {

		    String[] testsecond = innerListItem.split("[|]");
		    if (testFirst[0].equals(testsecond[1])) {
			boolean isFound = false;
			int count = 0;
			for (String mergedInt : mergedIntList) {

			    if (mergedInt.startsWith(testsecond[1] + "|")) {
				isFound = true;
				mergedInt = testsecond[0] + "|" + mergedInt;
				mergedIntList.set(count, mergedInt);
				break;
			    }
			    count++;
			}
			if (!isFound) {
			    mergedIntList.add(innerListItem + "|" + testFirst[1]);
			}
		    }
		}
	    }
	    intList = mergedIntList;
	}

	LOG.info("intList is " + intList);
	return intList;
    }
    
    
    /**
     * This method populates the List of Positions which is the start and end positions of the searched for query.
     * @param termWithOffsetsList {@code List} of {@code TermWithTermOffsets} containing only the matched terms and a {@code List} of all their positions and offsets. 
     * @param offsetPositionMap {@code Map} of the start and end positions of our matched query.
     * @param positionsList {@code List} of the start and end positions of each matched query.
     * @return {@code List} of the positions for a multiword phrase.
     */
    public List<Object> sortPositionsForMultiwordPhrase( List<TermWithTermOffsets> termWithOffsetsList,
	    Map <String,String> offsetPositionMap ,
	    List <Positions>positionsList) {

	 List<String>  stringSets = workThoughOffsets(termWithOffsetsList);
	 
	 List <String>templist;
	 List<Object> position = new ArrayList<>();
	 for(String item:stringSets){
	     templist = new ArrayList<>();
	     String[] stringArray = item.split("[|]");
	     for(String numberInArray:stringArray){
		 templist.add(numberInArray); 
	     }
	     
	     int start = Integer.parseInt(offsetPositionMap.get(templist.get(0)));
	     int end =  Integer.parseInt(offsetPositionMap.get(templist.get(templist.size() - 1))); 

	     positionsList.add(new Positions(start, end));
	     
	     position.add(templist);
    
	 }
	 LOG.info("sortPositionsForMultiwordPhrase position "+ position);
	 return position;
    }
    
    
    
    /**
     * Method to create a resource for a text-derived Annotation
     * @param imageId - {@code String} of the image Id
     * @param query - The query term(s) {@code String} e.g. ?q=turnips in the ground
     * @param isW3c - {@code boolean} true if we want W3C Annotations returned
     * @param xywh - {@code String} to create the target consisting of the image Id with coordinates attached e.g.  imageId#xywh=12,34,34,567
     * @param annoUrl - {@code String} a manufactured identifier for linking a hit to this resource.
     * @return - {@code Map} containing the resource.
     */
    public Map<String, Object> createResource(String imageId, String query, boolean isW3c, String xywh,
	    String annoUrl) {

	Map<String, Object> resource = new LinkedHashMap<>();
	resource.put(ROOT_ID, annoUrl);

	Map<String, Object> body = new LinkedHashMap<>();
	if (isW3c) {
	    body.put(ROOT_TYPE, "http://www.w3.org/2011/content#ContentAsText");
	    body.put("http://www.w3.org/2011/content#chars", query);
	    resource.put(ROOT_TYPE, "Annotation");
	    resource.put("motivation", "http://iiif.io/api/presentation/2#painting");
	    resource.put("body", body);
	    resource.put("target", imageId + "#xywh=" + xywh);
	} else {
	    body.put(ROOT_TYPE, "cnt:ContentAsText");
	    body.put("chars", query);
	    resource.put(ROOT_TYPE, "oa:Annotation");
	    resource.put("motivation", "sc:painting");
	    resource.put("resource", body);
	    resource.put("on", imageId + "#xywh=" + xywh);
	}

	return resource;
    }
    
    
    
    /**
     * Method to populate the Hits json
     * @param isW3c - {@code boolean} true if we want W3C Annotations returned.
     * @param hitMap - {@code Map} containing the hit.
     * @param annotationsList - {@code List} The resource urls that this hit references.
     * @param query - The query term(s) {@code String} e.g. ?q=turnips in the ground
     * @param beforeAfter - A {@code String[]} containing the text before[0] and after[1] the matched query.  
     */
    public void setHits(boolean isW3c, Map<String, Object> hitMap, List<String> annotationsList, String query,
	    String[] beforeAfter) {

	    hitMap.put("@type", "search:Hit");
	    hitMap.put("annotations", annotationsList);
	    hitMap.put("match", query);
	    hitMap.put("before", beforeAfter[0]);
	    hitMap.put("after", beforeAfter[1]);
	
    }
    
    
    
   
    /**
     * Method to manufacture a resource for the annotation
     * @param queryString The entire query {@code String}  e.g. http://www.searchme/search/oa?q=turnips
     * @param xywh The xywh coordinates{@code String}  e.g. 1234,4,45,36
     * @return The new {@code String}  representing the resource which is the original queryString minus the parameters with /searchResult followed by 8 random alphabetic characters and the xywh coordinates
     * e.g. http://www.searchme/search/oa/searchResultfert4dfg1234,4,45,36
     */
    public String createMadeUpResource (String queryString,String xywh) {
   	
	String query = queryString.substring(0, queryString.indexOf("?"));
	
	String searchResultRandom = RandomStringUtils.randomAlphabetic(8);
	
	query = query + "/searchResult"+searchResultRandom+xywh;
	return query;
    }
    
    
    
    public static void main(String[] args){
	
	byte[] encodedBytes = Base64.getEncoder().encode("http://localhost:8000/iiif/x/667f6125-89fa-44da-998e-0888be904f9f/manifest".getBytes());
	System.out.println("encodedBytes " + new String(encodedBytes));
	
    }
    
   
    
    
   
}
