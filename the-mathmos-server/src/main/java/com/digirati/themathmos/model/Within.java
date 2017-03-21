package com.digirati.themathmos.model;

import java.util.HashMap;
import java.util.Map;

public class Within {
    
    
    
    
    private Map <String, String>withinMap;
    
    
    
    public Within(){
	
	withinMap = new HashMap<>();
	
	withinMap.put("digukmhl", "http://wellcomelibrary.org/service/collections/collections/digukmhl/");
    }
    
    public Map<String, String> getWithinMap(){
	return withinMap;
    }
    
    public void  setWithinMap(Map<String, String> withinMap){
   	this.withinMap = withinMap;
     }

}
