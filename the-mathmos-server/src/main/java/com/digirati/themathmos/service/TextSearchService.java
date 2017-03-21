package com.digirati.themathmos.service;


import java.util.Map;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.annotation.page.PageParameters;

public interface TextSearchService {
    
    

   public ServiceResponse<Map<String, Object>> getTextPositions(String query, String queryString, boolean isW3c, String page, boolean isMixedSearch, String within);
   
    
    public long getTotalHits();
    
    public PageParameters getPageParameters();

}
