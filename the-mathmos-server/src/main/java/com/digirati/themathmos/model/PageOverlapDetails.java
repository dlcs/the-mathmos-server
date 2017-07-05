package com.digirati.themathmos.model;

public class PageOverlapDetails {
    
    
    public PageOverlapDetails(){
	
    }
    
    
    
    private String canvasId;
    private String imageId;
    private String nextCanvasId;
    private String nextImageId;
    private int endPositionOfCurrentText;
    
    public String getCanvasId() {
	return canvasId;
    }
    public void setCanvasId(String canvasId) {
	this.canvasId = canvasId;
    }
    public String getImageId() {
	return imageId;
    }
    public void setImageId(String imageId) {
	this.imageId = imageId;
    }
    public String getNextCanvasId() {
	return nextCanvasId;
    }
    public void setNextCanvasId(String nextCanvasId) {
	this.nextCanvasId = nextCanvasId;
    }
    public String getNextImageId() {
	return nextImageId;
    }
    public void setNextImageId(String nextImageId) {
	this.nextImageId = nextImageId;
    }
    public int getEndPositionOfCurrentText() {
	return endPositionOfCurrentText;
    }
    public void setEndPositionOfCurrentText(int endPositionOfCurrentText) {
	this.endPositionOfCurrentText = endPositionOfCurrentText;
    }
    

}
