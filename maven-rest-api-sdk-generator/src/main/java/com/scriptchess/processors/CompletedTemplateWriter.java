package com.scriptchess.processors;

import com.scriptchess.utils.FileWriter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CompletedTemplateWriter {
    public void writer(String basePackageName) {
        Map<String, String> contentMap = new HashMap<>();
        //write request templates
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/templates/request.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the request.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            data = data.replace("<PackageName>", basePackageName + ".requests");
            contentMap.put(basePackageName + ".requests.Request", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        resourceAsStream = this.getClass().getResourceAsStream("/templates/request-factory.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the request-factory.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            data = data.replace("<PackageName>", basePackageName + ".requests");
            contentMap.put(basePackageName + ".requests.RequestClientFactory", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        resourceAsStream = this.getClass().getResourceAsStream("/templates/sdk-config.template");
        if(resourceAsStream == null)
            throw new IllegalStateException("Unable to find the sdk-config.template");
        try {
            String data = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            data = data.replace("<PackageName>", basePackageName + ".config");
            contentMap.put(basePackageName + ".requests.SDKConfig", data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                resourceAsStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileWriter.writeFiles(contentMap);
    }
}
