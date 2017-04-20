package com.digirati.themathmos.model.annotation;

import java.util.Map;

public abstract class AbstractAnnotation {

    private String id;
    private Map<String, Object> jsonMap;
   
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getJsonMap() {
        return jsonMap;
    }

    public void setJsonMap(Map<String, Object> jsonMap) {
        this.jsonMap = jsonMap;
    }

    
}
