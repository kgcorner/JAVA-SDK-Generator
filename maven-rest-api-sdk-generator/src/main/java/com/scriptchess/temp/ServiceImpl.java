package com.scriptchess.temp;

import okhttp3.Request;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class ServiceImpl {
    public Object getStudent(String studentId) {
        String url = "";
        url = SDKConfig.getHostName() + url.replace("{studentId}", studentId);
        String response = Requests.doGet(url, Collections.emptyMap());
        ResponseTypeProcessor responseTypeProducer = MediaTypeProcessorFactory.getResponseTypeProducer("application/json");
        if(responseTypeProducer != null) {
            return responseTypeProducer.convert(response, Object.class);
        }
        return response;
    }

    public Object create(String studentId) {
        String studentJson = "{\n" +
                "    \"name\" : \"Gaurav\",\n" +
                "    \"class\":\"10th\",\n" +
                "    \"rollNum\":17,\n" +
                "    \"schoolName\" \"KMV\"\n" +
                "}";
        String url = "";
        url = SDKConfig.getHostName() + url.replace("{studentId}", studentId);
        String response = Requests.doPost(url, Collections.emptyMap(),studentJson.getBytes(StandardCharsets.UTF_8),
                MediaTypeFactory.getDefaultMediaType());
        ResponseTypeProcessor responseTypeProducer = MediaTypeProcessorFactory.getResponseTypeProducer("application/json");
        if(responseTypeProducer != null) {
            return responseTypeProducer.convert(response, Object.class);
        }
        return response;
    }
}
