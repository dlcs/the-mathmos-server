package com.digirati.themathmos.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;


public class GetPayloadServiceImplTest {

    private GetPayloadServiceImpl getPayloadServiceImpl;

    private static final Logger LOG = Logger.getLogger(GetPayloadServiceImpl.class);

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

	//url = "http://starsky.dlcs-ida.org/coordsimage/0/";

	url = "http://www.google.com";
	Map<String, Object> root = new HashMap<>();

	List <Object>images = new ArrayList<>();

	Map<String, Object> imageRoot = new HashMap<>();
	images.add(imageRoot);
	root.put("images", images);

	imageRoot.put("imageURI", "https://dlcs-ida.org/iiif-img/2/1/M-1011_R-09_0058");
	List <Object>positions = new ArrayList<>();
	positions.add(0);
	imageRoot.put("positions", positions);
	imageRoot.put("width", 1024);
	imageRoot.put("height", 768);


	payload = new Gson().toJson(root);



	returnedPayload = getPayloadServiceImpl.getJsonPayload(url, payload);
	LOG.info(returnedPayload);

	//url = "http://starsky.dlcs-ida.org/coordsimage/0/";
	url = "http://starsky.dlcs-ida.org/coords/";

	imageRoot.put("imageURI", "https://dlcs.io/iiif-img/wellcome/1/3d134dfb-8e62-4ee1-8034-86d28e253e43");
	positions.add(3);
	Map<String, Object> imageRoot2 = new HashMap<>();
	images.add(imageRoot2);
	imageRoot2.put("imageURI", "https://dlcs.io/iiif-img/wellcome/1/9b955204-adc4-417f-b340-0ad9bea63ff6");
	List <Object> positions2 = new ArrayList<>();
	positions2.add(278);
	imageRoot2.put("positions", positions2);
	imageRoot2.put("width", 1024);
	imageRoot2.put("height", 768);

	payload = new Gson().toJson(root);



	returnedPayload = getPayloadServiceImpl.getJsonPayload(url, payload);
	LOG.info(returnedPayload);


    }

}
