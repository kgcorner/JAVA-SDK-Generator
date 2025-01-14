package com.scriptchess.models;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private int id;
    private String userName;
    private List<Address> addresses;
}
