package com.digirati.themathmos.model;

import java.util.List;

public class TermWithTermOffsets {

    private String term;
    private List<TermOffsetsWithPosition> offsets;

    public String getTerm() {
	return term;
    }
    public void setTerm(String term) {
	this.term = term;
    }
    public List<TermOffsetsWithPosition> getOffsets() {
	return offsets;
    }
    public void setOffsets(List<TermOffsetsWithPosition> offsets) {
	this.offsets = offsets;
    }

    @Override
    public String toString(){
	return "term:" + term+ " postion offsets:"+offsets.toString();
    }
}
