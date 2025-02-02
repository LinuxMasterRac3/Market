package com.example;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class Portfolio {
    private List<Stock> stocks;
    private List<Transaction> transactions;
    private static final ObjectMapper mapper = new ObjectMapper();

    public Portfolio() {
        stocks = new ArrayList<>();
        transactions = new ArrayList<>();
    }

    public Portfolio(List<Stock> stocks, List<Transaction> transactions) {
        this.stocks = stocks != null ? stocks : new ArrayList<>();
        this.transactions = transactions != null ? transactions : new ArrayList<>();
    }

    public List<Stock> getStocks() { return stocks; }
    public List<Transaction> getTransactions() { return transactions; }

    public void setStocks(List<Stock> stocks) { this.stocks = stocks; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public String toJsonString() {
        try {
            ObjectNode json = mapper.createObjectNode();
            
            ArrayNode stocksArray = mapper.createArrayNode();
            if (stocks != null) {
                for (Stock stock : stocks) {
                    stocksArray.add(stock.toJsonNode());
                }
            }
            json.set("stocks", stocksArray);
            
            ArrayNode transactionsArray = mapper.createArrayNode();
            if (transactions != null) {
                for (Transaction tx : transactions) {
                    transactionsArray.add(tx.toJsonNode());
                }
            }
            json.set("transactions", transactionsArray);
            
            String jsonString = mapper.writeValueAsString(json);
            System.out.println("Generated portfolio JSON: " + jsonString);
            return jsonString;
        } catch (Exception e) {
            System.err.println("Error converting portfolio to JSON: " + e.getMessage());
            return "{\"error\":\"Error converting portfolio to JSON\", \"stocks\":[], \"transactions\":[]}";
        }
    }
}