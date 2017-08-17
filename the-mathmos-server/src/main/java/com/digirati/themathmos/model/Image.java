package com.digirati.themathmos.model;

import java.util.List;

public class Image {

    private String image_uri;
    private List<List<XYWHObject>> phrases;

    public Image() {

    }

    public Image(String image_uri, List<List<XYWHObject>> phrases) {
	this.image_uri = image_uri;
	this.phrases = phrases;
    }

    public String getImage_uri() {
	return image_uri;
    }

    public void setImage_uri(String image_uri) {
	this.image_uri = image_uri;
    }

    public List<List<XYWHObject>> getPhrases() {
	return phrases;
    }

    public void setPhrases(List<List<XYWHObject>> phrases) {
	this.phrases = phrases;
    }

    @Override
    public String toString() {
	return "image_uri:" + this.image_uri + " phrases:" + this.phrases;
    }
}
