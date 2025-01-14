package com.scriptchess.utils;

import com.scriptchess.model.Api;
import com.scriptchess.model.MethodParameter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

public class Utilities {

    public static String getClassName(Class<?> classObject) {
        return classObject.getName().replace(classObject.getPackageName()+".","");
    }

    public static String getClassName(String classObject) {
        String[] parts = classObject.split(Pattern.quote("."));
        if(parts.length > 0)
            return parts[parts.length -1];
        return classObject;
    }

    public static String camelCase(String name) {
        name = name.replace("<","").replace(">","").replace(",","");
        if(name.contains("-")) {
            String[] parts = name.split("-");
            StringBuilder sb = new StringBuilder();

            for(String part : parts) {
                sb.append((part.charAt(0)+"").toUpperCase()).append(part.substring(1));
            }
            name = sb.toString();
        }

        name = (name.charAt(0)+"").toLowerCase() + name.substring(1);
        return name;
    }

    public static String getClassName(Type type) {

        StringBuilder className = new StringBuilder();
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            className = new StringBuilder(((Class) parameterizedType.getRawType()).getName()
                    .replace(((Class) parameterizedType.getRawType()).getPackageName() + ".", ""));
            className.append("<");
            for(Type argType : parameterizedType.getActualTypeArguments()) {
                if(argType instanceof ParameterizedType) {
                    className.append(getClassName(argType));
                } else {
                    className.append(((Class) argType).getName()
                            .replace(((Class) argType).getPackageName() + ".", "")).append(",");
                }

            }
            if(className.charAt(className.length() -1) == ',')
                className = new StringBuilder(className.substring(0, className.length() - 1));
            className.append(">");
        } else {
            className = new StringBuilder(getClassName(type.getClass()));
        }
        return className.toString();

    }

    public static boolean isPrimitive(Class<?> classObject) {
        return classObject.getName().equals("int") ||
                classObject.getName().equals("long") ||
                classObject.getName().equals("float") ||
                classObject.getName().equals("double") ||
                classObject.getName().equals("short") ||
                classObject.getName().equals("char") ||
                classObject.getName().equals("byte") ||
                classObject.getName().equals("boolean") ||
                classObject.getName().equals("void");
    }

    public static boolean isString(Class<?> classObject) {
        return classObject.getName().equals("java.lang.String");
    }

    public static String buildParams(Api api, StringBuilder paramBuilder, StringBuilder importsBuilder) {
        String packageName = ProjectConfig.getGroupId();
        if(api.getPathVariables() != null && !api.getPathVariables().isEmpty()) {
            for(Map.Entry<String, MethodParameter> parameterEntry : api.getPathVariables().entrySet()) {
                paramBuilder.append(Utilities.getClassName(parameterEntry.getValue().getParameter().getType()))
                        .append(" ").append(parameterEntry.getValue().getName()).append(",");
                importsBuilder.append("import ").append(parameterEntry.getValue().getParameter().getType().getCanonicalName()).append(";").append("\n");
            }
        }

        if(api.getRequestParams()!= null && !api.getRequestParams().isEmpty()) {

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

        if(api.getRequestHeaders()!= null && !api.getRequestHeaders().isEmpty()) {
            for(Map.Entry<String, MethodParameter> parameterEntry : api.getRequestHeaders().entrySet()) {
                paramBuilder.append(Utilities.getClassName(parameterEntry.getValue().getParameter().getType()))
                        .append(" ").append(parameterEntry.getValue().getName()).append(",");
                importsBuilder.append("import").append(parameterEntry.getValue().getParameter().getType().getCanonicalName()).append(";").append("\n");
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

        String param = paramBuilder.toString();
        if(!param.isEmpty())
            param = param.substring(0, param.length() -1);
        return param;
    }


}
