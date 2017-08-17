package com.digirati.themathmos.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;

@Document(indexName = "text_index", type = "plaintext")
public class TextAnnotation {

    @Id
    private String id;

    @Field(type = FieldType.keyword, index = false)
    private String imageId;

    @Field(type = FieldType.keyword)
    private List<String> manifestId;

    @Mapping(mappingPath = "/mappings/text-field-mappings.json")
    private String plaintext;

    @CompletionField
    @Mapping(mappingPath = "/mappings/suggest-field-mappings.json")
    private ContextCompletion suggest;

    @Field(type = FieldType.keyword, index = false)
    private String nextImageId;

    @Field(type = FieldType.keyword, index = false)
    private String nextCanvasId;

    @Field(type = FieldType.Integer, index = false)
    private int endPositionOfCurrentText;

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

    public String getPlaintext() {
	return plaintext;
    }

    public void setPlaintext(String plaintext) {
	this.plaintext = plaintext;
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
	return "Plaintext [(" + getId() + "),(" + getImageId() + "),(" + getPlaintext() + "),(" + getManifestId()
		+ "),(" + getSuggest().getInput() + ")" + ",(nextCanvasId: " + this.getNextCanvasId() + ")"
		+ ",(nextImageId: " + this.getNextImageId() + "), (endPositionOfCurrentText: "
		+ this.getEndPositionOfCurrentText() + "]" + "getSuggest:" + getSuggest();

    }

    public String getNextImageId() {
	return nextImageId;
    }

    public void setNextImageId(String nextImageId) {
	this.nextImageId = nextImageId;
    }

    public String getNextCanvasId() {
	return nextCanvasId;
    }

    public void setNextCanvasId(String nextCanvasId) {
	this.nextCanvasId = nextCanvasId;
    }

    public int getEndPositionOfCurrentText() {
	return endPositionOfCurrentText;
    }

    public void setEndPositionOfCurrentText(int endPositionOfCurrentText) {
	this.endPositionOfCurrentText = endPositionOfCurrentText;
    }

}
