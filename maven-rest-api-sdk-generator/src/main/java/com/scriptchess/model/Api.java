package com.scriptchess.model;

import com.scriptchess.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Api {
    private String name;
    private String[] path;
    private String[] produces;
    private String[] consumes;
    private String[] headers;
    private Map<String, MethodParameter> requestParams;
    private Map<String, MethodParameter> pathVariables;
    private Map<String, MethodParameter> requestHeaders;
    private MethodParameter requestBody;
    private Controller controller;
    private Class returnType;
    private Constants.HTTP_METHODS method;
    private Type genericReturnType;
    private boolean multipart;
    public List<String> getGenericParamTypeArgs() {
        List<String> types= new ArrayList<>();
        if(genericReturnType != null) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                types.add(actualTypeArgument.getTypeName());
            }
        }
        return types;
    }

    public Type getGenericParamTypeRawType() {
        if(genericReturnType != null) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            return parameterizedType.getRawType();
        }
        return null;
    }
}
