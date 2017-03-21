package com.digirati.themathmos.service;


import java.util.Map;

import com.digirati.themathmos.model.ServiceResponse;

public interface W3CSearchService {
    
    

    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query, String queryString, String page, String within, String type);

}
