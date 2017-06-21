package com.digirati.themathmos.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;






@Document( indexName="text_index", type="text")
public class TextAnnotation {
    

    @Id
	private String id;
	
	private String imageId;
	
	@Field(type = FieldType.String, analyzer = "whitespace")
	private List<String> manifestId;
	
	private String text;
	
	@CompletionField (payloads = false)
	private ContextCompletion suggest;
	

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
	
	
	public ContextCompletion getSuggest() {
	    return suggest;
	}

	public void setSuggest(ContextCompletion newSuggest) {
	    this.suggest = newSuggest;
	}
	
	
	public List<String> getManifestId() {
	    return manifestId;
	}

	public void setManifestId(List<String> manifestId) {
	    this.manifestId = manifestId;
	}

	@Override
	public String toString() {
		return "Text [(" + getId() + "),(" + getImageId() + "),(" + getText() + "),(" + getManifestId() + "),(" + getSuggest().getInput() + ")]";
		
	}
	

}
