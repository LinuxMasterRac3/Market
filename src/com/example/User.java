package com.example;

public class User {
    private String username;
    private String password; // In una vera applicazione, le password dovrebbero essere hashate

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter e Setter
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    // Verifica che non vi siano metodi che serializzano o deserializzano utenti in JSON
    // Tutte le operazioni dovrebbero passare attraverso DatabaseManager
}
