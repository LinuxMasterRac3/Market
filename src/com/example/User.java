package com.example;

import org.json.JSONObject;

public class User {
    private String username;
    private String passwordHash;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("passwordHash", passwordHash);
        return json;
    }

    public static User fromJsonObject(JSONObject json) {
        String username = json.getString("username");
        String passwordHash = json.getString("passwordHash");
        return new User(username, passwordHash);
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
