package com.scriptchess.processors;

import com.scriptchess.model.Controller;
import com.scriptchess.utils.Constants;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpringBootRestControllerProcessor implements ControllerProcessor {

    @Override
    public Controller process(Annotation annotation) {
        Controller controller = new Controller();
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        Method[] methods = annotationClass.getMethods();
        for(Method method : methods) {
            try {
                if(method.getParameters().length > 0)
                    continue;
                Object value = method.invoke(annotation, (Object[])null);
                switch (method.getName()) {
                    case "path":
                    case "value":
                        controller.setPath(value.toString());
                        break;
                    case "produce":
                        controller.setProduces((String[])value);
                        break;
                    case "consume":
                        controller.setConsumes((String[])value);
                        break;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return controller;

    }

    @Override
    public boolean supports(Annotation annotation) {
        return annotation.annotationType().getCanonicalName().equals(Constants.REST_CONTROLLER);
    }
}
