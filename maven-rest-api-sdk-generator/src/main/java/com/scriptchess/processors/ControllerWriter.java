package com.scriptchess.processors;

import com.scriptchess.model.Api;
import com.scriptchess.model.Controller;
import com.scriptchess.utils.FileWriter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ControllerWriter {
    public Map<String, String> writeController(Controller controller, String basePackageName) {
        Map<String, String> contentMap = new HashMap<>();
        String packageName = basePackageName + ".clients." + controller.getName()+"Client";
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/templates/client.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the client.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            data = data.replace("<PackageName>", basePackageName + ".clients");
            data = data.replaceAll("<Controller>", controller.getName());
            data = data.replaceAll("<BasePackageName>", basePackageName);
            StringBuilder importBuilder = new StringBuilder();
            StringBuilder apiBuilder = new StringBuilder();
            ControllerApiWriter apiWriter = new ControllerApiWriter();
            for(Api api : controller.getApis()) {
                List<String> content = apiWriter.write(api);
                importBuilder.append("\n").append(content.get(0));
                apiBuilder.append("\n\n");
                apiBuilder.append(content.get(1));
            }

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
        } catch (IOException x) {
            throw new IllegalStateException("Unable to find /templates/client.template");
        }
        return contentMap;
    }
}
