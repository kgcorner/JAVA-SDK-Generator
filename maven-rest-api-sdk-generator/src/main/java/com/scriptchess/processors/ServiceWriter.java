package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.Controller;
import com.scriptchess.model.MethodParameter;
import com.scriptchess.utils.Constants;
import com.scriptchess.utils.Utilities;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceWriter {
    private static final String API_DEFINITION_TEMPLATE = "public <ReturnType> <ApiName>(<Params>);";
    public Map<String, String> write(Controller controller, String packageName) {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/templates/service.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the service.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            packageName = packageName + ".service";
            data = data.replace("<PackageName>", packageName);
            String imports = "";
            StringBuilder importsBuilder = new StringBuilder();
            List<Api> apis = controller.getApis();
            List<String> definitions = new ArrayList<>();
            for(Api api : apis) {
                String params = "";
                StringBuilder paramBuilder = new StringBuilder();
                if(api.getRequestHeaders() != null && !api.getRequestHeaders().isEmpty()) {
                    for(Map.Entry<String, MethodParameter> parameterEntry : api.getRequestHeaders().entrySet()) {
                        MethodParameter methodParameter = parameterEntry.getValue();
                        importsBuilder.append("import ").append(methodParameter.getParameter().getType()
                                .getCanonicalName()).append(";");
                        importsBuilder.append("\n");
                        paramBuilder.append(Utilities.getClassName(methodParameter.getParameter().getType()))
                                .append(methodParameter.getName()).append(",");
                    }
                }

                if(api.getRequestParams() != null && !api.getRequestParams().isEmpty()) {
                    for(Map.Entry<String, MethodParameter> parameterEntry : api.getRequestParams().entrySet()) {
                        MethodParameter methodParameter = parameterEntry.getValue();

                        if(methodParameter.isMultipartParam()) {
                            importsBuilder.append("import ").append(packageName).append(".request.Multipart").append(";");
                            importsBuilder.append("\n");
                            paramBuilder.append("Multipart")
                                    .append(" ").append(methodParameter.getName()).append(",");
                        } else {
                            importsBuilder.append("import ").append(methodParameter.getParameter().getType()
                                    .getCanonicalName()).append(";");
                            importsBuilder.append("\n");
                            paramBuilder.append(Utilities.getClassName(methodParameter.getParameter().getType()))
                                    .append(" ").append(methodParameter.getName()).append(",");
                        }

                    }
                }

                if(api.getPathVariables() != null && !api.getPathVariables().isEmpty()) {
                    for(Map.Entry<String, MethodParameter> parameterEntry : api.getPathVariables().entrySet()) {
                        MethodParameter methodParameter = parameterEntry.getValue();
                        importsBuilder.append("import ").append(methodParameter.getParameter().getType()
                                .getCanonicalName()).append(";");
                        importsBuilder.append("\n");
                        paramBuilder.append(Utilities.getClassName(methodParameter.getParameter().getType()))
                                .append(" ").append(methodParameter.getName()).append(",");
                    }
                }
                if(api.getRequestBody() != null) {
                    if(api.getRequestBody().isMultipartParam()) {
                        importsBuilder.append("import ").append(packageName).append(".request.Multipart").append(";");
                        importsBuilder.append("\n");
                        paramBuilder.append("Multipart")
                                .append(" ").append(api.getRequestBody().getName()).append(",");
                    } else {
                        importsBuilder.append("import ").append(api.getRequestBody().getParameter().getType()
                                .getCanonicalName()).append(";");
                        importsBuilder.append("\n");
                        paramBuilder.append(Utilities.getClassName(api.getRequestBody().getParameter().getType()))
                                .append(" ").append(api.getRequestBody().getName()).append(",");
                    }
                }

                params = paramBuilder.toString();
                if(!params.isEmpty()) {
                    //remove trailing comma
                    params = params.substring(0, params.length() -1);
                }
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

                if(api.getReturnType() !=null) {
                    definitions.add(API_DEFINITION_TEMPLATE.replace("<ReturnType>", Utilities.getClassName(api.getReturnType()))
                            .replace("<ApiName>", api.getName())
                            .replace("<Params>", params)
                    );
                } else {
                    definitions.add(API_DEFINITION_TEMPLATE.replace("<ReturnType>", Utilities.getClassName(api.getGenericReturnType()))
                            .replace("<ApiName>", api.getName())
                            .replace("<Params>", params)
                    );
                }

            }
            StringBuilder definitionBuilder = new StringBuilder();
            for(String def : definitions) {
                definitionBuilder.append(Constants.TAB).append(def).append("\n").append("\n").append("\n");
            }
            imports = importsBuilder.toString();
            imports = imports.replace("import int;","")
                    .replace("import java.lang.String;","")
                    .replace("import double;","")
                    .replace("import byte;","")
                    .replace("import float;","")
                    .replace("import short;","")
                    .replace("import long;","")
                    .replace("import boolean;","")
                    .replace("import char;","")
                    .replace("import void;","");
            Set<String> importsSet = new HashSet<>(Arrays.asList(imports.split("\n")));
            StringBuilder sb =new StringBuilder();
            for(String imp : importsSet) {
                sb.append(imp).append("\n");
            }
            imports = sb.toString();
            data = data.replace("<package>", packageName);
            data = data.replace("<imports>", imports);
            data = data.replace("<Controller>", controller.getName());
            data = data.replace("<Controller>", controller.getName());
            data = data.replace("<Apis definition>", definitionBuilder.toString());
            Map<String, String> content = new HashMap<>();
            content.put(packageName + "." + controller.getName() + "Service", data);
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
