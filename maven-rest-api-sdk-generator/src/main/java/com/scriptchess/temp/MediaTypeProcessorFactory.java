package com.scriptchess.temp;

import java.util.ArrayList;
import java.util.List;

public class MediaTypeProcessorFactory {
    public static List<RequestTypeProcessor> requestTypeProcessor;
    public static List<ResponseTypeProcessor> responseTypeProcessor;

    static {
        requestTypeProcessor = new ArrayList<>();
        requestTypeProcessor.add(new JSONRequestProcessor());

        responseTypeProcessor = new ArrayList<>();
        responseTypeProcessor.add(new JSONResponseProcessor());
    }

    public static RequestTypeProcessor getRequestTypeProducer(String mediaType) {
        for(RequestTypeProcessor processor : requestTypeProcessor) {
            if(processor.supports(mediaType)) {
                return processor;
            }
        }
        return null;
    }

    public static ResponseTypeProcessor getResponseTypeProducer(String mediaType) {
        for(ResponseTypeProcessor processor : responseTypeProcessor) {
            if(processor.supports(mediaType)) {
                return processor;
            }
        }
        return null;
    }
}
