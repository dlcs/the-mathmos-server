package com.digirati.pygar;


import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.springframework.data.elasticsearch.core.completion.Completion;
import org.springframework.data.elasticsearch.core.query.IndexQuery;



public class W3CSearchAnnotationCompletionBuilder {

    	private W3CSearchAnnotation result;

	public W3CSearchAnnotationCompletionBuilder(String id) {
		result = new W3CSearchAnnotation();
	}

	public W3CSearchAnnotationCompletionBuilder suggest(String[] input) {
		return suggest(input, null, null, null);
	}

	public W3CSearchAnnotationCompletionBuilder suggest(String[] input, String output) {
		return suggest(input, output, null, null);
	}

	public W3CSearchAnnotationCompletionBuilder suggest(String[] input, String output, Object payload) {
		return suggest(input, output, payload, null);
	}

	public W3CSearchAnnotationCompletionBuilder suggest(String[] input, String output, Object payload, Integer weight) {
	    
	        String [] inputWords = analyseInput(input);
		Completion suggest = new Completion(inputWords);
		suggest.setOutput(output);
		suggest.setPayload(payload);
		
		suggest.setWeight(weight);

		result.setSuggest(suggest);
		return this;
	}

	public W3CSearchAnnotation build() {
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
		  //  if(!(word.startsWith("http:") || word.startsWith("https:"))){
		//	 word = word.replaceAll("[^a-zA-Z0-9\\s]", "");
		  //  }
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
