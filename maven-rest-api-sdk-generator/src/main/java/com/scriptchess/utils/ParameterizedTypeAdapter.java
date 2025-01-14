package com.scriptchess.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public class ParameterizedTypeAdapter  extends TypeAdapter<ParameterizedType> {
    @Override
    public void write(JsonWriter jsonWriter, ParameterizedType parameterizedType) throws IOException {
        if(parameterizedType == null){
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(Utilities.getClassName(parameterizedType));
    }

    @Override
    public ParameterizedType read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        Class<?> clazz = null;
        try {
            clazz = Class.forName(jsonReader.nextString());
        } catch (ClassNotFoundException exception) {
            throw new IOException(exception);
        }
        return null;
    }
}
