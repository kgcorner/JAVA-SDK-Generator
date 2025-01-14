package com.scriptchess.temp;

public interface ResponseTypeProcessor {
    boolean supports(String mediaType);
    <T> T convert(String data, Class<T> type);
    <T> T convertFromList(String data, Class<T> type);
}
