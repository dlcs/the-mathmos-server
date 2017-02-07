package com.digirati.themathmos.web.controller;

import javax.servlet.http.HttpServletRequest;



import org.springframework.web.bind.annotation.RestController;







@RestController(BasicController.CONTROLLER_NAME)
public class BasicController {
    
    
    public static final String CONTROLLER_NAME = "basicController";
    

    protected String createQueryString(HttpServletRequest request){
	String queryString = request.getRequestURL().toString();
	if(null != request.getQueryString()){
	    queryString += "?"+ request.getQueryString();
	}
	return queryString;
    }
  

}
