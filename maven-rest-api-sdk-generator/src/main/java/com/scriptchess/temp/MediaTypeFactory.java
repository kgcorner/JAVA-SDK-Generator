package com.scriptchess.temp;

import okhttp3.MediaType;

public class MediaTypeFactory {
    public static MediaType getDefaultMediaType() {
        return getMediaType("application/json");
    }

    public static MediaType getMediaType(String type) {
        return MediaType.get(type);
    }
}
