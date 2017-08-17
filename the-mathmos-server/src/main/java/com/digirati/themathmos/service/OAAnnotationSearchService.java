package com.digirati.themathmos.service;


import java.util.Map;

import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;

public interface OAAnnotationSearchService {


   public ServiceResponse<Map<String, Object>> getAnnotationPage(Parameters parameters, String queryString, String page, String within, String type);


}
