package com.digirati.themathmos.service.impl;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.google.common.collect.Iterators;

public class SearchQueryUtils {



    public void setUpBuilder(long totalHits, Client client){
	SearchHits searchHits = mock(SearchHits.class);

	when(searchHits.getTotalHits()).thenReturn(totalHits);

	SearchHit[] hits = new SearchHit[1];
	SearchHit hit = mock(SearchHit.class);
	hits[0] = hit;
	when(searchHits.iterator()).thenReturn(Iterators.forArray(hits));

	when(hit.getSourceAsString()).thenReturn(null);



	SearchResponse response = mock(SearchResponse.class);
	when(response.getHits()).thenReturn(searchHits);

	ListenableActionFuture<SearchResponse> action = mock(ListenableActionFuture.class);
	when(action.actionGet()).thenReturn(response);

	SearchRequestBuilder builder = mock(SearchRequestBuilder.class);
	when(builder.setQuery(anyObject())).thenReturn(builder);
	when(builder.setPostFilter(anyObject())).thenReturn(builder);
	when(builder.setFrom(anyInt())).thenReturn(builder);
	when(builder.setSize(anyInt())).thenReturn(builder);
	when(builder.execute()).thenReturn(action);

	when(client.prepareSearch("w3cannotation")).thenReturn(builder);
    }

}
