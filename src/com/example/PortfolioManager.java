package com.example;

import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONObject;

// Importa la classe Transaction esterna
import com.example.Transaction;

public class PortfolioManager {
    private Map<String, Portfolio> userPortfolios;
    private static final String PORTFOLIO_FILE = "portafoglio.json"; // Cambiato da portfolios.json

    public PortfolioManager() {
        loadPortfolios();
    }

    public String getPortfolioJson(String username) {
        try {
            Portfolio portfolio = userPortfolios.getOrDefault(username, new Portfolio());
            JSONObject json = new JSONObject();
            JSONArray stocksArray = new JSONArray();
            
            for (Stock stock : portfolio.getStocks()) {
                JSONObject stockJson = new JSONObject();
                stockJson.put("ticker", stock.getTicker());
                stockJson.put("quantity", stock.getQuantity());
                stockJson.put("purchasePrice", stock.getPurchasePrice());
                stockJson.put("currentPrice", stock.getCurrentPrice());
                stockJson.put("isBond", stock.isBond());
                stocksArray.put(stockJson);
            }
            
            json.put("stocks", stocksArray);
            /* Aggiunta delle transazioni al JSON */
            JSONArray transactionsArray = new JSONArray();
            for (Transaction tx : portfolio.getTransactions()) { // Utilizza com.example.Transaction
                transactionsArray.put(tx.toJson());
            }
            json.put("transactions", transactionsArray);
            return json.toString();
        } catch (Exception e) {
            System.err.println("Error creating portfolio JSON: " + e.getMessage());
            return "{\"stocks\":[]}";
        }
    }

    // Metodo per aggiungere una transazione
    public void addTransaction(String username, Transaction transaction) { // Utilizza com.example.Transaction
        Portfolio portfolio = userPortfolios.getOrDefault(username, new Portfolio());
        portfolio.getTransactions().add(transaction);
        userPortfolios.put(username, portfolio);
        savePortfolios();
    }

    // Metodo per ottenere lo storico delle transazioni
    public List<Transaction> getTransactions(String username) { // Utilizza com.example.Transaction
        Portfolio portfolio = userPortfolios.getOrDefault(username, new Portfolio());
        return portfolio.getTransactions();
    }

    public void addStock(String username, Stock stock) {
        Portfolio portfolio = userPortfolios.getOrDefault(username, new Portfolio());
        portfolio.getStocks().add(stock);
        double currentPrice = YahooFinanceAPI.getCurrentPrice(stock.getTicker());
        stock.setCurrentPrice(currentPrice);
        userPortfolios.put(username, portfolio);
        savePortfolios();
        /* Aggiunta della transazione */
        addTransaction(username, new Transaction("Aggiunta", stock.getTicker(), stock.getQuantity(), stock.getPurchasePrice()));
    }

    public boolean removeStock(String username, String ticker) {
        Portfolio portfolio = userPortfolios.get(username);
        if (portfolio == null) return false;
        boolean removed = portfolio.getStocks().removeIf(s -> s.getTicker().equalsIgnoreCase(ticker));
        if (removed) {
            userPortfolios.put(username, portfolio);
            savePortfolios();
            System.out.println("Stock " + ticker + " rimossa con successo."); // Log aggiunto
            /* Aggiunta della transazione */
            addTransaction(username, new Transaction("Rimozione", ticker, 0, 0.0));
        } else {
            System.out.println("Stock " + ticker + " non trovata."); // Log aggiunto
        }
        return removed;
    }

    public boolean modifyStock(String username, String ticker, int newQuantity, double newPurchasePrice, boolean isBond) {
        Portfolio portfolio = userPortfolios.getOrDefault(username, new Portfolio());
        for (Stock stock : portfolio.getStocks()) {
            if (stock.getTicker().equalsIgnoreCase(ticker)) {
                stock.setQuantity(newQuantity);
                stock.setPurchasePrice(newPurchasePrice);
                stock.setBond(isBond);
                // Aggiorna il currentPrice utilizzando l'API
                double currentPrice = YahooFinanceAPI.getCurrentPrice(ticker);
                if (currentPrice > 0) {
                    stock.setCurrentPrice(currentPrice);
                }
                userPortfolios.put(username, portfolio);
                savePortfolios();
                /* Aggiunta della transazione */
                addTransaction(username, new Transaction("Modifica", ticker, newQuantity, newPurchasePrice));
                return true;
            }
        }
        return false; // Stock non trovata
    }

    public void updatePrices(String username) {
        Portfolio portfolio = userPortfolios.get(username);
        if (portfolio == null) return;
        try {
            for (Stock stock : portfolio.getStocks()) {
                System.out.println("Aggiornamento prezzo per: " + stock.getTicker()); // Log aggiunto
                double currentPrice = YahooFinanceAPI.getCurrentPrice(stock.getTicker());
                if (currentPrice > 0) {
                    stock.setCurrentPrice(currentPrice);
                    System.out.println("Prezzo aggiornato per " + stock.getTicker() + ": " + currentPrice); // Log aggiunto
                } else {
                    System.err.println("Impossibile aggiornare il prezzo per " + stock.getTicker()); // Log aggiunto
                }
            }
            userPortfolios.put(username, portfolio);
            savePortfolios();
            System.out.println("Portafoglio salvato dopo l'aggiornamento dei prezzi"); // Log aggiunto
        } catch (Exception e) {
            System.err.println("Errore nel metodo updatePrices: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double calculateTaxes(String username) {
        Portfolio portfolio = userPortfolios.getOrDefault(username, new Portfolio());
        double taxes = 0.0;
        for (Stock stock : portfolio.getStocks()) {
            double profit = (stock.getCurrentPrice() - stock.getPurchasePrice()) * stock.getQuantity();
            double rate = stock.isBond() ? 0.125 : 0.26;
            taxes += profit * rate;
        }
        return taxes;
    }

    private void loadPortfolios() {
        userPortfolios = new HashMap<>();
        try {
            File file = new File(PORTFOLIO_FILE);
            if (!file.exists()) {
                savePortfolios();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(PORTFOLIO_FILE));
            StringBuilder jsonStr = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr.append(line);
            }
            reader.close();

            String jsonContent = jsonStr.toString().trim();
            if (jsonContent.isEmpty()) {
                return;
            }

            JSONObject jsonObject = new JSONObject(jsonContent);
            if (jsonObject.has("stocks")) {
                // Formato vecchio (singolo portfolio)
                Portfolio portfolio = Portfolio.fromJson(jsonContent);
                userPortfolios.put("default", portfolio);
            } else {
                // Formato nuovo (multi-utente)
                for (String username : jsonObject.keySet()) {
                    Portfolio portfolio = Portfolio.fromJson(jsonObject.getJSONObject(username).toString());
                    userPortfolios.put(username, portfolio);
                }
            }
        } catch (IOException e) {
            userPortfolios = new HashMap<>();
        }
    }

    private void savePortfolios() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(PORTFOLIO_FILE));
            if (userPortfolios.size() == 1 && userPortfolios.containsKey("default")) {
                // Salva nel formato vecchio per compatibilità
                writer.write(userPortfolios.get("default").toJson());
            } else {
                // Salva nel nuovo formato multi-utente
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, Portfolio> entry : userPortfolios.entrySet()) {
                    jsonObject.put(entry.getKey(), new JSONObject(entry.getValue().toJson()));
                }
                writer.write(jsonObject.toString());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio dei portafogli: " + e.getMessage());
        }
    }
}