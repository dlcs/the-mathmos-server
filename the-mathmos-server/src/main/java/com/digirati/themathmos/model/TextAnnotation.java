package com.digirati.themathmos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.completion.Completion;



@Document( indexName="text_index", type="text")
public class TextAnnotation {
    

    
	@Id
    	private String id;
	
	private String imageId;
    	
	private String text;
	
	@CompletionField (payloads = true)
    	private Completion suggest;
    	

	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}
    	

	public String getImageId() {
	    return imageId;
	}

	public void setImageId(String imageId) {
	    this.imageId = imageId;
	}

	public String getText() {
	    return text;
	}

	public void setText(String text) {
	    this.text = text;
	}
	
	
	public Completion getSuggest() {
	    return suggest;
	}

	public void setSuggest(Completion suggest) {
	    this.suggest = suggest;
	}
	
	
    	@Override
	public String toString() {
		return "Text [(" + getId() + "),(" + getImageId() + "),(" + getText() + "),(" + getSuggest().getInput() + ")]";
		
	}
	

}
