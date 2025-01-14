package com.scriptchess.temp;

public interface RequestTypeProcessor {
    boolean supports(String mediaType);
    String convert(Object data);
}
