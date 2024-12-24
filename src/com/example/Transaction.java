package com.example;

import org.json.JSONObject;
import java.time.LocalDateTime;

public class Transaction {
    private String type; // "Aggiunta", "Rimozione", "Modifica"
    private String ticker;
    private int quantity;
    private double price;
    private LocalDateTime timestamp;

    public Transaction(String type, String ticker, int quantity, double price) {
        this.type = type;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
    }

    // Getters e Setters
    public String getType() { return type; }
    public String getTicker() { return ticker; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("ticker", ticker);
        json.put("quantity", quantity);
        json.put("price", price);
        json.put("timestamp", timestamp.toString());
        return json;
    }
}
