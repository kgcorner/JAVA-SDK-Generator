package com.scriptchess.processors;

import com.scriptchess.model.Controller;

import java.lang.annotation.Annotation;

public interface ControllerProcessor extends AnnotationProcessor {
    Controller process(Annotation annotation);
}
