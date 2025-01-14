package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.Controller;
import com.scriptchess.model.MethodParameter;
import com.scriptchess.temp.*;
import com.scriptchess.utils.Constants;
import com.scriptchess.utils.Utilities;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceApiWriter {
    public List<String> write(Api api, String basePackageName) {

        //write request templates
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/templates/api.template");
        StringBuilder importsBuilder = new StringBuilder();
        StringBuilder methodBodyBuilders = new StringBuilder();
        StringBuilder paramBuilders = new StringBuilder();
        List<String>  content = new ArrayList<>();
        importsBuilder.append("import ").append(basePackageName).append(".request.").append("ResponseTypeProcessor;").append("\n");
        importsBuilder.append("import ").append(basePackageName).append(".request.").append("RequestTypeProcessor;").append("\n");
        importsBuilder.append("import ").append(basePackageName).append(".request.").append("MediaTypeProcessorFactory;").append("\n");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the request.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            if(api.getReturnType() != null)
                data = data.replace("<ReturnType>", Utilities.getClassName(api.getReturnType()));
            else
                data = data.replace("<ReturnType>", Utilities.getClassName(api.getGenericReturnType()));
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
            String param = Utilities.buildParams(api, paramBuilders, importsBuilder);
            data = data.replace("<Params>", param);
            data = data.replace("<MethodName>", api.getName());


            //Write url
            methodBodyBuilders.append(Constants.TAB_TAB).append("String ").append("url = \"").append(api.getPath()[0])
                    .append("\";").append("\n");

            //format url
            if(api.getPathVariables() != null && !api.getPathVariables().isEmpty()) {
                for(Map.Entry<String, MethodParameter> pathEntry : api.getPathVariables().entrySet()) {
                    if(Utilities.isPrimitive(pathEntry.getValue().getParameter().getType())) {
                        methodBodyBuilders.append(Constants.TAB_TAB).append("url = url.replace(\"{")
                                .append(pathEntry.getKey()).append("}\", ")
                                .append(pathEntry.getValue().getName())
                                .append("+ \"\"")
                                .append(");").append("\n");
                    } else {
                        if(Utilities.isString(pathEntry.getValue().getParameter().getType())) {
                            methodBodyBuilders.append(Constants.TAB_TAB).append("url = url.replace(\"{")
                                    .append(pathEntry.getKey()).append("}\", ")
                                    .append(pathEntry.getValue().getName())
                                    .append(");").append("\n");
                        } else {
                            throw new IllegalArgumentException("Custom object for Path variables are not " +
                                    "supported at this moment");
                        }
                    }
                }
            }

            if(api.getRequestParams() != null && !api.getRequestParams().isEmpty()) {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(Constants.TAB_TAB).append("url = url + \"?\";").append(";\n");
                for(Map.Entry<String, MethodParameter> paramEntry : api.getRequestParams().entrySet()) {
                    if(Utilities.isPrimitive(paramEntry.getValue().getParameter().getType())) {
                        urlBuilder.append(Constants.TAB_TAB).append("url = url + \"").append(paramEntry.getKey())
                                .append(" = \"+ ")
                                .append(paramEntry.getValue().getName())
                                .append("+ \"\" + \"&\"")
                                .append(";").append("\n");
                    } else {
                        if(Utilities.isString(paramEntry.getValue().getParameter().getType())) {
                            urlBuilder.append(Constants.TAB_TAB).append("url = url + \"").append(paramEntry.getKey())
                                    .append(" = \" + ")
                                    .append(paramEntry.getValue().getParameter().getName())
                                    .append(" + \"&\"")
                                    .append(";").append("\n");
                        } else {
                            if(api.isMultipart()) {
                                //TODO: implement Multipart implementation
                            } else {
                                throw new IllegalArgumentException("Custom object for Query params are not " +
                                        "supported at this moment");
                            }
                        }
                    }
                }
                String urlPart = urlBuilder.toString();
                if(urlPart.endsWith("&\";\n")) {
                    urlPart = urlPart.substring(0, urlPart.length() - "+ \"&\"\n".length() -1);
                    methodBodyBuilders.append(urlPart).append(";").append("\n");
                } else {
                    methodBodyBuilders.append(urlPart).append("\n");
                }


            }
            importsBuilder.append("import ").append(basePackageName).append(".config").append(".").append("SDKConfig;").append("\n");
            importsBuilder.append("import ").append("okhttp3.MediaType;").append("\n");
            importsBuilder.append("import ").append("java.nio.charset.StandardCharsets;").append("\n");
            //Append host name
            methodBodyBuilders.append(Constants.TAB_TAB)
                    .append("if(SDKConfig.getHostName() == null || SDKConfig.getHostName().isEmpty())")
                    .append("\n")
                    .append(Constants.TAB_TAB_TAB)
                    .append("throw new IllegalStateException(\"Host name is not set\");").append("\n");

            methodBodyBuilders.append(Constants.TAB_TAB)
                    .append("url = SDKConfig.getHostName() + url;").append("\n");

            //Create header Map
            boolean headerMapExists = false;
            if(api.getRequestHeaders() != null && !api.getRequestHeaders().isEmpty()) {
                headerMapExists = true;
                importsBuilder.append("import java.util.Map;").append("\n");;
                importsBuilder.append("import java.util.HashMap;").append("\n");;
                methodBodyBuilders.append(Constants.TAB_TAB)
                        .append("Map<String, String> headerMap = new HashMap<String, String>();").append("\n");;
                for(Map.Entry<String, MethodParameter> parameterEntry : api.getRequestHeaders().entrySet()) {
                    methodBodyBuilders.append(Constants.TAB_TAB)
                            .append("headerMap.put(\"")
                            .append(parameterEntry.getKey())
                            .append("\", ")
                            .append(parameterEntry.getValue().getParameter().getName())
                            .append(");").append("\n");;
                }

            }
            importsBuilder.append("\n").append("import ").append(basePackageName).append(".request.Requests;").append("\n");
            if(headerMapExists) {
                switch (api.getMethod()) {
                    case GET -> {
                        methodBodyBuilders.append(Constants.TAB_TAB).append("String response = Requests.doGet(url, headerMap);")
                                .append("\n");
                    }
                    case POST -> {
                        if(api.getConsumes().length > 0) {
                            methodBodyBuilders.append(Constants.TAB_TAB).append("RequestTypeProcessor requestTypeProcessor = null;").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB).append("String[] consumers = ").append("new String[]{");
                            methodBodyBuilders.append(Arrays.toString(api.getConsumes())).append("};").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB).append("if(consumers.length > 0) {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("for(String consumer : consumers) {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(consumer);")
                                    .append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("if(requestTypeProcessor != null)").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB).append(Constants.TAB)
                                    .append("break;").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB).append("}").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("else {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(\"application/json\");")
                                    .append("\n");
                        } else {
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB)
                                    .append("RequestTypeProcessor requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(\"application/json\");")
                                    .append("\n");
                        }

                        methodBodyBuilders.append(Constants.TAB_TAB).append("if(consumers.length > 0) {").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("for(String consumer : consumers) {").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                .append("requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(consumer);")
                                .append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                .append("if(requestTypeProcessor != null)").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB).append(Constants.TAB)
                                .append("break;").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB).append("}").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("else {").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                .append("requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(\"application/json\");")
                                .append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("if(requestTypeProcessor == null) {").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                .append("throw new IllegalArgumentException(\"Unable to process the request body for consumer type : \" \n" +
                                        "+ Arrays.toString(consumers));").append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                    }
                    case PUT -> {
                    }
                    case PATCH -> {
                    }
                    case DELETE -> {

                    }
                }
                methodBodyBuilders.append(Constants.TAB_TAB).append("String response = Requests.doGet(url, headerMap);")
                        .append("\n");
            } else {
                importsBuilder.append("\n").append("import java.util.*;").append("\n");
                switch (api.getMethod()) {
                    case GET -> {
                        methodBodyBuilders.append(Constants.TAB_TAB).append("String response = Requests.doGet(url, Collections.EMPTY_MAP);")
                                .append("\n");

                    }
                    case POST -> {
                        if(api.getConsumes().length > 0) {
                            methodBodyBuilders.append(Constants.TAB_TAB).append("RequestTypeProcessor requestTypeProcessor = null;").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB).append("String[] consumers = ").append("new String[]{");
                            methodBodyBuilders.append(Arrays.toString(api.getConsumes())).append("};").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB).append("if(consumers.length > 0) {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("for(String consumer : consumers) {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(consumer);")
                                    .append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("if(requestTypeProcessor != null)").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB).append(Constants.TAB)
                                    .append("break;").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB).append("}").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("else {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(\"application/json\");")
                                    .append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("if(requestTypeProcessor == null) {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("throw new IllegalArgumentException(\"Unable to process the request body for consumer type : \" \n" +
                                            "+ Arrays.toString(consumers));").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                        } else {
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB)
                                    .append("RequestTypeProcessor requestTypeProcessor = MediaTypeProcessorFactory.getRequestTypeProcessor(\"application/json\");")
                                    .append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("if(requestTypeProcessor == null) {").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append(Constants.TAB)
                                    .append("throw new IllegalArgumentException(\"Unable to process the request body for consumer type : \" + \n" +
                                            "\"application/json\");").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB_TAB).append("}").append("\n");
                            methodBodyBuilders.append(Constants.TAB_TAB).append("String response = Requests.doPost(url, Collections.emptyMap(), requestTypeProcessor.convert(");
                            if(api.getRequestBody() != null)
                                methodBodyBuilders.append(api.getRequestBody().getName()).append(")")
                                        .append(".getBytes(StandardCharsets.UTF_8)").append(",");
                            methodBodyBuilders.append("MediaType.parse(\"application/json\")").append(");");
                        }

                    }
                    case PUT -> {
                    }
                    case PATCH -> {
                    }
                    case DELETE -> {

                    }
                }
            }
            if(api.getReturnType() != null  && !Utilities.getClassName(api.getReturnType()).equals("void")) {
                //Add Media Processor Entry
                if(api.getProduces() != null && api.getProduces().length > 0 && api.getProduces()[0] != null && !api.getProduces()[0].isEmpty()) {
                    methodBodyBuilders.append(Constants.TAB_TAB)
                            .append("ResponseTypeProcessor responseTypeProducer = ")
                            .append("MediaTypeProcessorFactory.getResponseTypeProcessor(\"")
                            .append(api.getProduces()[0])
                            .append("\");").append("\n");;
                    methodBodyBuilders.append(Constants.TAB_TAB)
                            .append("if(responseTypeProducer == null)").append("\n")
                            .append(Constants.TAB_TAB_TAB)
                            .append("throw new IllegalStateException(\"No Media Type processor found for")
                            .append(api.getProduces()[0])
                            .append("\");")
                            .append("\n");
                    methodBodyBuilders.append(Constants.TAB_TAB)
                            .append("return responseTypeProducer.convert(response, ")
                            .append(Utilities.getClassName(api.getReturnType()))
                            .append(".class);")
                            .append("\n");
                } else {
                    methodBodyBuilders.append(Constants.TAB_TAB)
                            .append("ResponseTypeProcessor responseTypeProducer = ")
                            .append("MediaTypeProcessorFactory.getResponseTypeProcessor(\"application/json\");").append("\n");;
                    if(api.getReturnType() != null) {
                        methodBodyBuilders.append(Constants.TAB_TAB)
                                .append("return responseTypeProducer.convert(response, ")
                                .append(Utilities.getClassName(api.getReturnType()))
                                .append(".class);")
                                .append("\n");
                    } else {
                        methodBodyBuilders.append(Constants.TAB_TAB)
                                .append("return responseTypeProducer.convert(response, ")
                                .append(Utilities.getClassName(api.getGenericReturnType()))
                                .append(".class);")
                                .append("\n");
                    }

                }
            } else {
                if(api.getGenericReturnType() != null) {
                    if(api.getProduces() != null && api.getProduces().length > 0 && api.getProduces()[0] != null && !api.getProduces()[0].isEmpty()) {
                        methodBodyBuilders.append(Constants.TAB_TAB)
                                .append("ResponseTypeProcessor responseTypeProducer = ")
                                .append("MediaTypeProcessorFactory.getResponseTypeProcessor(\"")
                                .append(api.getProduces()[0])
                                .append("\");").append("\n");;
                        methodBodyBuilders.append(Constants.TAB_TAB)
                                .append("if(responseTypeProducer == null)").append("\n")
                                .append(Constants.TAB_TAB_TAB)
                                .append("throw new IllegalStateException(\"No Media Type processor found for")
                                .append(api.getProduces()[0])
                                .append("\");")
                                .append("\n");
                        methodBodyBuilders.append(Constants.TAB_TAB)
                                .append("return responseTypeProducer.convert(response, ")
                                .append(Utilities.getClassName(api.getReturnType()))
                                .append(".class);")
                                .append("\n");
                    } else {
                        methodBodyBuilders.append(Constants.TAB_TAB)
                                .append("ResponseTypeProcessor responseTypeProducer = ")
                                .append("MediaTypeProcessorFactory.getResponseTypeProcessor(\"application/json\");").append("\n");;
                        if(api.getReturnType() != null) {
                            methodBodyBuilders.append(Constants.TAB_TAB)
                                    .append("return responseTypeProducer.convert(response, ")
                                    .append(Utilities.getClassName(api.getReturnType()))
                                    .append(".class);")
                                    .append("\n");
                        } else {
                            Type rawType = api.getGenericParamTypeRawType();
                            if(rawType.getTypeName().equals("java.util.List")) {
                                methodBodyBuilders.append(Constants.TAB_TAB)
                                        .append("return responseTypeProducer.convertFromList(response, ")
                                        .append(Utilities.getClassName(api.getGenericParamTypeArgs().get(0)))
                                        .append(".class);")
                                        .append("\n");
                            } else {
                                //TODO: Implement Map
                            }

                        }

                    }
                }
            }

            data = data.replace("<MethodBody>", methodBodyBuilders.toString());
            content.add(importsBuilder.toString());
            content.add(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return content;
    }



    private RequestTypeProcessor getRequestProcessor(Api api) {
        RequestTypeProcessor requestTypeProcessor = null;
        if(api.getConsumes() != null && api.getConsumes().length > 0) {
            ServiceLoader<RequestTypeProcessor> requestTypeProcessors = ServiceLoader.load(RequestTypeProcessor.class);
            for(RequestTypeProcessor processor : requestTypeProcessors) {
                for (int i = 0; i < api.getConsumes().length; i++) {
                    if(processor.supports(api.getConsumes()[i])) {
                        requestTypeProcessor = processor;
                        break;
                    }
                }
                if(requestTypeProcessor != null)
                    break;
            }

            if(requestTypeProcessor == null)
                throw new IllegalStateException("No processor found for consumer type" + Arrays.toString(api.getConsumes()));

        }

        if(api.getMethod() == Constants.HTTP_METHODS.GET) {
            requestTypeProcessor = new JSONRequestProcessor();
        }

        if(api.getMethod() == Constants.HTTP_METHODS.POST || api.getMethod() == Constants.HTTP_METHODS.PUT
                || api.getMethod() == Constants.HTTP_METHODS.PATCH) {
            if(api.getRequestBody() != null) {
                if(api.getRequestBody().getClass().isAssignableFrom(Multipart.class)) {
                    requestTypeProcessor = new MultipartRequestTypeProcessor();
                } else {
                    if(hasStreamContent(api.getRequestBody().getClass())) {
                        throw new IllegalStateException("Not supported yet");
                    } else {
                        requestTypeProcessor = new JSONRequestProcessor();
                    }
                }
            }
        }
        return requestTypeProcessor;
    }

    private boolean hasStreamContent(Class<? extends MethodParameter> aClass) {
        Field[] fields = aClass.getFields();
        for(Field field : fields) {
            if(field.getType().isAssignableFrom(InputStream.class) || field.getType().isAssignableFrom(File.class))
                return true;
        }
        return false;
    }
}
