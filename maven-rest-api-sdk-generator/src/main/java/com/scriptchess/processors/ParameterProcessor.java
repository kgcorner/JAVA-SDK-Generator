package com.scriptchess.processors;

import com.scriptchess.model.Api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public interface ParameterProcessor extends AnnotationProcessor {
    Api process(Annotation annotation, Parameter parameter, Api api);
}
