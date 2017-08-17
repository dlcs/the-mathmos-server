package com.digirati.themathmos.model;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ContextCompletion {

    private String[] input;
    private Object contexts;

    private ContextCompletion() {
	// required by mapper to instantiate object
    }

    public ContextCompletion(String[] input) {
	this.input = input;
    }

    public Object getContexts() {
	return contexts;
    }

    public void setContexts(Object contexts) {
	this.contexts = contexts;
    }

    public String[] getInput() {
	return input;
    }

    public void setInput(String[] input) {
	this.input = input;
    }

   
}
