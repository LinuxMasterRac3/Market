package com.example;

import org.json.JSONObject;

public class Stock {
    private String ticker;
    private int quantity;
    private double purchasePrice;
    private double currentPrice;
    private boolean isBond;

    public Stock(String ticker, int quantity, double purchasePrice, double currentPrice, boolean isBond) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.isBond = isBond;
    }

    // Aggiungi un costruttore sovraccarico
    public Stock(String ticker, int quantity, double purchasePrice, boolean isBond) {
        this(ticker, quantity, purchasePrice, 0.0, isBond); // Imposta currentPrice a 0.0 di default
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        json.put("ticker", ticker);
        json.put("quantity", quantity);
        json.put("purchasePrice", purchasePrice);
        json.put("currentPrice", currentPrice);
        json.put("isBond", isBond);
        return json;
    }

    public static Stock fromJsonObject(JSONObject json) {
        String ticker = json.getString("ticker");
        int quantity = json.getInt("quantity");
        double purchasePrice = json.getDouble("purchasePrice");
        double currentPrice = json.getDouble("currentPrice");
        boolean isBond = json.getBoolean("isBond");

        Stock stock = new Stock(ticker, quantity, purchasePrice, currentPrice, isBond);
        return stock;
    }

    // Getter e setter
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public boolean isBond() { return isBond; }
    public void setBond(boolean bond) { isBond = bond; }
}