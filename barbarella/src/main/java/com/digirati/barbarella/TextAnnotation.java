package com.digirati.barbarella;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;



@Document( indexName="text_index", type="text")
public class TextAnnotation {
    

    
	@Id
    	private String id;
    	
	private String text;
    	

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
	

}
