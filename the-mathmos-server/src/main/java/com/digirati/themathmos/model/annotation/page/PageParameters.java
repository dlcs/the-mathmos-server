package com.digirati.themathmos.model.annotation.page;

public class PageParameters {

    private int total;

    private String firstPageNumber;
    private String lastPageNumber;
    private String nextPageNumber;
    private int nextPage = 0;
    private int lastPage = 0;
    private String previousPageNumber;
    private String startIndexString;

    private int startIndex;

    public int getTotal() {
	return total;
    }
    public void setTotal(int total) {
	this.total = total;
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
    public int getStartIndex() {
	return startIndex;
    }
    public void setStartIndex(int startIndex) {
	this.startIndex = startIndex;
    }
    public int getNextPage() {
	return nextPage;
    }
    public void setNextPage(int nextPage) {
	this.nextPage = nextPage;
    }
    public int getLastPage() {
	return lastPage;
    }
    public void setLastPage(int lastPage) {
	this.lastPage = lastPage;
    }
    public String getStartIndexString() {
	return startIndexString;
    }
    public void setStartIndexString(String startIndexString) {
	this.startIndexString = startIndexString;
    }


    @Override
    public String toString(){
	return "total:" +this.getTotal() + " startIndex:" +this.getStartIndex() + " nextPage:" + this.getNextPage() ;
    }

}
