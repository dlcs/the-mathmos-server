package com.digirati.themathmos.model;

import java.util.List;

public class Images {

    private List <Image> images;

    public Images(){

    }

    public Images(List<Image> images){
	this.images = images;
    }

    public List <Image> getImages() {
	return images;
    }

    public void setImages(List <Image> images) {
	this.images = images;
    }
}