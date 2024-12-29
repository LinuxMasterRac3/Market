package com.example;

import java.sql.SQLException;
import java.util.List;

public class PortfolioManager {
    private final DatabaseManager dbManager;

    public PortfolioManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // Add getTransactions method
    public List<Transaction> getTransactions(String username) throws SQLException {
        return dbManager.getTransactions(username);
    }

    public Portfolio getPortfolio(String username) {
        try {
            List<Stock> stocks = dbManager.getStocks(username);
            List<Transaction> transactions = dbManager.getTransactions(username);
            return new Portfolio(stocks, transactions);
        } catch (SQLException e) {
            System.err.println("Error retrieving portfolio for " + username + ": " + e.getMessage());
            return new Portfolio();
        }
    }

    public void addStock(String username, Stock stock) {
        try {
            double currentPrice = YahooFinanceAPI.getCurrentPrice(stock.getTicker());
            stock.setCurrentPrice(currentPrice);
            dbManager.addStock(username, stock);
            addTransaction(username, new Transaction("ADD", stock.getTicker(), stock.getQuantity(), stock.getPurchasePrice()));
        } catch (SQLException e) {
            System.err.println("Error adding stock: " + e.getMessage());
        }
    }

    public boolean removeStock(String username, String ticker) {
        try {
            boolean success = dbManager.removeStock(username, ticker);
            if (success) {
                addTransaction(username, new Transaction("REMOVE", ticker, 0, 0));
            }
            return success;
        } catch (SQLException e) {
            System.err.println("Error removing stock: " + e.getMessage());
            return false;
        }
    }

    public boolean modifyStock(String username, String ticker, int newQuantity, double newPurchasePrice, boolean isBond) {
        try {
            List<Stock> stocks = dbManager.getStocks(username);
            Stock stockToModify = null;
            for (Stock stock : stocks) {
                if (stock.getTicker().equalsIgnoreCase(ticker)) {
                    stockToModify = stock;
                    break;
                }
            }
            if (stockToModify != null) {
                stockToModify.setQuantity(newQuantity);
                stockToModify.setPurchasePrice(newPurchasePrice);
                stockToModify.setBond(isBond);
                double currentPrice = YahooFinanceAPI.getCurrentPrice(ticker);
                if (currentPrice > 0) {
                    stockToModify.setCurrentPrice(currentPrice);
                }
                boolean updated = dbManager.updateStock(username, stockToModify);
                if (updated) {
                    addTransaction(username, new Transaction("Modifica", ticker, newQuantity, newPurchasePrice));
                }
                return updated;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Errore nella modifica dello stock: " + e.getMessage());
            return false;
        }
    }

    public void updatePrices(String username) {
        try {
            List<Stock> stocks = dbManager.getStocks(username);
            for (Stock stock : stocks) {
                double currentPrice = YahooFinanceAPI.getCurrentPrice(stock.getTicker());
                if (currentPrice > 0) {
                    stock.setCurrentPrice(currentPrice);
                    dbManager.updateStock(username, stock);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento dei prezzi: " + e.getMessage());
        }
    }

    public double calculateTaxes(String username) {
        double taxes = 0.0;
        try {
            List<Stock> stocks = dbManager.getStocks(username);
            for (Stock stock : stocks) {
                double profit = (stock.getCurrentPrice() - stock.getPurchasePrice()) * stock.getQuantity();
                double rate = stock.isBond() ? 0.125 : 0.26;
                taxes += profit * rate;
            }
        } catch (SQLException e) {
            System.err.println("Errore nel calcolo delle tasse: " + e.getMessage());
        }
        return taxes;
    }

    private void addTransaction(String username, Transaction transaction) throws SQLException {
        dbManager.addTransaction(username, transaction);
    }
}