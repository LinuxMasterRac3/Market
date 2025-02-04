package com.example;

import java.util.Date;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CashflowTransaction {
    private String type;
    private double amount;
    private String description;
    private Date date;

    public CashflowTransaction(String type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = new Date();
    }

    // Getters and setters
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public Date getDate() { return date; }

    public ObjectNode toJsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("type", type);
        node.put("amount", amount);
        node.put("description", description);
        node.put("date", date.getTime());
        return node;
    }
}
