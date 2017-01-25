package com.digirati.themathmos.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.DefaultResultMapper;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/** If we are in the TextSearchServiceImpl formQuery method and not using
 * searchRequestBuilder.setFetchSource(false);
 * then remove all of below.
 */
public class TextSearchAnnotationMapper extends DefaultResultMapper {

    
    private final static Logger LOG = Logger.getLogger(TextSearchAnnotationMapper.class);
    

    @Override
    public <T> Page<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
	long totalHits = response.getHits().totalHits();
	List<T> results = new ArrayList<>();
	for (SearchHit hit : response.getHits()) {
	    if (hit != null) {
		T result;
		if (StringUtils.isNotBlank(hit.sourceAsString())) {
		    result = mapEntity(hit.sourceAsString(), clazz);
		} else {
		    result = mapEntity(hit.getFields().values(), clazz);
		}
		setPersistentEntityId(result, hit.getId(), clazz);

		results.add(result);
	    }
	}
	return new PageImpl<>(results, pageable, totalHits);
    }

    private <T> T mapEntity(Collection<SearchHitField> values, Class<T> clazz) {
	return mapEntity(buildJSONFromFields(values), clazz);
    }

    private String buildJSONFromFields(Collection<SearchHitField> values) {
	JsonFactory nodeFactory = new JsonFactory();
	try {
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
	    generator.writeStartObject();
	    for (SearchHitField value : values) {
		if (value.getValues().size() > 1) {
		    generator.writeArrayFieldStart(value.getName());
		    for (Object val : value.getValues()) {
			generator.writeObject(val);
		    }
		    generator.writeEndArray();
		} else {
		    generator.writeObjectField(value.getName(), value.getValue());
		}
	    }
	    generator.writeEndObject();
	    generator.flush();
	    return new String(stream.toByteArray(), Charset.forName("UTF-8"));
	} catch (IOException e) {
	    LOG.error("IOException in buildJSONFromFields ", e);
	    return null;
	}
    }

    /*
     * vastly changed method from the one in defaultResultMapper. Mappingcontext
     *was always null do we were never getting the _id field contents when we has no _source in the results
     */
    private <T> void setPersistentEntityId(T result, String id, Class<T> clazz) {
	if (clazz.isAnnotationPresent(Document.class)) {
	    Method setter;
	    try {
		setter = clazz.getDeclaredMethod("setId", String.class);
		if (setter != null) {
		    try {
			setter.invoke(result, id);
		    } catch (Exception t) {
			LOG.error("Exception in setPersistentEntityId trying to invoke setter", t);
		    }
		}
	    } catch (NoSuchMethodException e) {
		LOG.error("NoSuchMethodException in setPersistentEntityId ", e);
	    } catch (SecurityException e) {
		LOG.error("SecurityException in setPersistentEntityId ", e);
	    }

	}
    }

}
