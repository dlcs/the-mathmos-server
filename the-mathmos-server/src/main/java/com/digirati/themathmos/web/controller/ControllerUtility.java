package com.digirati.themathmos.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;



public class ControllerUtility {
    
    
 
    

    protected String createQueryString(HttpServletRequest request){
	String queryString = request.getRequestURL().toString();
	if(null != request.getQueryString()){
	    queryString += "?"+ request.getQueryString();
	}
	return queryString;
    }
    
    
    protected boolean validateParameters(String query, String motivation, String date, String user){

	if(StringUtils.isEmpty(query) && StringUtils.isEmpty(motivation) && StringUtils.isEmpty(date) && StringUtils.isEmpty(user)){
	    return false;
	}else{
	    return true;
	}
    }
  
    protected boolean validateQueryParameter(String query, String motivation, String date, String user){

   	if(StringUtils.isEmpty(query)){
   	    return false;
   	}else{
   	    return true;
   	}
    }

}
