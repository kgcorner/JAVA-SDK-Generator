package com.scriptchess.temp;

import com.google.gson.Gson;

public class JSONRequestProcessor implements RequestTypeProcessor {
    @Override
    public boolean supports(String mediaType) {
        return mediaType != null && mediaType.equals("application/json");
    }

    @Override
    public String convert(Object data) {
        Gson gson = new Gson();
        return gson.toJson(data);
    }
}
