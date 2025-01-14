package com.scriptchess.temp;

public class MultipartRequestTypeProcessor implements RequestTypeProcessor {
    @Override
    public boolean supports(String mediaType) {
        return false;
    }

    @Override
    public String convert(Object data) {
        return "";
    }
}
