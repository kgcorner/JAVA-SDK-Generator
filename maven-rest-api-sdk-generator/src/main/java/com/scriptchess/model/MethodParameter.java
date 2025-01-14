package com.scriptchess.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Parameter;

@Data
@AllArgsConstructor
public class MethodParameter {
    private String name;
    private boolean multipartParam;
    private transient Parameter parameter;
}
