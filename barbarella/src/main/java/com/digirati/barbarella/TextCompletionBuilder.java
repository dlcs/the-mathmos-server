package com.digirati.barbarella;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.query.IndexQuery;



public class TextCompletionBuilder {

    	private TextAnnotation result;

	public TextCompletionBuilder(String id) {
		result = new TextAnnotation();
	}

	public TextCompletionBuilder suggest(String[] input) {
		return suggest(input, null, null, null);
	}

	public TextCompletionBuilder suggest(String[] input, String output) {
		return suggest(input, output, null, null);
	}

	public TextCompletionBuilder suggest(String[] input, String output, Object payload) {
		return suggest(input, output, payload, null);
	}

	public TextCompletionBuilder suggest(String[] input, String output, Object payload, Integer weight) {
	    
	        String [] inputWords = analyseInput(input);
		Completion suggest = new Completion(inputWords);
		suggest.setOutput(output);

		suggest.setPayload(payload);		
		suggest.setWeight(weight);

		return this;
	}

	public TextAnnotation build() {
		return result;
	}

	public IndexQuery buildIndex() {
		IndexQuery indexQuery = new IndexQuery();
		indexQuery.setId(result.getId());
		indexQuery.setObject(result);
		return indexQuery;
	}
	

	private String[]  analyseInput(String[] input){
	    Set<String> inputAnalysisSet = new HashSet<>();
	    for (String term:input){
		
		term = parseHtml(term);
		String lowerTerm = term.toLowerCase();
		inputAnalysisSet.add(lowerTerm);
		String[] words = lowerTerm.split("\\s+");
		
		for(String word:words){		
		    inputAnalysisSet.add(word);
		}
	    }
	    
	    return  inputAnalysisSet.toArray(new String[inputAnalysisSet.size()]);
	}
	
	private String parseHtml(String input){
	    Pattern htmlPattern = Pattern.compile(".*\\<[^>]+>.*", Pattern.DOTALL);
	    boolean isHTML = htmlPattern.matcher(input).matches();
	    if(isHTML){
		return Jsoup.parse(input).text();
	    }else{
		return input;
	    }
	}
	
	
	
}
