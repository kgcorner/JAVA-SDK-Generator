package com.scriptchess.mojo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scriptchess.model.Api;
import com.scriptchess.model.Controller;
import com.scriptchess.processors.*;
import com.scriptchess.utils.*;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class ApiMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    private ServiceLoader<ControllerProcessor> controllerProcessors;
    private ServiceLoader<ParameterProcessor> parameterProcessors;
    private ServiceLoader<ApiProcessor> apiProcessors;
    private Set<String> mappingClassesSet;
    private URLClassLoader urlClassLoader;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ProjectConfig.setGroupId(project.getGroupId());
        this.createMappingClassSet();
        this.setApiProcessors();
        this.setControllerProcessors();
        this.setParameterProcessors();
        PluginDescriptor pluginDescriptor = (PluginDescriptor) this.getPluginContext().get("pluginDescriptor");
        List<Dependency> dependencies = pluginDescriptor.getPlugin().getDependencies();
        List<Artifact> artifacts = pluginDescriptor.getArtifacts();
        Set<String> dependentArtifacts = new HashSet<>();
        List<File> dependencyFiles = new ArrayList<>();
        for(Dependency dependency : dependencies) {
            dependentArtifacts.add(dependency.getGroupId() + ":" + dependency.getArtifactId() + ":"
                    + dependency.getVersion());
        }

        for(Artifact artifact : artifacts) {
            String key = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
            if(dependentArtifacts.contains(key)) {
                dependencyFiles.add(artifact.getFile());
            }
        }
        List<Class> controllerClasses = new ArrayList<>();
        for(File file : dependencyFiles) {
            List<String> classes = findClassNames(file);
            controllerClasses.addAll(findResControllers(classes));
        }
        List<Controller> controllers = new ArrayList<>();
        for(Class controllerClass : controllerClasses) {
            Controller controller = createController(controllerClass);
            getLog().info("Processing API: " + controller.getName());
            if(controller != null) {
                List<Api> apis = createApis(controllerClass);
                controller.setApis(apis);
                controllers.add(controller);
                getLog().info("Found " + apis.size() +" Apis in controller " + controller.getName());
            }

        }
        getLog().info("********************************************");
        getLog().info("********************************************");
        getLog().info("********************************************");
        getLog().info("********************************************");
        GsonBuilder gsonBuilder = new GsonBuilder();
        //gsonBuilder.registerTypeAdapterFactory(new ClassTypeAdapterFactory());
        gsonBuilder.registerTypeAdapter(Class.class, new ClassTypeAdapter());

        //gsonBuilder.registerTypeAdapterFactory(new ParametrizedTypeAdapterFactory());
        gsonBuilder.registerTypeAdapter(Type.class, new ParameterizedTypeAdapter());
        Gson gson = gsonBuilder.create();
        //String json = gson.toJson(controllers);
        //System.out.println(json);
        getLog().info("********************************************");
        getLog().info("********************************************");
        getLog().info("********************************************");
        getLog().info("********************************************");

        Map<String, String> fileContentMap = new HashMap<>();

        for(Controller controller : controllers) {
            //Generate ServiceLayer
            Map<String, String> contentMap = new ServiceWriter().write(controller, project.getGroupId());
            if(contentMap != null && !contentMap.isEmpty()) {
                fileContentMap.putAll(contentMap);
            }
            contentMap = new ControllerWriter().writeController(controller, project.getGroupId());
            if(contentMap != null && !contentMap.isEmpty()) {
                fileContentMap.putAll(contentMap);
            }
            contentMap = new ServiceImplementationWriter().writeController(controller, project.getGroupId());
            if(contentMap != null && !contentMap.isEmpty()) {
                fileContentMap.putAll(contentMap);
            }
        }

        FileWriter.writeFiles(fileContentMap);
        writeSimpleFile("request","Requests","/templates/request.template");
        writeSimpleFile("request","RequestClientFactory","/templates/request-factory.template");
        writeSimpleFile("config","SDKConfig","/templates/sdk-config.template");
        writeSimpleFile("request","MediaTypeProcessorFactory","/templates/media-type-processor-factory.template");
        writeSimpleFile("request","JSONRequestProcessor","/templates/json-request-processor.template");
        writeSimpleFile("request","JSONResponseProcessor","/templates/json-response-processor.template");
        writeSimpleFile("request","RequestTypeProcessor","/templates/request-type-processor.template");
        writeSimpleFile("request","ResponseTypeProcessor","/templates/response-type-processor.template");
        writeSimpleFile("request","Multipart","/templates/multipart.template");
        writeSimpleFile("request","MediaTypeFactory","/templates/media-type-factory.template");
    }

    private List<Api> createApis(Class controllerClass) {
        List<Method> apiMethods = new ArrayList<>();
        List<Api> apis = new ArrayList<>();
        Method[] methods = controllerClass.getMethods();
        for(Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                if(this.mappingClassesSet.contains(annotation.annotationType().getCanonicalName())) {
                    apiMethods.add(method);
                }
            }
        }

        for(Method method : apiMethods) {
            Api api = new Api();
            if(method.getGenericReturnType() instanceof Class<?>)
                api.setReturnType(method.getReturnType());
            else {
                api.setGenericReturnType(method.getGenericReturnType());
            }
            api.setName(method.getName());
            //process api Level annotation
            for (Annotation annotation : method.getAnnotations()) {
                for(ApiProcessor next : apiProcessors) {
                    if(next.supports(annotation)) {
                        api = next.process(annotation, api);
                    }
                }
            }

            //process Parameter level Annotations
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            for(java.lang.reflect.Parameter param : parameters) {
                Annotation[] annotations = param.getAnnotations();
                for(Annotation annotation : annotations) {
                    for(ParameterProcessor next : parameterProcessors) {
                        if(next.supports(annotation)) {
                            api = next.process(annotation, param, api);
                        }
                    }
                }
            }
            apis.add(api);
        }
        return apis;
    }

    private void setControllerProcessors() {
        this.controllerProcessors = ServiceLoader.load(ControllerProcessor.class);
    }

    private void setParameterProcessors() {
        this.parameterProcessors = ServiceLoader.load(ParameterProcessor.class);
    }

    private void setApiProcessors() {
        this.apiProcessors = ServiceLoader.load(ApiProcessor.class);
    }

    private void createMappingClassSet() {
        this.mappingClassesSet = new HashSet<>();
        mappingClassesSet.add(Constants.GET_MAPPING);
        mappingClassesSet.add(Constants.POST_MAPPING);
        mappingClassesSet.add(Constants.PUT_MAPPING);
        mappingClassesSet.add(Constants.PATCH_MAPPING);
        mappingClassesSet.add(Constants.DELETE_MAPPING);
    }

    private Controller createController(Class controllerClass) {
        Annotation[] annotations = controllerClass.getAnnotations();
        for(Annotation annotation : annotations) {
            for(ControllerProcessor next : controllerProcessors) {
                if(next.supports(annotation)) {
                    Controller controller = next.process(annotation);
                    controller.setName(controllerClass.getName().replace(controllerClass.getPackageName() + ".", ""));
                    return controller;
                }
            }
        }
        return null;
    }



    private List<Class> findResControllers(List<String> classes) {
        List<Class> controllerClasses = new ArrayList<>();

        for(String className : classes) {
            try {
                Class<?> aClass = Class.forName(className, true, urlClassLoader);
                Annotation[] annotations = aClass.getAnnotations();
                for(Annotation annotation : annotations) {
                    if(annotation.annotationType().getCanonicalName().equals(Constants.REST_CONTROLLER)) {
                        controllerClasses.add(aClass);
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return controllerClasses;
    }

    private List<String> findClassNames(File file) {
        List<String> classes = new ArrayList<>();
        JarFile jarFile = null;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
            jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if(jarEntry.getName().endsWith("class")) {
                    classes.add(jarEntry.getName().replace("BOOT-INF/classes/","")
                                .replace("/", ".")
                                .replace(".class","")
                            .replace("$1",""));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private void writeSimpleFile(String childPackageName, String className, String fileName) {
        String packageName = project.getGroupId() + "." + childPackageName;
        InputStream resourceAsStream = this.getClass().getResourceAsStream(fileName);
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the service.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            data = data.replace("<PackageName>", packageName);
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put(packageName+"." + className, data);
            FileWriter.writeFiles(contentMap);
        } catch (IOException x) {
            throw new IllegalStateException("Unable to find " + fileName);
        }
    }
}
