package com.example;

import org.json.JSONObject;

public class User {
    private String username;
    private String passwordHash;
    private boolean isLoggedIn;  // Nuovo campo per lo stato della sessione

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.isLoggedIn = false;
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("passwordHash", passwordHash);
        json.put("isLoggedIn", isLoggedIn);
        return json;
    }

    public static User fromJsonObject(JSONObject json) {
        String username = json.getString("username");
        String passwordHash = json.getString("passwordHash");
        User user = new User(username, passwordHash);
        user.setLoggedIn(json.optBoolean("isLoggedIn", false));
        return user;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    
    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.isLoggedIn = loggedIn; }

    public void logout() {
        this.isLoggedIn = false;
    }
}
