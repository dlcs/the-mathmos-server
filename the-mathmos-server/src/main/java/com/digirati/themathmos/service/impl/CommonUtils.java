package com.digirati.themathmos.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.common.text.Text;
import org.springframework.data.domain.Page;

import com.digirati.themathmos.model.TermOffsetStart;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.google.gson.internal.LinkedTreeMap;

public class CommonUtils {

    private final static Logger LOG = Logger.getLogger(CommonUtils.class);

    protected static final String WC3CONTEXT_PATH = "http://www.w3.org/ns/anno.jsonld";
    protected static final String FULL_LAYER = "http://iiif.io/api/presentation/2#Layer";

    protected static final String PRESENTATIONCONTEXT_PATH = "http://iiif.io/api/presentation/2/context.json";
    protected static final String SEARCHCONTEXT_PATH = "http://iiif.io/api/search/1/context.json";

    protected static final String FULL_HAS_ANNOTATIONS = "http://iiif.io/api/presentation/2#hasAnnotations";
    protected static final String FULL_HAS_TERMLIST = "http://iiif.io/api/search/1#hasTermList";

    protected static final String FULL_ANNOTATIONLIST = "http://iiif.io/api/presentation/2#AnnotationList";
    protected static final String OA_ANNOTATIONLIST = "sc:AnnotationList";
    
    protected static final String FULL_HAS_HITLIST = "http://iiif.io/api/search/1#hasHitList";
    protected static final String OA_HITS = "hits";

    protected static final String W3C_RESOURCELIST = "@list";
    protected static final String OA_TERMSLIST = "terms";
    protected static final String OA_RESOURCELIST = "resources";

    protected static final String CONTEXT = "@context";

    protected static final String ROOT_ID = "@id";
    protected static final String ROOT_TYPE = "@type";

    protected List getResources(Map<String, Object> root, boolean isW3c) {
	List resources;
	if (isW3c) {
	    Map map = (LinkedHashMap) root.get(FULL_HAS_ANNOTATIONS);
	    resources = (List) map.get(W3C_RESOURCELIST);
	} else {
	    resources = (List) root.get(OA_RESOURCELIST);
	}
	return resources;
    }

    protected void setResources(Map<String, Object> root, boolean isW3c) {
	List resources = new ArrayList();

	if (isW3c) {
	    Map map = new LinkedHashMap();
	    root.put(FULL_HAS_ANNOTATIONS, map);
	    map.put(W3C_RESOURCELIST, resources);
	} else {
	    root.put(OA_RESOURCELIST, resources);
	}
    }

    protected void setHits(Map<String, Object> root, boolean isW3c) {
	List hits = new ArrayList();
	if (isW3c) {
	    Map map = new LinkedHashMap();
	    root.put(FULL_HAS_HITLIST, map);
	    map.put(W3C_RESOURCELIST, hits);
	} else {
	    root.put(OA_HITS, hits);
	}
    }

    protected List getHits(Map<String, Object> root, boolean isW3c) {
	List hits;
	if (isW3c) {
	    Map map = (LinkedHashMap) root.get(FULL_HAS_HITLIST);
	    hits = (List) map.get(W3C_RESOURCELIST);
	} else {
	    hits = (List) root.get(OA_HITS);
	}
	return hits;
    }

    protected void setContextIdType(Map<String, Object> root, boolean isW3c, String query) {
	if (isW3c) {
	    root.put(CONTEXT, WC3CONTEXT_PATH);
	} else {
	    root.put(CONTEXT, PRESENTATIONCONTEXT_PATH);
	}

	root.put(ROOT_ID, query);
	if (isW3c) {
	    root.put(ROOT_TYPE, FULL_ANNOTATIONLIST);
	} else {
	    root.put(ROOT_TYPE, OA_ANNOTATIONLIST);
	}
    }


    protected Map<String, Object> buildAnnotationListHead(String query, boolean isW3c) {
	Map<String, Object> root = new LinkedHashMap<>();
	setContextIdType(root, isW3c, query);
	setResources(root, isW3c);
	return root;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> buildAnnotationPageHead(String query, boolean isW3c, PageParameters pagingParams) {
	String total = pagingParams.getTotalElements();
	String first = pagingParams.getFirstPageNumber();
	String last = pagingParams.getLastPageNumber();
	String next = pagingParams.getNextPageNumber();
	String previous = pagingParams.getPreviousPageNumber();
	String startIndex = pagingParams.getStartIndex();

	Map<String, Object> root = new LinkedHashMap<>();

	setContextIdType(root, isW3c, query);

	if (isW3c) {
	    Map withinMap = new LinkedHashMap();
	    withinMap.put("type", FULL_LAYER);
	    withinMap.put("as:totalItems", total);
	    withinMap.put("first", first);
	    withinMap.put("last", last);
	    root.put("dcterms:isPartOf", withinMap);
	} else {
	    Map withinMap = new LinkedHashMap();
	    withinMap.put(ROOT_TYPE, "sc:Layer");
	    withinMap.put("total", total);
	    withinMap.put("first", first);
	    withinMap.put("last", last);
	    root.put("within", withinMap);
	}

	if (null != next) {
	    root.put("next", next);
	}
	if (null != previous) {
	    root.put("prev", previous);
	}
	if (isW3c) {
	    root.put("as:startIndex", startIndex);
	} else {
	    root.put("startIndex", startIndex);
	}

	setResources(root, isW3c);

	return root;
    }

    public List<String> getListFromSpaceSeparatedTerms(String terms) {
	if (null != terms) {
	    String[] termsArray = terms.split("[ ]");

	    return Arrays.asList(termsArray);
	}
	return new ArrayList<String>();
    }

    public String[] getBeforeAndAfterFromText(Text fragment) {

	String textString = fragment.string();

	int lastIndexOfStartEm = textString.lastIndexOf("<em>");
	int lastIndexOfEndEm = textString.lastIndexOf("</em>");

	String[] startEnd = new String[2];
	startEnd[0] = textString.substring(0, lastIndexOfStartEm);
	startEnd[1] = textString.substring(lastIndexOfEndEm + 5, textString.length());

	return startEnd;
    }

    public String getCoordinatesFromSource(String source, String textString) {

	int lastIndexOfStartEm = textString.lastIndexOf("<em>");
	int lastIndexOfEndEm = textString.lastIndexOf("</em>");

	String[] startEnd = new String[2];
	startEnd[0] = textString.substring(0, lastIndexOfStartEm);
	startEnd[1] = textString.substring(lastIndexOfEndEm + 5, textString.length());

	int indexOfStart = source.indexOf(startEnd[0]) + startEnd[0].length();
	int indexOfEnd = source.indexOf(startEnd[1]);
	return indexOfStart + "|" + indexOfEnd;
    }

    
    
    
    public String[] getHighlights(int start, int end, int surroundingText,
	    Map<String, TermOffsetStart> sourcePositionMap) {
	String[] beforeAfter = new String[2];

	String before = "";
	for (int s = (start - surroundingText); s < start; s++) {
	    if (sourcePositionMap.containsKey(s + "")) {
		before += sourcePositionMap.get(s + "").getTerm() + " ";
	    }
	}
	
	beforeAfter[0] = before;
	String after = "";
	for (int e = end + 1; e < (end + surroundingText); e++) {
	    if (sourcePositionMap.containsKey(e + "")) {
		after += sourcePositionMap.get(e + "").getTerm() + " ";
	    }
	}

	if (after.length() > 0) {
	    after = " "+after.substring(0, after.length() - 1);
	}
	beforeAfter[1] = after;

	return beforeAfter;
    }
    
    public <T> PageParameters getAnnotationPageParameters(Page<T> annotationPage, String queryString, int defaultPagingNumber, long totalHits){
   	PageParameters parameters = new PageParameters();

   	
   	parameters.setTotalElements(totalHits+"");
   	
   	int lastPage = (int) (totalHits/defaultPagingNumber)+1;
   	parameters.setFirstPageNumber(getPagingParam(queryString, 1));

   	parameters.setLastPageNumber(getPagingParam(queryString,lastPage)); 
   	
   	int nextPage = (annotationPage.getNumber()/defaultPagingNumber)+2;
   	if(lastPage >= nextPage){
   	    parameters.setNextPageNumber(getPagingParam(queryString,nextPage));  
   	}
   	
   	if(annotationPage.hasPrevious() ){
   	    int previousPage = annotationPage.getNumber() /defaultPagingNumber ;
   	    parameters.setPreviousPageNumber(getPagingParam(queryString,previousPage)); 
   	}
   	parameters.setStartIndex(annotationPage.getNumber()+ "");
   	return parameters;
       }
       
       
    public String getPagingParam(String queryString, int replacementParamValue) {
	if (!queryString.contains("page=")) {
	    return queryString + "&page=" + replacementParamValue;
	}
	return queryString.replaceAll("page=[^&]+", "page=" + replacementParamValue);
    }
}
