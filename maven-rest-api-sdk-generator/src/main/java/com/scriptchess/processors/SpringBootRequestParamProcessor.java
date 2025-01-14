package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.MethodParameter;
import com.scriptchess.utils.Constants;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;

public class SpringBootRequestParamProcessor implements ParameterProcessor {

    @Override
    public Api process(Annotation annotation, Parameter parameter, Api api) {
        if(api == null)
            throw new IllegalArgumentException("Api can't be null");
        Class<? extends Annotation> aClass = annotation.annotationType();
        Method[] methods = aClass.getMethods();
        for(Method method : methods) {
            try {
                if(method.getParameters().length > 0)
                    continue;
                if(method.getName().equals("value")) {
                    Object value = method.invoke(annotation,(Object[]) null);
                    if(value == null)
                        continue;
                    if(api.getRequestParams() == null) {
                        api.setRequestParams(new HashMap<>());
                    }
                    if(parameter.getType().getCanonicalName().equals("org.springframework.web.multipart.MultipartFile")) {
                        api.setMultipart(true);
                        if(parameter.getName().contains("arg"))
                            api.getRequestParams().put(value.toString(), new MethodParameter(value.toString(), true, parameter));
                        else
                            api.getRequestParams().put(value.toString(), new MethodParameter(parameter.getName(), true, parameter));
                    } else {
                        if(parameter.getName().contains("arg"))
                            api.getRequestParams().put(value.toString(), new MethodParameter(value.toString(), false, parameter));
                        else
                            api.getRequestParams().put(value.toString(), new MethodParameter(parameter.getName(), false, parameter));
                    }
                }


            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return api;
    }

    @Override
    public boolean supports(Annotation annotation) {
        return annotation.annotationType().getCanonicalName().equals(Constants.REQUEST_PARAM);
    }

}
