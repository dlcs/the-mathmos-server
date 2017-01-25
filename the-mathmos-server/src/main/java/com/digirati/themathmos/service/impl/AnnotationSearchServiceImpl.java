package com.digirati.themathmos.service.impl;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.mapper.W3CSearchAnnotationMapper;
import com.digirati.themathmos.model.W3CSearchAnnotation;
import com.digirati.themathmos.model.annotation.page.PageParameters;

@Service(AnnotationSearchServiceImpl.SERVICE_NAME)
public class AnnotationSearchServiceImpl {

    private final static Logger LOG = Logger.getLogger(AnnotationSearchServiceImpl.class);

    public static final String SERVICE_NAME = "annotationSearchServiceImpl";
    SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD'T'hh:mm:ssZ");

    
    private Client client;

    protected AnnotationUtils annotationUtils;
    
    protected static final int DEFAULT_PAGING_NUMBER = AnnotationSearchConstants.DEFAULT_PAGING_NUMBER;;
    private static final int DEFAULT_STARTING_PAGING_NUMBER = 0;
        
    private long totalHits = 0;  
 
    private PageParameters pagingParameters = null;
    
    

    @Autowired
    public AnnotationSearchServiceImpl(AnnotationUtils annotationUtils, ElasticsearchTemplate template) {
	this.annotationUtils = annotationUtils;
	this.client = template.getClient();
    }
    
    
    public PageParameters getPageParameters(){
	return pagingParameters;
    }
    
    public long getTotalHits(){
	return totalHits;
    }
    
   
    public String[] getAnnotationsPage(String query, String motivation, String date, String user, String queryString,
	    boolean isW3c, String page)  {
	
	totalHits = 0;
	
	pagingParameters = null;
	
	int pagingSize = DEFAULT_PAGING_NUMBER;
	int from = DEFAULT_STARTING_PAGING_NUMBER;
	
	//TODO validate that pagenumber is int and is in expected range.
	if(!StringUtils.isEmpty(page)){
	    Integer pagingInteger =  Integer.parseInt(page);
	    
	    from = (pagingInteger.intValue()-1) * pagingSize;
	    
	}

	Page<W3CSearchAnnotation> annotationPage = null;
	
	QueryBuilder builder = buildAllThings(query,motivation,date, user);
	annotationPage = formQuery(builder,from,pagingSize);
	
	if(null == annotationPage){
	    return new String[0];
	}
	String[] annoSearchArray = new String[annotationPage.getNumberOfElements()];
	
	LOG.info(String.format("Our paged search returned [%s] items ", annotationPage.getNumberOfElements()));
	int count = 0;
	for (W3CSearchAnnotation w3CAnnotation : annotationPage) {
	    String jsonLd;
	    if (isW3c) {
		jsonLd = w3CAnnotation.getW3cJsonLd();
	    } else {
		jsonLd = w3CAnnotation.getOaJsonLd();
	    }
	    annoSearchArray[count] = jsonLd;
	    count++;
	}
	pagingParameters = annotationUtils.getAnnotationPageParameters(annotationPage, queryString, DEFAULT_PAGING_NUMBER, totalHits);
	return annoSearchArray;	
	
    }
   
    
    private Page<W3CSearchAnnotation> formQuery(QueryBuilder queryBuilder,int pageNumber, int pagingSize){
   	Pageable pageable  = new PageRequest(pageNumber, pagingSize);
   	
   	W3CSearchAnnotationMapper resultsMapper = new W3CSearchAnnotationMapper();

   	SearchRequestBuilder searchRequestBuilder  = client.prepareSearch("w3cannotation");
   	searchRequestBuilder.setQuery(queryBuilder);	
   	searchRequestBuilder.setPostFilter(QueryBuilders.boolQuery());
   	searchRequestBuilder.setFrom(pageNumber).setSize(pagingSize);
   		
   	LOG.info("doSearch query "+ searchRequestBuilder.toString());
   	SearchResponse response = searchRequestBuilder.execute()
   		.actionGet();
   	
   	totalHits = response.getHits().totalHits();
   	LOG.info("Total hits are: "+totalHits);
   	
   	return resultsMapper.mapResults(response, W3CSearchAnnotation.class, pageable);
       }

    
    private String getPagingParam(String queryString, int replacementParamValue){
	if(!queryString.contains("page=")){
	    return queryString+"&page="+replacementParamValue;
	}
	return queryString.replaceAll("page=[^&]+","page="+replacementParamValue);
    }
    
    
   
   
   private QueryBuilder buildDateRangeQuery(String field,String from, String to){
       QueryBuilder dateRange = QueryBuilders.rangeQuery(field).from(from).to(to).includeLower(true).includeUpper(true);
       return dateRange;
   }
   
   private List<QueryBuilder> buildDateRangeQuery(String field, String allRanges) {
	List<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
	List<String> dates = annotationUtils.getListFromSpaceSeparatedTerms(allRanges);
	QueryBuilder buildDateRangeQuery;
	for (String dateString : dates) {

	    try {
		String[] splitDate = dateString.split("[/]");
		if (splitDate.length != 2) {
		    throw new SearchQueryException(
			    "Please enter dates with the ISO8601 format YYYY-MM-DDThh:mm:ssZ/YYYY-MM-DDThh:mm:ssZ");
		} else {
		    DatatypeConverter.parseDateTime(splitDate[0]);
		    DatatypeConverter.parseDateTime(splitDate[1]);
		    
		    buildDateRangeQuery = buildDateRangeQuery(field, splitDate[0], splitDate[1]);
		    queryBuilders.add(buildDateRangeQuery);  
		}
	    } catch (IllegalArgumentException e) {
		LOG.debug(String.format("Wrong date format entered for [%s] ",allRanges), e);
		throw new SearchQueryException("Please enter dates with the ISO8601 format YYYY-MM-DDThh:mm:ssZ");
	    } 
	}
	
	return queryBuilders;
    }
   
    private QueryBuilder buildDates(String field, String allRanges){
	List<QueryBuilder> dates = buildDateRangeQuery(field, allRanges);
	
	BoolQueryBuilder should = QueryBuilders.boolQuery();
	
	for(QueryBuilder dateRange:dates){
	    should =  should.should(dateRange);
	}
	return should;
    }
	
    private QueryBuilder buildAllThings(String query,String motivations, String allDateRanges, String users) {
	List <QueryBuilder> queryList  = new ArrayList<QueryBuilder>();
	
	BoolQueryBuilder must = QueryBuilders.boolQuery();
	
	
	if(null != query){
	    String tidyQuery = annotationUtils.convertSpecialCharacters(query);
	    
	    must = must.must(QueryBuilders.multiMatchQuery(tidyQuery, "body","target","bodyURI", "targetURI").type(Type.PHRASE));

	   // must = must.must(QueryBuilders.queryStringQuery(tidyQuery).field("body").field("target").field("bodyURI").field("targetURI"));
	    
	}
		
	if(null != motivations){
   
	    if(motivations.contains("non-")){
		
		List<String>motivationsList = annotationUtils.getListFromSpaceSeparatedTerms(motivations);
		if(motivationsList.size() > 1){
		    throw new SearchQueryException(
    			"You have a motivation that is a non-<motivation>, there can only be one motivation in this instance."); 
		}else{		  
		    motivations = motivations.replaceAll("non-", "");
		    queryList.add(QueryBuilders.existsQuery("motivations"));		    
		    must = must.mustNot(QueryBuilders.queryStringQuery(motivations).field("motivations"));
   
		}
	    }else{
		queryList.add(QueryBuilders.queryStringQuery(motivations).field("motivations"));
	    }
	}

	if(null != allDateRanges){
	    queryList.add(buildDates("created", allDateRanges));
	}

	if(null != users){
	    queryList.add(QueryBuilders.queryStringQuery(users).field("creators"));
	}
   	
 
   	for(QueryBuilder eachQuery:queryList){
   	    must =  must.must(eachQuery);
   	}
   	
   	
   	return must;
     }
    
   

 
}
