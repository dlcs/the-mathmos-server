package com.digirati.themathmos.service;


import java.util.Map;

import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.impl.TextUtils;

public interface TextSearchService {



   public ServiceResponse<Map<String, Object>> getTextPositions(String query, String queryString, boolean isW3c, String page, boolean isMixedSearch, String within, String widthHeight);


    public long getTotalHits();

    public PageParameters getPageParameters();

    public TextUtils getTextUtils();

}
