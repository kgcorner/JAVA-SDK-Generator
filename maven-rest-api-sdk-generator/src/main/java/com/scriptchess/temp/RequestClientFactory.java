package com.scriptchess.temp;


import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

import java.util.ServiceLoader;

public class RequestClientFactory {
    private static OkHttpClient client;

    public static OkHttpClient getHttpClient() {
        if(client != null)
            return client;
        ServiceLoader<Interceptor> interceptors = ServiceLoader.load(Interceptor.class);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        for(Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }
        client = builder.build();
        return client;
    }
}
