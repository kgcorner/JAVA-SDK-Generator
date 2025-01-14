package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.Controller;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServiceImplementationWriter {
    public Map<String, String> writeController(Controller controller, String basePackageName) {
        Map<String, String> contentMap = new HashMap<>();
        String implPackageName = basePackageName + ".service.impl";
        String packageName = basePackageName + ".service.impl." + controller.getName()+"ServiceImpl";
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/templates/service-impl.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the client.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            data = data.replace("<PackageName>", implPackageName);
            data = data.replaceAll("<Controller>", controller.getName());
            StringBuilder importBuilder = new StringBuilder();
            StringBuilder apiBuilder = new StringBuilder();
            ServiceApiWriter serviceApiWriter = new ServiceApiWriter();
            for(Api api : controller.getApis()) {
                List<String> content = serviceApiWriter.write(api, basePackageName);
                importBuilder.append("\n").append(content.get(0)).append("\n");
                apiBuilder.append("\n\n");
                apiBuilder.append(content.get(1));
            }
            importBuilder.append("import ").append(basePackageName).append(".service.")
                    .append(controller.getName()).append("Service;");
            String imports = importBuilder.toString();
            String apis = apiBuilder.toString();
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
            data = data.replace("<imports>", imports);
            data = data.replace("<Apis>", apis);
            contentMap.put(packageName, data);
        }catch (IOException x) {
            throw new IllegalStateException("Unable to find /templates/service-impl.template");
        }
        return contentMap;
    }
}
