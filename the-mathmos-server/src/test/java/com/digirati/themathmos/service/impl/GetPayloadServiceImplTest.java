package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class GetPayloadServiceImplTest {
    
    private GetPayloadServiceImpl getPayloadServiceImpl;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	getPayloadServiceImpl = new GetPayloadServiceImpl();
    }

    @Test
    public void testGetPayloadServiceImpl() {
	assertNotNull(getPayloadServiceImpl.httpClient.toString());
    }

    @Test
    public void testGetJsonPayload() {
	
	String payload = "";
	String url = "https://jsonplaceholder.typicode.com/posts";
	String returnedPayload = getPayloadServiceImpl.getJsonPayload(url, payload);
	
	assertEquals(returnedPayload, "");
	
	url = "http://code.jsontest.com"; 
	returnedPayload = getPayloadServiceImpl.getJsonPayload(url, payload);
	assertNotEquals(returnedPayload, "");
    }

}
