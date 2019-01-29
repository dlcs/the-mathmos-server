package com.digirati.themathmos.service.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.model.Image;
import com.digirati.themathmos.model.ImageHelperObject;
import com.digirati.themathmos.model.Images;
import com.digirati.themathmos.model.PageOverlapDetails;
import com.digirati.themathmos.model.PositionListObjects;
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

	// We are getting 10 words before and 10 words after our query for the Hits.
	private static final int BEFORE_AFTER_WORDS = 10;

	public ImageHelperObject createOffsetPayload(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap,
			String width, String height, Map<String, Map<String, String>> offsetPositionMap,
			Map<String, PageOverlapDetails> canvasOverlapDetailMap) {

		if (null == termWithOffsetsMap || termWithOffsetsMap.isEmpty()) {
			return null;
		}

		Map<String, Object> root = buildImageListHead();

		ImageHelperObject imageHelper = createImages(termWithOffsetsMap, width, height, offsetPositionMap,
				canvasOverlapDetailMap);
		List<Map<String, Object>> imageList = imageHelper.getImageJson();
		List<Map<String, Object>> images = (List<Map<String, Object>>) root.get(IMAGESLIST);

		images.addAll(imageList);

		imageHelper.setOffsetPayloadMap(root);
		return imageHelper;

	}

	/**
	 *
	 * @param query
	 *            {@code String} The query e.g. "we are family"
	 * @param images
	 *            {@code Images} The java representation of the images payload
	 *            from the coordinate service
	 * @param imageHelper
	 *            {@code ImageHelperObject} A helper object to store some
	 *            objects for the writing of annotations
	 * @param imageCanvasMap
	 *            {@code Map} A map of canvasIds mapped to image Ids and vice
	 *            versa.
	 * @return {@code Map} with the key : xywh {@code String} and value: chars
	 *         for the resource {@code String}
	 */

	public Map<String, String> createCoordinateAnnotationFromImages(String query, Images images,
			ImageHelperObject imageHelper, Map<String, String> imageCanvasMap) {

		Map<String, String> crossPageImageMap = imageHelper.getCrossPageImageMap();
		LOG.info("crossPageImageMap: " + crossPageImageMap);

		String[] queryArray = query.split(" ");
		int queryArrayLength = queryArray.length;

		Map<String, String> crossCoordinates = new HashMap<>();

		if (images != null) {
			List<Image> imageList = images.getImages();

			for (Image image : imageList) {
				String imageId = image.getImage_uri();
				String id = imageCanvasMap.get(imageId);

				if (null != crossPageImageMap && crossPageImageMap.containsKey(imageId)) {
					String crossImageId = crossPageImageMap.get(imageId);
					String crossCanvasId = crossPageImageMap.get(id);
					Image crossImage = null;
					for (Image testImage : imageList) {
						if (testImage.getImage_uri().equals(crossImageId)) {
							crossImage = testImage;
							break;
						}
					}

					LOG.info("crossImageId: " + crossImageId);
					LOG.info("crossCanvasId: " + crossCanvasId);
					int countCount = 0;

					// get the last xywh in the current image
					int currentSize = image.getPhrases().get(0).size();
					int currentCount = image.getPhrases().get(0).get(currentSize - 1).getCount();

					// get the first xywh in the next image
					int nextCount = crossImage.getPhrases().get(0).get(countCount).getCount();
					String nextXywh = crossImage.getPhrases().get(0).get(countCount).getXywh();

					String endQuery = "";
					for (int y = (currentCount); y < (currentCount + nextCount); y++) {
						endQuery += queryArray[y] + " ";
					}
					int countTally = currentCount + nextCount;

					// deal with test that might be on a new line
					while (countTally < queryArrayLength) {
						countCount++;
						countTally = amendCrossCoordinates(crossImage, countCount, countTally, queryArray,
								crossCoordinates);
					}
					endQuery = endQuery.substring(0, endQuery.length() - 1);
					crossCoordinates.put(nextXywh, endQuery);
				}
			}
			LOG.info("crossCoordinates: " + crossCoordinates);
		}

		return crossCoordinates;

	}

	/**
	 * Method to deal with cross boundary resource chars that might be across
	 * multiple lines
	 *
	 * @param crossImage
	 *            {@code Image}
	 * @param next
	 *            {@code int}
	 * @param countTally
	 *            {@code int} the count of the next phrase for an image
	 * @param queryArray
	 *            {@code String[]} the query in a String[]
	 * @param crossCoordinates
	 *            {@code Map} we place key:xywh and value: words for resource
	 *            chars for this coordinate match
	 * @return {@code int} the count of words we have placed in the
	 *         crossCoordinates map.
	 */

	public int amendCrossCoordinates(Image crossImage, int next, int countTally, String[] queryArray,
			Map<String, String> crossCoordinates) {
		int nextAgainCount = crossImage.getPhrases().get(0).get(next).getCount();
		String nextAgainXywh = crossImage.getPhrases().get(0).get(next).getXywh();
		String endAgainQuery = "";
		for (int p = (countTally); p < (countTally + nextAgainCount); p++) {
			endAgainQuery += queryArray[p] + " ";
		}
		endAgainQuery = endAgainQuery.substring(0, endAgainQuery.length() - 1);
		crossCoordinates.put(nextAgainXywh, endAgainQuery);
		return countTally + nextAgainCount;
	}

	/**
	 * Method to create annotations from a json payload of coordinates. We are
	 * assuming that the order that we requested positions for is maintained by
	 * Starsky.
	 *
	 * @param query
	 *            {@code String} The actual query, e.g.
	 *            http://searchme/search/oa?q=turnips, then query is turnips
	 * @param coordinatePayload
	 *            {@code String} The json payload of coordinates returned when
	 *            we send off our term vector positions for query matches
	 * @param isW3c
	 *            - {@code boolean} true if we want W3C Annotations returned
	 * @param positionMap
	 *            - {@code Map}
	 * @param termPositionMap
	 *            - {@code Map}
	 * @param queryString
	 *            - {@code String} The entire query String e.g.
	 *            http://searchme/search/oa?q=turnips
	 * @param pageParams
	 *            - {@code PageParameters} An object that holds the page
	 *            parameters for our annotation
	 * @param isMixedSearch
	 *            - {@code boolean} true if we are searching both text and
	 *            annotations
	 * @return {@code Map} a Map representing the json for an text-derived
	 *         annotation.
	 */
	public Map<String, Object> createCoordinateAnnotation(String query, String coordinatePayload, boolean isW3c,
			ImageHelperObject imageHelper, Map<String, Map<String, TermOffsetStart>> termPositionMap,
			String queryString, PageParameters pageParams, boolean isMixedSearch, Map<String, String> imageCanvasMap,
			Map<String, String> crossXywhQueryMap) {

		if (null == coordinatePayload) {
			return null;
		}
		Map<String, Object> javaRootBodyMapObject = new Gson().fromJson(coordinatePayload, Map.class);

		Map<String, List<Positions>> positionMap = imageHelper.getPositionsMap();
		Map<String, String> crossPageImageMap = imageHelper.getCrossPageImageMap();
		LOG.info("crossPageImageMap: " + crossPageImageMap);

		if (null == javaRootBodyMapObject) {
			return null;
		}

		String[] queryArray = query.split(" ");
		int queryArrayLength = queryArray.length;
		LOG.info("queryArrayLength: " + queryArrayLength);

		Map<String, Object> root;

		root = this.buildAnnotationPageHead(queryString, isW3c, pageParams, true);

		List<Map> resources = this.getResources(root, isW3c);

		this.setHits(root);

		List images = (List) javaRootBodyMapObject.get(IMAGESLIST);
		List<Positions> crossPositionList = null;

		// iterate through the images
		for (Object object : (List) images) {
			Map<String, Object> image = (Map<String, Object>) object;

			// pull out the image id
			String imageId = (String) image.get("image_uri");
			String id = imageCanvasMap.get(imageId);

			// map for storing xywh keys against resource urls
			Map<String, String> annoURLMap = new HashMap<>();
			List<Positions> positionList = positionMap.get(id);

			LOG.debug("positionList: " + positionList);
			Map<String, TermOffsetStart> sourcePositionMap = termPositionMap.get(id);

			Object phraseObject = image.get(PHRASES);

			List<Map<String, Object>> hitList = this.getHits(root);

			if (phraseObject instanceof ArrayList) {
				List phraseObjectList = (ArrayList) image.get(PHRASES);

				int count = 0;
				for (Object innerPhraseArray : phraseObjectList) {
					if (innerPhraseArray instanceof ArrayList) {
						List innerObjectList = (ArrayList) innerPhraseArray;

						Map<String, String> xywhMap = new HashMap<>();
						String xywh = null;
						String termCount;

						int queryCount = 0;
						String[] beforeAfter = null;
						for (Object phraseArray : innerObjectList) {
							LinkedTreeMap map = (LinkedTreeMap) phraseArray;
							xywh = (String) map.get("xywh");
							termCount = removeDotZero((Double) map.get("count")) + "";

							int start = positionList.get(count).getStartPosition();
							int end = positionList.get(count).getEndPosition();
							if (null != sourcePositionMap) {
								beforeAfter = this.getHighlights(start, end, BEFORE_AFTER_WORDS, sourcePositionMap);
							}

							StringBuilder queryForResource = new StringBuilder();

							int countInt = Integer.parseInt(termCount);
							if (queryArrayLength > countInt) {
								if (queryCount == 0) {
									for (int r = 0; r < countInt; r++) {
										queryForResource.append(queryArray[r] + " ");
									}

									xywhMap.put(xywh, queryForResource.substring(0, queryForResource.length() - 1));
								} else {
									Collection<String> stringList = xywhMap.values();
									int countOfElements = 0;

									for (String value : stringList) {
										String[] valueArray = value.split(" ");
										countOfElements += valueArray.length;
									}

									for (int r = countOfElements; r < countInt + countOfElements; r++) {
										queryForResource.append(queryArray[r] + " ");
									}

									xywhMap.put(xywh, queryForResource.substring(0, queryForResource.length() - 1));
								}
								if (null != crossXywhQueryMap && crossXywhQueryMap.containsKey(xywh)) {
									xywhMap.put(xywh, crossXywhQueryMap.get(xywh));
								}
								queryCount++;
							} else {
								xywhMap.put(xywh, query);
							}
							String annoUrl = createMadeUpResource(queryString, xywh);
							annoURLMap.put(xywh, annoUrl);
						}
						LOG.debug("xywhMap " + xywhMap);
						Map<String, Object> hitMap = new LinkedHashMap<>();

						List<String> list = new ArrayList<>(xywhMap.keySet());
						Collections.sort(list, Collections.reverseOrder());
						Set<String> resultSet = new LinkedHashSet<>(list);
						List<String> annotationsList = new ArrayList<>();
						for (String xywhKey : resultSet) {
							String partQuery = xywhMap.get(xywhKey);
							annotationsList.add(annoURLMap.get(xywhKey));
							resources.add(createResource(id, partQuery, isW3c, xywhKey, annoURLMap.get(xywhKey)));
						}
						if (null != beforeAfter) {
							setHits(isW3c, hitMap, annotationsList, query, beforeAfter);
						}

						LOG.debug("Hit:" + hitMap.toString());
						if (!hitMap.isEmpty()) {
							hitList.add(hitMap);
						}
					}
					count++;
				}
			}
		}

		if (TextSearchServiceImpl.DEFAULT_TEXT_PAGING_NUMBER > resources.size() && !isMixedSearch) {
			if (isW3c) {
				root.remove(W3C_WITHIN_IS_PART_OF);
				root.remove(W3C_STARTINDEX);
			} else {
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
	 *
	 * @return {@code Map} representing a map containing an images key with an
	 *         empty {@code List}
	 */
	protected Map<String, Object> buildImageListHead() {
		Map<String, Object> root = new HashMap<>();

		List<Object> images = new ArrayList<>();
		root.put(IMAGESLIST, images);

		return root;
	}

	/**
	 * Method to populate the positionMap,(key is the image id {@code String}
	 * and value is a list of {@code Position} objects) and create a json
	 * payload to send to the text server (Starsky). This is of the form:
	 *
	 * <pre>
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
	 * </pre>
	 *
	 * @param termWithOffsetsMap
	 *            {@code Map} containing image ids as keys and a List of their
	 *            {@code TermWithTermOffsets} as values.
	 * @param width
	 *            {@code String} The width to scale the coordinates from
	 *            Starsky.
	 * @param height
	 *            {@code String} The height to scale the coordinates from
	 *            Starsky.
	 * @param offsetPositionMap
	 *            {@code Map} containing {@code String} keys with image id and a
	 *            {@code Map}
	 * @return {@code List} - representing the json payload to send to Starsky.
	 *
	 */
	public ImageHelperObject createImages(Map<String, List<TermWithTermOffsets>> termWithOffsetsMap, String width,
			String height, Map<String, Map<String, String>> offsetPositionMap,
			Map<String, PageOverlapDetails> canvasOverlapDetailMap) {

		ImageHelperObject imageHelper = new ImageHelperObject();
		List<Map<String, Object>> realRoot = new ArrayList<>();

		List<Object> newPositions = null;
		PositionListObjects newPositionsList = null;
		Map<String, List<Positions>> imagePositionsMap = new HashMap<>();

		Set<String> keySet = termWithOffsetsMap.keySet();
		for (String canvasId : keySet) {
			Map<String, Object> root = new HashMap<>();
			List<TermWithTermOffsets> termWithOffsetsList = termWithOffsetsMap.get(canvasId);

			List<Object> positions = new ArrayList<>();

			Map<String, String> startMap = offsetPositionMap.get(canvasId);

			List<Positions> positionsList = new ArrayList<>();

			String imageURL = canvasOverlapDetailMap.get(canvasId).getImageId();

			if (termWithOffsetsList.size() == 1) {
				List<TermOffsetsWithPosition> offsets = termWithOffsetsList.get(0).getOffsets();
				for (TermOffsetsWithPosition offset : offsets) {
					positions.add(offset.getStart());
					Positions positionObject = new Positions(offset.getPosition(), offset.getPosition());
					positionsList.add(positionObject);
				}

			} else {

				positions = sortPositionsForMultiwordPhrase(termWithOffsetsList, startMap, positionsList);
				newPositionsList = sortMultiTermPosition(positions, canvasOverlapDetailMap.get(canvasId),
						positionsList);
				newPositions = newPositionsList.getNewPositions();

			}

			imagePositionsMap.put(canvasId, positionsList);

			root.put("imageURI", imageURL);
			root.put("positions", positions);
			if (!StringUtils.isEmpty(width)) {
				root.put("width", width);
			}
			if (!StringUtils.isEmpty(height)) {
				root.put("height", height);
			}

			realRoot.add(root);

			// only add the new image if we cross a page boundary
			if (null != newPositions && !newPositions.isEmpty()) {
				Map<String, Object> newRoot = new HashMap<>();
				newRoot.put("imageURI", canvasOverlapDetailMap.get(canvasId).getNextImageId());
				newRoot.put("positions", newPositions);
				if (!StringUtils.isEmpty(width)) {
					newRoot.put("width", width);
				}
				if (!StringUtils.isEmpty(height)) {
					newRoot.put("height", height);
				}
				realRoot.add(newRoot);
				imagePositionsMap.put(canvasOverlapDetailMap.get(canvasId).getNextCanvasId(),
						newPositionsList.getPositionsList());
				Map<String, String> crossPageImageMap = new HashMap<>();
				crossPageImageMap.put(imageURL, canvasOverlapDetailMap.get(canvasId).getNextImageId());
				crossPageImageMap.put(canvasId, canvasOverlapDetailMap.get(canvasId).getNextCanvasId());
				imageHelper.setCrossPageImageMap(crossPageImageMap);
			}

		}
		imageHelper.setPositionsMap(imagePositionsMap);
		imageHelper.setImageJson(realRoot);
		return imageHelper;
	}

	public PositionListObjects sortMultiTermPosition(List<Object> positions, PageOverlapDetails overlapDetails,
			List<Positions> positionsList) {

		PositionListObjects positionListObjects = new PositionListObjects();
		Iterator<Object> iter = positions.iterator();
		String imageId = overlapDetails.getNextImageId();
		int lastPositionOfCurrent = overlapDetails.getEndPositionOfCurrentText();
		List<Object> newPositions = new ArrayList<>();
		List<Object> newPositionsOuter = new ArrayList<>();
		List<Positions> newPositionsList = new ArrayList<>();
		int count = 0;
		while (iter.hasNext()) {
			List<String> position = (List) iter.next();
			Positions listPosition = positionsList.get(count);
			Iterator<String> positionIter = position.iterator();
			int positionCount = 0;
			while (positionIter.hasNext()) {
				String testPosition = positionIter.next();
				int testInt = Integer.parseInt(testPosition);
				// we disregard this hit if the phrase starts in the next page
				if (positionCount == 0 && testInt > lastPositionOfCurrent) {
					break;
				}
				if (testInt > lastPositionOfCurrent) {
					positionIter.remove();
					LOG.info(" remove testPosition:" + testPosition);
					newPositions.add((testInt - lastPositionOfCurrent - 1) + "");
				}
				positionCount++;

			}
			if (newPositions.size() > 0) {
				Positions newPosition = new Positions(0, newPositions.size() - 1);
				newPositionsOuter.add(newPositions);
				newPositionsList.add(newPosition);
				LOG.info(" newPositionsList:" + newPositionsList);
			}
			count++;
		}

		positionListObjects.setNewPositions(newPositionsOuter);
		positionListObjects.setPositionsList(newPositionsList);
		LOG.info("sortMultiTermPosition newPositions " + newPositionsOuter);
		LOG.info("sortMultiTermPosition positionsList " + newPositionsList);
		return positionListObjects;
	}

	/**
	 * Our term with offsets is just a List right now, so say our query is fox
	 * brown, we loop through for the offsets of brown capturing the start int
	 * and then do an inner loop through the offsets of fox to see if we can
	 * find the end of fox plus 1(the space). If these match (the start of brown
	 * and the end of fox plus 1) then we add these to our intList. This will
	 * work for phrases of 2 words. If the phrase contains more that 2 words
	 * then we do: We still have an intList containing start and ends for pairs
	 * e.g. fox brown laughs we have the list so that we know brown is before
	 * laughs and another with fox is before laughs. So we basically loop
	 * through them and find where the start of one is the end of another.
	 *
	 *
	 * We are assured that all terms are separated by a space from Starsky.
	 *
	 * @param termWithOffsetsList.
	 *            A {@code List} of the terms with their offsets
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
	 * This method populates the List of Positions which is the start and end
	 * positions of the searched for query.
	 *
	 * @param termWithOffsetsList
	 *            {@code List} of {@code TermWithTermOffsets} containing only
	 *            the matched terms and a {@code List} of all their positions
	 *            and offsets.
	 * @param offsetPositionMap
	 *            {@code Map} of the start and end positions of our matched
	 *            query.
	 * @param positionsList
	 *            {@code List} of the start and end positions of each matched
	 *            query.
	 * @return {@code List} of the positions for a multiword phrase.
	 */
	public List<Object> sortPositionsForMultiwordPhrase(List<TermWithTermOffsets> termWithOffsetsList,
			Map<String, String> offsetPositionMap, List<Positions> positionsList) {

		List<String> stringSets = workThoughOffsets(termWithOffsetsList);

		List<String> templist;
		List<Object> position = new ArrayList<>();
		for (String item : stringSets) {
			templist = new ArrayList<>();
			String[] stringArray = item.split("[|]");
			for (String numberInArray : stringArray) {
				templist.add(numberInArray);
			}

			int start = Integer.parseInt(offsetPositionMap.get(templist.get(0)));
			int end = Integer.parseInt(offsetPositionMap.get(templist.get(templist.size() - 1)));

			positionsList.add(new Positions(start, end));

			position.add(templist);

		}
		LOG.info("sortPositionsForMultiwordPhrase positionsList " + positionsList);
		LOG.info("sortPositionsForMultiwordPhrase position " + position);
		return position;
	}

	/**
	 * Method to create a resource for a text-derived Annotation
	 *
	 * @param imageId
	 *            - {@code String} of the image Id
	 * @param query
	 *            - The query term(s) {@code String} e.g. ?q=turnips in the
	 *            ground
	 * @param isW3c
	 *            - {@code boolean} true if we want W3C Annotations returned
	 * @param xywh
	 *            - {@code String} to create the target consisting of the image
	 *            Id with coordinates attached e.g. imageId#xywh=12,34,34,567
	 * @param annoUrl
	 *            - {@code String} a manufactured identifier for linking a hit
	 *            to this resource.
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

	public void amendStartsAndEndsPositions(String text, int startText, int endText) {

		String endPreviousText = text.substring(0, startText - 1);
		String currrentText = text.substring(startText, endText);
		String startNextText = text.substring(endText + 1, text.length());
		LOG.info("[" + text + "]");
		LOG.info("[" + currrentText + "]");
		LOG.info("[" + endPreviousText + "]");
		LOG.info("[" + startNextText + "]");

	}

	public static void main(String[] args) {

		byte[] encodedBytes = Base64.getEncoder()
				.encode("http://localhost:8000/iiif/x/667f6125-89fa-44da-998e-0888be904f9f/manifest".getBytes());
		LOG.info("http://localhost:8000/iiif/x/667f6125-89fa-44da-998e-0888be904f9f/manifest : encodedBytes "
				+ new String(encodedBytes));

		encodedBytes = Base64.getEncoder()
				.encode("https://presley.dlcs-ida.org/iiif/x/a72e7576-8c96-44d5-b164-1133c999c636/manifest".getBytes());
		LOG.info("https://presley.dlcs-ida.org/iiif/x/a72e7576-8c96-44d5-b164-1133c999c636/manifest : encodedBytes "
				+ new String(encodedBytes));

		encodedBytes = Base64.getEncoder()
				.encode("http://localhost:8000/iiif/x/86e22fc7-89ba-42f5-82af-9c0c292f28e2/manifest".getBytes());
		LOG.info("http://localhost:8000/iiif/x/86e22fc7-89ba-42f5-82af-9c0c292f28e2/manifest : encodedBytes "
				+ new String(encodedBytes));

		encodedBytes = Base64.getEncoder()
				.encode("http://localhost:8000/iiif/x/86e22fc7-89ba-42f5-82af-9c0c292f28e1/manifest".getBytes());
		LOG.info("http://localhost:8000/iiif/x/86e22fc7-89ba-42f5-82af-9c0c292f28e1/manifest : encodedBytes "
				+ new String(encodedBytes));

		encodedBytes = Base64.getEncoder()
				.encode("https://presley.glam-dev.org/iiif/customer/_roll_M-1011_127_cvs-17-17/manifest".getBytes());
		LOG.info("https://presley.glam-dev.org/iiif/customer/_roll_M-1011_127_cvs-17-17/manifest : encodedBytes "
				+ new String(encodedBytes));

		String encodedString = "aHR0cHM6Ly9wcmVzbGV5LmdsYW0tZGV2Lm9yZy9paWlmL3Rlc3QvX3JvbGxfTS0xMDExXzEyN19jdnMtMTMxLTEzMi9tYW5pZmVzdA==";
		encodedBytes = Base64.getDecoder().decode(encodedString);
		LOG.info(
				"aHR0cHM6Ly9wcmVzbGV5LmdsYW0tZGV2Lm9yZy9paWlmL3Rlc3QvX3JvbGxfTS0xMDExXzEyN19jdnMtMTMxLTEzMi9tYW5pZmVzdA== : decodedBytes "
						+ new String(encodedBytes));

	}
}
