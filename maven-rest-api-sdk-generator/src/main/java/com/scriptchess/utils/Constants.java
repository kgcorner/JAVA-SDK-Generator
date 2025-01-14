package com.scriptchess.utils;

public class Constants {
    public static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    public static final String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    public static final String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    public static final String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    public static final String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    public static final String PATCH_MAPPING = "org.springframework.web.bind.annotation.PatchMapping";
    public static final String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    public static final String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";
    public static final String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    public static final String REQUEST_HEADER = "org.springframework.web.bind.annotation.RequestHeader";
    public static final String GENERATED_SOURCE_DIRECTORY_NAME = "generated-source";
    public static final String TAB = "  ";
    public static final String TAB_TAB = "      ";
    public static final String TAB_TAB_TAB = "          ";
    public static enum HTTP_METHODS  {
        GET,POST,PUT, PATCH,DELETE
    }
}
