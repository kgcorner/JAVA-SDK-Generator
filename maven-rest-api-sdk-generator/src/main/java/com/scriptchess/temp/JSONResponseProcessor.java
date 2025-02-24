package com.scriptchess.temp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class JSONResponseProcessor implements ResponseTypeProcessor {
    @Override
    public boolean supports(String mediaType) {
        return mediaType != null && mediaType.equals("application/json");
    }

    @Override
    public <T> T convert(String data, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(data, type);
    }

    @Override
    public <T> T convertFromList(String data, Class<T> type) {
        Type typeOfT = TypeToken.getParameterized(List.class, type).getType();
        return new Gson().fromJson(data, typeOfT);
    }
}
