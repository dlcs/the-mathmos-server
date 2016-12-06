package com.digirati.themathmos.repository;

import java.util.List;

import com.digirati.themathmos.model.annotation.w3c.W3CAnnotation;

public interface AnnotationRepository {

    public List<W3CAnnotation> getAnnotationsByAnnotationIds(String[] annotationIds);
    
}
