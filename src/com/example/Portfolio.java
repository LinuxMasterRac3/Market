package com.example;

import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Portfolio {
    private List<Stock> stocks;
    private List<Transaction> transactions;

    public Portfolio() {
        stocks = new ArrayList<>();
        transactions = new ArrayList<>();
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public static Portfolio fromJson(String jsonString) {
        Portfolio portfolio = new Portfolio();
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray stocksArray = jsonObject.getJSONArray("stocks");
        for (int i = 0; i < stocksArray.length(); i++) {
            JSONObject stockJson = stocksArray.getJSONObject(i);
            Stock stock = Stock.fromJsonObject(stockJson);
            portfolio.getStocks().add(stock);
        }

        if (jsonObject.has("transactions")) {
            JSONArray transactionsArray = jsonObject.getJSONArray("transactions");
            for (int i = 0; i < transactionsArray.length(); i++) {
                JSONObject txJson = transactionsArray.getJSONObject(i);
                Transaction tx = new Transaction(
                    txJson.getString("type"),
                    txJson.getString("ticker"),
                    txJson.getInt("quantity"),
                    txJson.getDouble("price")
                );
                portfolio.getTransactions().add(tx);
            }
        }

        return portfolio;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray stocksArray = new JSONArray();
        for (Stock stock : this.stocks) {
            stocksArray.put(stock.toJsonObject());
        }
        jsonObject.put("stocks", stocksArray);

        JSONArray transactionsArray = new JSONArray();
        for (Transaction tx : transactions) {
            transactionsArray.put(tx.toJson());
        }
        jsonObject.put("transactions", transactionsArray);

        return jsonObject.toString();
    }
}