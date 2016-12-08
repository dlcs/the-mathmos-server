package com.digirati.themathmos.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;



@Document(indexName="text_index", type="text")
public class TextAnnotation {
    

    
	@Id
    	private String id;
    	
	private String text;
    	

    	//@CompletionField (payloads = false)
    	//private Completion suggest;


	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}
    	

	public String getText() {
	    return text;
	}

	public void setText(String text) {
	    this.text = text;
	}
	
	
    	
    	@Override
	public String toString() {
		return "Text [(" + getId() + ")]";
		
	}

	
	//public Completion getSuggest() {
	//    return suggest;
	//}

	//public void setSuggest(Completion suggest) {
	//    this.suggest = suggest;
	//}

	
    	

}