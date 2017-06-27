package com.digirati.themathmos.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ContextCompletion {

    private String[] input;
    private String output;
    private Integer weight;
    private Object payload;
    private Object context;

    private ContextCompletion() {
	// required by mapper to instantiate object
    }

    public ContextCompletion(String[] input) {
	this.input = input;
    }

    public Object getContext() {
	return context;
    }

    public void setContext(Object context) {
	this.context = context;
    }

    public String[] getInput() {
	return input;
    }

    public void setInput(String[] input) {
	this.input = input;
    }

    public String getOutput() {
	return output;
    }

    public void setOutput(String output) {
	this.output = output;
    }

    public Object getPayload() {
	return payload;
    }

    public void setPayload(Object payload) {
	this.payload = payload;
    }

    public Integer getWeight() {
	return weight;
    }

    public void setWeight(Integer weight) {
	this.weight = weight;
    }
}
