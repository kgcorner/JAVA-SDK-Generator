package com.scriptchess.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Controller {
    private String name;
    private String path;
    private String[] produces;
    private String[] consumes;
    private List<Api> apis;
}
