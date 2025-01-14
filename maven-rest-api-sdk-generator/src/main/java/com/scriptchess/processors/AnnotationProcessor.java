package com.scriptchess.processors;

import java.lang.annotation.Annotation;

public interface AnnotationProcessor {
    boolean supports(Annotation annotation);
}
