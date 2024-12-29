package com.example;

import java.sql.SQLException;

public class UserManager {
    private final DatabaseManager dbManager;

    public UserManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean register(String username, String password) throws SQLException {
        return dbManager.addUser(username, password);
    }

    public boolean authenticate(String username, String password) throws SQLException {
        boolean isAuthenticated = dbManager.authenticateUser(username, password);
        if (isAuthenticated) {
            dbManager.setUserLoggedIn(username, true);
        }
        return isAuthenticated;
    }

    public void logout(String username) throws SQLException {
        dbManager.setUserLoggedIn(username, false);
    }

    public void setUserLoggedIn(String username, boolean loggedIn) throws SQLException {
        dbManager.setUserLoggedIn(username, loggedIn);
    }
}