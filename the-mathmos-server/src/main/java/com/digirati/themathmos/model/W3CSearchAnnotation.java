package com.digirati.themathmos.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.completion.Completion;

@Document( indexName="w3cannotation", type="annotations")
public class W3CSearchAnnotation {
    

    
	@Id
    	private String id;
    	
    	private List<String> motivations;
    	
    	@Field( type = FieldType.Date)
    	private List<String> created;
    	
    	@Field( type = FieldType.Date)
    	private List<String> generated;
    	
    	@Field( type = FieldType.Date)
    	private List<String> modified;
    	
    	@Field(type = FieldType.String, analyzer = "whitespace")
    	private List<String> creators;
    	
    	private List<String> generator;
    	
    	private List <String> target;
    	
    	private List <String> body;
    	
    	@Field(type = FieldType.String, analyzer = "whitespace")
    	private List <String> targetURI;
    	
    	@Field(type = FieldType.String, analyzer = "whitespace")
    	private List <String> uri;
    	
    	@Field(type = FieldType.String, analyzer = "whitespace")
    	private List <String> bodyURI;
    	
    	private List <String> xywh;
    	
    	@Field( type = FieldType.String, index = FieldIndex.no)
    	private String w3cJsonLd;
    	
    	@Field( type = FieldType.String, index = FieldIndex.no)
    	private String oaJsonLd;
    	
    	@CompletionField (payloads = true)
    	private Completion suggest;


	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}
    	

	public List<String> getMotivations() {
	    return motivations;
	}

	public void setMotivations(List<String> motivations) {
	    this.motivations = motivations;
	}

	
	public List<String> getCreated() {
	    return created;
	}

	public void setCreated(List<String> created) {
	    this.created = created;
	}	

	public List<String> getGenerated() {
	    return generated;
	}

	public void setGenerated(List<String> generated) {
	    this.generated = generated;
	}

	public List<String> getModified() {
	    return modified;
	}

	public void setModified(List<String> modified) {
	    this.modified = modified;
	}

	public List<String> getCreators() {
	    return creators;
	}

	public void setCreators(List<String> creators) {
	    this.creators = creators;
	}

	public List<String> getGenerator() {
	    return generator;
	}

	public void setGenerator(List<String> generator) {
	    this.generator = generator;
	}

	public List <String> getTarget() {
	    return target;
	}

	public void setTarget(List <String> target) {
	    this.target = target;
	}

	public List <String> getBody() {
	    return body;
	}

	public void setBody(List <String> body) {
	    this.body = body;
	}
	
	public List <String> getTargetURI() {
	    return targetURI;
	}

	public void setTargetURI(List <String> targetURI) {
	    this.targetURI = targetURI;
	}
	
	public List <String> getURI() {
	    return uri;
	}

	public void setURI(List <String> URI) {
	    this.uri = URI;
	}

	public List <String> getBodyURI() {
	    return bodyURI;
	}

	public void setBodyURI(List <String> bodyURI) {
	    this.bodyURI = bodyURI;
	}
    	
    	@Override
	public String toString() {
		return "W3CAnnotation [(" + getId() + ")]";
		
	}

	public String getW3cJsonLd() {
	    return w3cJsonLd;
	}

	public void setW3cJsonLd(String w3cJsonLd) {
	    this.w3cJsonLd = w3cJsonLd;
	}

	public String getOaJsonLd() {
	    return oaJsonLd;
	}

	public void setOaJsonLd(String oaJsonLd) {
	    this.oaJsonLd = oaJsonLd;
	}
	
	public Completion getSuggest() {
	    return suggest;
	}

	public void setSuggest(Completion suggest) {
	    this.suggest = suggest;
	}

	public List <String> getXywh() {
	    return xywh;
	}

	public void setXywh(List <String> xywh) {
	    this.xywh = xywh;
	}
    	

}
