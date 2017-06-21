package com.digirati.themathmos.model;

import org.springframework.data.elasticsearch.core.completion.Completion;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ContextCompletion extends Completion{

  
    private Object context;
    
    public ContextCompletion(String[] input) {
	super(input);
    }
    public Object getContext() {
	return context;
    }
    public void setContext(Object context) {
	this.context = context;
    }

}
