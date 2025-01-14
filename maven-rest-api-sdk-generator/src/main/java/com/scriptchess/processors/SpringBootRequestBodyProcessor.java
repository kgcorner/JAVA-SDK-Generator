package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.MethodParameter;
import com.scriptchess.utils.Constants;
import com.scriptchess.utils.Utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

public class SpringBootRequestBodyProcessor implements ParameterProcessor {

    @Override
    public Api process(Annotation annotation, Parameter parameter, Api api) {
        if(api == null)
            throw new IllegalArgumentException("Api can't be null");
        if(parameter.getName().contains("arg")) {
            if(parameter.getType().getCanonicalName().equals("org.springframework.web.multipart.MultipartFile")) {
                api.setMultipart(true);
                api.setRequestBody(new MethodParameter("multipart", true, parameter));
            } else {
                api.setRequestBody(new MethodParameter(Utilities.camelCase(Utilities.getClassName(parameter.getType())),
                        false, parameter));
            }
        } else {
            if(parameter.getType().getCanonicalName().equals("org.springframework.web.multipart.MultipartFile")) {
                api.setMultipart(true);
                api.setRequestBody(new MethodParameter(parameter.getName(), true, parameter));
            } else {
                api.setRequestBody(new MethodParameter(parameter.getName(),
                        false, parameter));
            }
        }

        return api;
    }

    @Override
    public boolean supports(Annotation annotation) {
        return annotation.annotationType().getCanonicalName().equals(Constants.REQUEST_BODY);
    }
}
