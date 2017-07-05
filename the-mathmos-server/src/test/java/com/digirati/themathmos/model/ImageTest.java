package com.digirati.themathmos.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
	Image image  = new Image();
	image.setImage_uri("http://imageuri");
	List<List<XYWHObject>> phrases = new ArrayList<>();
	
	image.setPhrases(phrases);
	
	XYWHObject obj = new XYWHObject(1, "23,24,3556,45");
	List <XYWHObject> objList = new ArrayList<>();
	objList.add(obj);
	
	phrases.add(objList);
	
	System.out.println(image.toString());
	assertTrue(image.getImage_uri().equals("http://imageuri"));
	assertTrue(image.getPhrases().get(0).get(0).getCount() == 1);
	
	XYWHObject obj2 = new XYWHObject(2, "33,24,3556,45");
	objList.add(obj2);
	assertTrue(image.getPhrases().get(0).get(1).getCount() == 2);
    }

}
