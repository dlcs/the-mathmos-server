package com.digirati.themathmos.model;

public class TermOffsetStart {
    
    private String term;
    private int start;
    
    
    public int getStart() {
	return start;
    }
    public void setStart(int start) {
	this.start = start;
    }
   
    
   
    public String getTerm() {
	return term;
    }
    public void setTerm(String term) {
	this.term = term;
    }
    
    @Override
    public String toString(){
	return "term:" + term +" start: " + start;
    }

}
