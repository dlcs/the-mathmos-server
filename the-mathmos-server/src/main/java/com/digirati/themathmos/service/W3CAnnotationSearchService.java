package com.digirati.themathmos.service;

import java.util.Map;

import com.digirati.themathmos.model.ServiceResponse;


public interface W3CAnnotationSearchService {
    
    
    public ServiceResponse<Map<String, Object>> getAnnotationPage(String query, String motivation, String date, String user, String queryString, String page);
}
