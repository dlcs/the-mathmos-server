package com.digirati.themathmos.web.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.digirati.themathmos.AnnotationSearchConstants;
import com.digirati.themathmos.exception.SearchException;
import com.digirati.themathmos.exception.SearchQueryException;
import com.digirati.themathmos.model.Parameters;
import com.digirati.themathmos.model.ServiceResponse;
import com.digirati.themathmos.model.ServiceResponse.Status;
import com.digirati.themathmos.model.annotation.page.PageParameters;
import com.digirati.themathmos.service.AnnotationAutocompleteService;
import com.digirati.themathmos.service.W3CAnnotationSearchService;
import com.digirati.themathmos.service.impl.AnnotationUtils;

@RestController(W3CAnnotationSearchController.CONTROLLER_NAME)
public class W3CAnnotationSearchController {

	public static final String CONTROLLER_NAME = "w3cAnnotationSearchController";

	private W3CAnnotationSearchService w3cAnnotationSearchService;
	private AnnotationAutocompleteService annotationAutocompleteService;
	private ControllerUtility controllerUtility;
	private AnnotationUtils annotationUtils;

	@Autowired
	public W3CAnnotationSearchController(W3CAnnotationSearchService w3cAnnotationSearchService,
			AnnotationAutocompleteService annotationAutocompleteService) {
		this.w3cAnnotationSearchService = w3cAnnotationSearchService;
		this.annotationAutocompleteService = annotationAutocompleteService;
		this.controllerUtility = new ControllerUtility();
		this.annotationUtils = this.annotationAutocompleteService.getAnnotationUtils();
	}

	// autocomplete parameter defaults to 1 if not specified
	public static final String PARAM_MIN = "min";

	private static final String W3C_SEARCH_REQUEST_PATH = "/w3c/search";
	private static final String W3C_AUTOCOMPLETE_REQUEST_PATH = "/w3c/autocomplete";

	private static final String WITHIN_W3C_SEARCH_REQUEST_PATH = "/{withinId}/w3c/search";
	private static final String WITHIN_W3C_AUTOCOMPLETE_REQUEST_PATH = "/{withinId}/w3c/autocomplete";

	@CrossOrigin
	@RequestMapping(value = W3C_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> searchGet(
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = false) String query,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
			HttpServletRequest request) {
		String queryString = controllerUtility.createQueryString(request);
		String type = null;

		if (!controllerUtility.validateParameters(query, motivation, date, user)) {
			throw new SearchQueryException(AnnotationSearchConstants.EMPTY_QUERY_MESSAGE);
		}
		ServiceResponse<Map<String, Object>> serviceResponse = w3cAnnotationSearchService
				.getAnnotationPage(new Parameters(query, motivation, date, user), queryString, page, null, type);

		Status serviceResponseStatus = serviceResponse.getStatus();

		if (serviceResponseStatus.equals(Status.OK)) {
			return new ResponseEntity<Map<String, Object>>(serviceResponse.getObj(),
					controllerUtility.getResponseHeaders(), HttpStatus.OK);
		}

		if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
			Map<String, Object> emptyMap = annotationUtils.returnEmptyResultSet(queryString, true, new PageParameters(),
					false);
			return new ResponseEntity<Map<String, Object>>(emptyMap, controllerUtility.getResponseHeaders(),
					HttpStatus.OK);
		}

		throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
	}

	@CrossOrigin
	@RequestMapping(value = WITHIN_W3C_SEARCH_REQUEST_PATH, method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> searchWithinGet(@PathVariable String withinId,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = false) String query,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_PAGE, required = false) String page,
			HttpServletRequest request) {
		String queryString = controllerUtility.createQueryString(request);
		String within = withinId;
		String type = null;

		if (!controllerUtility.validateParameters(query, motivation, date, user)) {
			throw new SearchQueryException(AnnotationSearchConstants.EMPTY_QUERY_MESSAGE);
		}
		ServiceResponse<Map<String, Object>> serviceResponse = w3cAnnotationSearchService
				.getAnnotationPage(new Parameters(query, motivation, date, user), queryString, page, within, type);

		Status serviceResponseStatus = serviceResponse.getStatus();

		if (serviceResponseStatus.equals(Status.OK)) {
			return new ResponseEntity<Map<String, Object>>(serviceResponse.getObj(),
					controllerUtility.getResponseHeaders(), HttpStatus.OK);
		}

		if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
			Map<String, Object> emptyMap = annotationUtils.returnEmptyResultSet(queryString, true, new PageParameters(),
					false);
			return new ResponseEntity<Map<String, Object>>(emptyMap, controllerUtility.getResponseHeaders(),
					HttpStatus.OK);
		}

		throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));
	}

	@CrossOrigin
	@RequestMapping(value = W3C_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> autocompleteGet(
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user,
			@RequestParam(value = PARAM_MIN, required = false) String min, HttpServletRequest request) {

		String queryString = controllerUtility.createQueryString(request);

		ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query, motivation,
				date, user, min, queryString, true, null);

		Status serviceResponseStatus = serviceResponse.getStatus();

		if (serviceResponseStatus.equals(Status.OK)) {
			return new ResponseEntity<Map<String, Object>>(serviceResponse.getObj(),
					controllerUtility.getResponseHeaders(), HttpStatus.OK);
		}

		if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
			Map<String, Object> emptyMap = annotationUtils.returnEmptyAutocompleteResultSet(queryString, motivation,
					date, user);
			return new ResponseEntity<Map<String, Object>>(emptyMap, controllerUtility.getResponseHeaders(),
					HttpStatus.OK);
		}

		throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

	}

	@CrossOrigin
	@RequestMapping(value = WITHIN_W3C_AUTOCOMPLETE_REQUEST_PATH, method = RequestMethod.GET)
	public ResponseEntity<Map<String, Object>> autocompleteWithinGet(@PathVariable String withinId,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_QUERY, required = true) String query,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_MOTIVATION, required = false) String motivation,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_DATE, required = false) String date,
			@RequestParam(value = AnnotationSearchConstants.PARAM_FIELD_USER, required = false) String user,
			@RequestParam(value = PARAM_MIN, required = false) String min, HttpServletRequest request) {

		String queryString = controllerUtility.createQueryString(request);
		String within = withinId;
		ServiceResponse<Map<String, Object>> serviceResponse = annotationAutocompleteService.getTerms(query, motivation,
				date, user, min, queryString, true, within);

		Status serviceResponseStatus = serviceResponse.getStatus();

		if (serviceResponseStatus.equals(Status.OK)) {
			return new ResponseEntity<Map<String, Object>>(serviceResponse.getObj(),
					controllerUtility.getResponseHeaders(), HttpStatus.OK);
		}

		if (serviceResponseStatus.equals(Status.NOT_FOUND)) {
			Map<String, Object> emptyMap = annotationUtils.returnEmptyAutocompleteResultSet(queryString, motivation,
					date, user);
			return new ResponseEntity<Map<String, Object>>(emptyMap, controllerUtility.getResponseHeaders(),
					HttpStatus.OK);

		}

		throw new SearchException(String.format("Unexpected service response status [%s]", serviceResponseStatus));

	}

}
