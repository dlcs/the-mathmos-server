package com.digirati.themathmos.service;


import java.util.Map;

import com.digirati.themathmos.model.ServiceResponse;

public interface TextSearchService {
    
    

    public ServiceResponse<Map<String, Object>> getTextPositions(String query, String queryString, boolean isW3c, String page);

}
