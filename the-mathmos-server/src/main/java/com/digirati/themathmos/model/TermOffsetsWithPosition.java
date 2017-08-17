package com.digirati.themathmos.model;

public class TermOffsetsWithPosition extends TermOffsets{

    private int position;

    public int getPosition() {
	return position;
    }

    public void setPosition(int position) {
	this.position = position;
    }

    @Override
    public String toString(){
	return "position: " + position +" start:" + this.getStart() +" end:"+this.getStart();
    }
}
