package com.digirati.themathmos.model;

public class Positions {
    
    private int startPosition;
    private int endPosition;
    
    public Positions(int start, int end){
	this.startPosition = start;
	this.endPosition = end;
    }
    
    
    public int getStartPosition() {
	return startPosition;
    }
    public void setStartPosition(int startPosition) {
	this.startPosition = startPosition;
    }
    public int getEndPosition() {
	return endPosition;
    }
    public void setEndPosition(int endPosition) {
	this.endPosition = endPosition;
    }	

    @Override
    public String toString(){
	return "position start:"+ startPosition +":"+endPosition;
    }
    
    
}

