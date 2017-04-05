package com.digirati.themathmos.service;

import java.util.Map;

import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;

public interface AnnotationAutocompleteService {
    
    
    public ServiceResponse<Map<String, Object>> getTerms(String query, String motivation, String date, String user, String min, String queryString, boolean isW3c, String within);
    
    public ServiceResponse<Map<String, Object>> getTerms(Parameters parameters, String min, String queryString, boolean isW3c, String within);   
    
    public ServiceResponse<Map<String, Object>> getTerms(String query, String min, String queryString, boolean isW3c, String within);
    
    public ServiceResponse<Map<String, Object>> getMixedTerms(String query, String min, String queryString, boolean isW3c, String within);  

}
