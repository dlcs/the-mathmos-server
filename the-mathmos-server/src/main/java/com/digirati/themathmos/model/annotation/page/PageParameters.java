package com.digirati.themathmos.model.annotation.page;

public class PageParameters {
    
    private String totalElements;
    private String firstPageNumber;
    private String lastPageNumber;
    private String nextPageNumber;
    private String previousPageNumber;
    private String startIndex;
    
    public String getTotalElements() {
	return totalElements;
    }
    public void setTotalElements(String totalElements) {
	this.totalElements = totalElements;
    }
    public String getFirstPageNumber() {
	return firstPageNumber;
    }
    public void setFirstPageNumber(String firstPageNumber) {
	this.firstPageNumber = firstPageNumber;
    }
    public String getLastPageNumber() {
	return lastPageNumber;
    }
    public void setLastPageNumber(String lastPageNumber) {
	this.lastPageNumber = lastPageNumber;
    }
    public String getNextPageNumber() {
	return nextPageNumber;
    }
    public void setNextPageNumber(String nextPageNumber) {
	this.nextPageNumber = nextPageNumber;
    }
    public String getPreviousPageNumber() {
	return previousPageNumber;
    }
    public void setPreviousPageNumber(String previousPageNumber) {
	this.previousPageNumber = previousPageNumber;
    }
    public String getStartIndex() {
	return startIndex;
    }
    public void setStartIndex(String startIndex) {
	this.startIndex = startIndex;
    }

}