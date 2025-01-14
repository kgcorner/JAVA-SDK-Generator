package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.MethodParameter;
import com.scriptchess.utils.Constants;
import com.scriptchess.utils.Utilities;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerApiWriter {
    public List<String> write(Api api) {
        List<String> contents = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        StringBuilder importsBuilder = new StringBuilder();
        StringBuilder methodBodyBuilder = new StringBuilder();
        StringBuilder paramBuilder = new StringBuilder();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/templates/api.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the api.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            if(api.getReturnType() != null)
                data = data.replace("<ReturnType>", Utilities.getClassName(api.getReturnType()));
            else
                data = data.replace("<ReturnType>", Utilities.getClassName(api.getGenericReturnType()));
            data = data.replace("<MethodName>", api.getName());
            if(api.getReturnType() != null) {
                importsBuilder.append("import ").append(api.getReturnType().getCanonicalName()).append(";").append("\n");
            } else {
                if(api.getGenericReturnType() instanceof ParameterizedType parameterizedType) {
                    importsBuilder.append("import ").append(parameterizedType.getRawType().getTypeName()).append(";").append("\n");
                    for(Type type : parameterizedType.getActualTypeArguments()) {
                        importsBuilder.append("import ").append(type.getTypeName()).append(";").append("\n");
                    }

                }
            }
            String param = Utilities.buildParams(api, paramBuilder, importsBuilder);
            data = data.replace("<Params>", param);
            if(api.getReturnType() == null || !Utilities.getClassName(api.getReturnType()).equals("void"))
                methodBodyBuilder.append(Constants.TAB)
                        .append("return service.")
                        .append(api.getName())
                        .append("(");
            else
                methodBodyBuilder.append(Constants.TAB)
                        .append("service.")
                        .append(api.getName())
                        .append("(");
            StringBuilder nakedParamBuilder = new StringBuilder();
            if(api.getRequestParams() != null && !api.getRequestParams().isEmpty()) {
                for(Map.Entry<String, MethodParameter> entry : api.getRequestParams().entrySet()) {
                    nakedParamBuilder.append(entry.getValue().getName()).append(",");
                }
            }

            if(api.getPathVariables() != null && !api.getPathVariables().isEmpty()) {
                for(Map.Entry<String, MethodParameter> entry : api.getPathVariables().entrySet()) {
                    nakedParamBuilder.append(entry.getValue().getName()).append(",");
                }
            }

            if(api.getRequestHeaders() != null && !api.getRequestHeaders().isEmpty()) {
                for(Map.Entry<String, MethodParameter> entry : api.getRequestHeaders().entrySet()) {
                    nakedParamBuilder.append(entry.getValue().getName()).append(",");
                }
            }

            if(api.getRequestBody() != null) {
                nakedParamBuilder.append(api.getRequestBody().getName()).append(",");
            }


            String nakedParams = nakedParamBuilder.toString();
            if(!nakedParams.isEmpty()) {
                nakedParams = nakedParams.substring(0, nakedParams.length() -1);
            }
            methodBodyBuilder.append(nakedParams).append(");").append("\n");
            data = data.replace("<MethodBody>", methodBodyBuilder.toString());
            contents.add(importsBuilder.toString());
            contents.add(data);
        } catch (IOException x) {
            throw new IllegalStateException("Unable to find /templates/client.template");
        }
        return contents;
    }
}
