package com.scriptchess.processors;

import com.scriptchess.model.Api;

import java.lang.annotation.Annotation;

public interface ApiProcessor extends AnnotationProcessor {
    Api process(Annotation annotation, Api api);
}
