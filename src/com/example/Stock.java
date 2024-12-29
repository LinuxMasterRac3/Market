package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Stock {
    private String ticker;
    private int quantity;
    private double purchasePrice;
    private double currentPrice;
    private boolean isBond;

    public Stock(String ticker, int quantity, double purchasePrice, boolean isBond) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = 0.0; // Prezzo iniziale
        this.isBond = isBond;
    }

    public Stock(String ticker, int quantity, double purchasePrice, double currentPrice, boolean isBond) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.isBond = isBond;
    }

    // Getter e Setter
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public boolean isBond() { return isBond; }
    public void setBond(boolean isBond) { this.isBond = isBond; }

    public ObjectNode toJsonNode() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("ticker", ticker);
        json.put("quantity", quantity);
        json.put("purchasePrice", purchasePrice);
        json.put("currentPrice", currentPrice);
        json.put("isBond", isBond);
        return json;
    }
}