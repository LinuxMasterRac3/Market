package com.example;

import java.util.*;
import java.io.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserManager {
    private List<User> users;
    private static final String USER_FILE = "users.json";

    public UserManager() {
        loadUsers();
    }

    public boolean registerUser(String username, String password) {
        if (getUserByUsername(username) != null) {
            return false; // Utente già esistente
        }
        String passwordHash = hashPassword(password);
        User user = new User(username, passwordHash);
        users.add(user);
        saveUsers();
        return true;
    }

    public boolean authenticate(String username, String password) {
        User user = getUserByUsername(username);
        if (user == null) return false;
        String passwordHash = hashPassword(password);
        return user.getPasswordHash().equals(passwordHash);
    }

    private User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for(byte b : hash){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private void loadUsers() {
        users = new ArrayList<>();
        try {
            File file = new File(USER_FILE);
            if (!file.exists()) {
                saveUsers(); // Crea file vuoto
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(USER_FILE));
            StringBuilder jsonStr = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr.append(line);
            }
            reader.close();
            JSONArray jsonArray = new JSONArray(jsonStr.toString());
            for(int i=0;i<jsonArray.length();i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                users.add(User.fromJsonObject(obj));
            }
        } catch (IOException e) {
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE));
            JSONArray jsonArray = new JSONArray();
            for(User user : users) {
                jsonArray.put(user.toJsonObject());
            }
            writer.write(jsonArray.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio degli utenti: " + e.getMessage());
        }
    }
}