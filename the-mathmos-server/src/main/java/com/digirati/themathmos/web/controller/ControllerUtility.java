package com.digirati.themathmos.web.controller;

import javax.servlet.http.HttpServletRequest;



public class ControllerUtility {
    
    
 
    

    protected String createQueryString(HttpServletRequest request){
	String queryString = request.getRequestURL().toString();
	if(null != request.getQueryString()){
	    queryString += "?"+ request.getQueryString();
	}
	return queryString;
    }
  

}
