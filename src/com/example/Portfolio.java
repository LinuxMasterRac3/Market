package com.example;

import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Portfolio {
    private List<Stock> stocks;

    public Portfolio() {
        stocks = new ArrayList<>();
    }

    public List<Stock> getStocks() {
        return stocks;
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
        return portfolio;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        JSONArray stocksArray = new JSONArray();
        for (Stock stock : this.stocks) {
            stocksArray.put(stock.toJsonObject());
        }
        jsonObject.put("stocks", stocksArray);
        return jsonObject.toString();
    }
}