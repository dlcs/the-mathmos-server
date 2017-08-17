package com.digirati.themathmos.model;

import java.util.List;
import java.util.Map;

public class ImageHelperObject {

    private List<Map<String, Object>> imageJson;
    private Map <String, List<Positions>> positionsMap;
    private Map<String, Object> offsetPayloadMap;
    private Map<String,String> crossPageImageMap;

    public List<Map<String, Object>> getImageJson() {
	return imageJson;
    }

    public void setImageJson(List<Map<String, Object>> imageJson) {
	this.imageJson = imageJson;
    }

    public Map <String, List<Positions>> getPositionsMap() {
	return positionsMap;
    }

    public void setPositionsMap(Map <String, List<Positions>> positionsMap) {
	this.positionsMap = positionsMap;
    }

    public Map<String, Object> getOffsetPayloadMap() {
	return offsetPayloadMap;
    }

    public void setOffsetPayloadMap(Map<String, Object> offsetPayloadMap) {
	this.offsetPayloadMap = offsetPayloadMap;
    }

    public Map<String,String> getCrossPageImageMap() {
	return crossPageImageMap;
    }

    public void setCrossPageImageMap(Map<String,String> crossPageImageMap) {
	this.crossPageImageMap = crossPageImageMap;
    }
}