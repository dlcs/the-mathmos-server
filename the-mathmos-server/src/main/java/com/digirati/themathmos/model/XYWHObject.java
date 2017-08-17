package com.digirati.themathmos.model;

public class XYWHObject {


    private int count;
    private String xywh;

    public XYWHObject(){
    }

    public XYWHObject(int count, String xywh){
	this.count = count;
	this.xywh = xywh;
    }


    public int getCount() {
	return count;
    }
    public void setCount(int count) {
	this.count = count;
    }
    public String getXywh() {
	return xywh;
    }
    public void setXywh(String xywh) {
	this.xywh = xywh;
    }

    @Override
    public String toString(){
	return "count:" + this.count +   " xywh:" + this.xywh;
    }
}
