package com.digirati.themathmos.service;

import java.util.Map;

import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.service.impl.AnnotationUtils;

public interface AnnotationAutocompleteService {
    
    
    public AnnotationUtils getAnnotationUtils();
    
    public ServiceResponse<Map<String, Object>> getTerms(String query, String motivation, String date, String user, String min, String queryString, boolean isW3c, String within);
    
    public ServiceResponse<Map<String, Object>> getTerms(Parameters parameters, String min, String queryString, boolean isW3c, String within);   
    
    public ServiceResponse<Map<String, Object>> getTerms(String query, String min, String queryString, boolean isW3c, String within);
    
    public ServiceResponse<Map<String, Object>> getMixedTerms(String query, String min, String queryString, boolean isW3c, String within);  

}
