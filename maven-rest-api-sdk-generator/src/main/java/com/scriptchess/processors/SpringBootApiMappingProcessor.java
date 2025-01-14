package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.utils.Constants;
import okhttp3.internal.http.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;

public class SpringBootApiMappingProcessor implements ApiProcessor {

    @Override
    public Api process(Annotation annotation, Api api) {
        if(api == null)
            throw new IllegalArgumentException("Api can't be null");
        switch (annotation.annotationType().getCanonicalName()) {
            case Constants.GET_MAPPING -> api.setMethod(Constants.HTTP_METHODS.GET);
            case Constants.POST_MAPPING -> api.setMethod(Constants.HTTP_METHODS.POST);
            case Constants.PUT_MAPPING -> api.setMethod(Constants.HTTP_METHODS.PUT);
            case Constants.PATCH_MAPPING -> api.setMethod(Constants.HTTP_METHODS.PATCH);
            case Constants.DELETE_MAPPING -> api.setMethod(Constants.HTTP_METHODS.DELETE);
        }
        Class<? extends Annotation> aClass = annotation.annotationType();
        Method[] methods = aClass.getMethods();
        for(Method method : methods) {
            try {
                if(method.getParameters().length > 0)
                    continue;
                Object value = method.invoke(annotation,(Object[]) null);
                if(value == null)
                    continue;
                switch (method.getName()) {
                    case "value": {
                        String[] value1 = (String[]) value;
                        if(value1.length == 0) {
                            value1 = new String[]{""};
                        }
                        api.setPath(value1);
                    }

                        break;
                    case "consumes":
                        api.setConsumes((String[]) value);
                        break;
                    case "produces":
                        api.setProduces((String[]) value);
                        break;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return api;
    }

    @Override
    public boolean supports(Annotation annotation) {
        return annotation.annotationType().getCanonicalName().equals(Constants.GET_MAPPING) ||
                annotation.annotationType().getCanonicalName().equals(Constants.PUT_MAPPING) ||
                annotation.annotationType().getCanonicalName().equals(Constants.POST_MAPPING) ||
                annotation.annotationType().getCanonicalName().equals(Constants.DELETE_MAPPING) ||
                annotation.annotationType().getCanonicalName().equals(Constants.PATCH_MAPPING);
    }

}
