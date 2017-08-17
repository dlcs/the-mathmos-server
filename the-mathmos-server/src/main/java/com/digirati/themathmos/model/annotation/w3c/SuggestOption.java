package com.digirati.themathmos.model.annotation.w3c;


public class SuggestOption  {

    private float score;
    private String text;


    public SuggestOption(float score, String text){
	this.score = score;
	this.text = text;
    }

    public SuggestOption(String text){
	this.text = text;
    }
    public float getScore() {
	return score;
    }
    public void setScore(float score) {
	this.score = score;
    }
    public String getText() {
	return text;
    }
    public void setText(String text) {
	this.text = text;
    }


}
