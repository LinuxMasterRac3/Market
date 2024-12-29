package com.example;

import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

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

    public ObjectNode toJsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("type", type);
        json.put("ticker", ticker);
        json.put("quantity", quantity);
        json.put("price", price);
        json.put("timestamp", timestamp.toString());
        return json;
    }

    public String toJsonString() {
        return toJsonNode().toString();
    }

    // Alias for backward compatibility
    public String toJson() {
        return toJsonString();
    }

    public static String toJsonArrayString(List<Transaction> transactions) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(transactions).toString();
    }

    // No hardcoded INSTANCE_CONNECTION_NAME found; handles transaction data independently
}
