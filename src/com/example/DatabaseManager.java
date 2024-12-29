package com.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager implements AutoCloseable {
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;
    private Connection connection;

    static {
        // Get environment variables with Supabase-specific defaults
        String dbHost = System.getenv().getOrDefault("SUPABASE_DB_HOST", "aws-0-us-west-1.pooler.supabase.com");
        String dbPort = System.getenv().getOrDefault("SUPABASE_DB_PORT", "6543");
        String dbName = System.getenv().getOrDefault("SUPABASE_DB_NAME", "postgres");
        DB_USER = System.getenv().getOrDefault("SUPABASE_DB_USER", "postgres.nltoknxotsigtyrceuyt");
        DB_PASSWORD = System.getenv().getOrDefault("SUPABASE_DB_PASSWORD", "your_password_here");
        
        // Construct database URL with SSL only (removed pool_mode)
        DB_URL = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", 
            dbHost, dbPort, dbName);
        
        // Remove password from log for security
        System.out.println("Database URL: " + DB_URL.replaceAll("password=.*?[&]", "password=****&"));
        
        // Load the PostgreSQL driver
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found: " + e.getMessage());
        }
    }

    public DatabaseManager() throws SQLException {
        connect();
        initializeDatabase();
    }

    private void connect() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("sslmode", "require");
        props.setProperty("prepareThreshold", "0"); // Disable prepared statements
        props.setProperty("reWriteBatchedInserts", "true");

        int retryCount = 10;  // Increased retry count for VPC connection
        int waitTimeSeconds = 10;  // Increased wait time
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                System.out.println("Connecting to database... Attempt " + attempt);
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(DB_URL, props);
                System.out.println("Database connection established successfully");
                break; // Exit loop if connection is successful
            } catch (ClassNotFoundException e) {
                System.err.println("PostgreSQL JDBC Driver not found: " + e.getMessage());
                throw new SQLException("PostgreSQL JDBC Driver not found.", e);
            } catch (SQLException e) {
                System.err.println("Failed to connect to database on attempt " + attempt + ": " + e.getMessage());
                if (attempt == retryCount) {
                    throw e; // Rethrow exception if max attempts reached
                }
                System.out.println("Retrying in " + waitTimeSeconds + " seconds...");
                try {
                    Thread.sleep(waitTimeSeconds * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted during database connection retries.", ie);
                }
            }
        }
    }

    private void initializeDatabase() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Create tables with proper Supabase schema
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "id UUID DEFAULT uuid_generate_v4() PRIMARY KEY," +  // Add UUID support
            "username VARCHAR(50) UNIQUE NOT NULL," +
            "password VARCHAR(255) NOT NULL," +
            "is_logged_in BOOLEAN DEFAULT FALSE," +
            "created_at TIMESTAMPTZ DEFAULT NOW()," +
            "updated_at TIMESTAMPTZ DEFAULT NOW()" +
            ")");

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS stocks (" +
            "id UUID DEFAULT uuid_generate_v4() PRIMARY KEY," +
            "username VARCHAR(50) REFERENCES users(username)," +
            "ticker VARCHAR(10) NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "purchase_price DECIMAL(10,2) NOT NULL," +
            "current_price DECIMAL(10,2) NOT NULL," +
            "is_bond BOOLEAN DEFAULT FALSE," +
            "created_at TIMESTAMPTZ DEFAULT NOW()," +
            "updated_at TIMESTAMPTZ DEFAULT NOW()" +
            ")");

        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS transactions (" +
            "id UUID DEFAULT uuid_generate_v4() PRIMARY KEY," +
            "username VARCHAR(50) REFERENCES users(username)," +
            "type VARCHAR(10) NOT NULL," +
            "ticker VARCHAR(10) NOT NULL," +
            "quantity INTEGER NOT NULL," +
            "price DECIMAL(10,2) NOT NULL," +
            "timestamp TIMESTAMPTZ DEFAULT NOW()" +
            ")");

        // Create indexes for better performance
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_stocks_username ON stocks(username)");
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_username ON transactions(username)");
    }

    // Metodi per gli utenti
    public boolean addUser(String username, String password) throws SQLException {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Utente già esistente
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String dbPassword = rs.getString("password");
                return dbPassword.equals(password);
            }
            return false;
        }
    }

    public void setUserLoggedIn(String username, boolean isLoggedIn) throws SQLException {
        String query = "UPDATE users SET is_logged_in = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, isLoggedIn);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    // Metodi per gli stock
    public void addStock(String username, Stock stock) throws SQLException {
        String query = "INSERT INTO stocks (username, ticker, quantity, purchase_price, current_price, is_bond) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, stock.getTicker());
            pstmt.setInt(3, stock.getQuantity());
            pstmt.setDouble(4, stock.getPurchasePrice());
            pstmt.setDouble(5, stock.getCurrentPrice());
            pstmt.setBoolean(6, stock.isBond());
            pstmt.executeUpdate();
        }
    }

    public List<Stock> getStocks(String username) throws SQLException {
        List<Stock> stocks = new ArrayList<>();
        String query = "SELECT * FROM stocks WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                stocks.add(new Stock(
                    rs.getString("ticker"),
                    rs.getInt("quantity"),
                    rs.getDouble("purchase_price"),
                    rs.getDouble("current_price"),
                    rs.getBoolean("is_bond")
                ));
            }
        }
        return stocks;
    }

    public boolean removeStock(String username, String ticker) throws SQLException {
        String query = "DELETE FROM stocks WHERE username = ? AND ticker = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, ticker);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    public boolean updateStock(String username, Stock stock) throws SQLException {
        String query = "UPDATE stocks SET quantity = ?, purchase_price = ?, current_price = ?, is_bond = ? WHERE username = ? AND ticker = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, stock.getQuantity());
            pstmt.setDouble(2, stock.getPurchasePrice());
            pstmt.setDouble(3, stock.getCurrentPrice());
            pstmt.setBoolean(4, stock.isBond());
            pstmt.setString(5, username);
            pstmt.setString(6, stock.getTicker());
            int affected = pstmt.executeUpdate();
            return affected > 0;
        }
    }

    // Metodi per le transazioni
    public void addTransaction(String username, Transaction tx) throws SQLException {
        String query = "INSERT INTO transactions (username, type, ticker, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, tx.getType());
            pstmt.setString(3, tx.getTicker());
            pstmt.setInt(4, tx.getQuantity());
            pstmt.setDouble(5, tx.getPrice());
            pstmt.executeUpdate();
        }
    }

    public List<Transaction> getTransactions(String username) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE username = ? ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getString("type"),
                    rs.getString("ticker"),
                    rs.getInt("quantity"),
                    rs.getDouble("price")
                ));
            }
        }
        return transactions;
    }

    public void reconnect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    // Add proper cleanup method
    public void cleanup() {
        try {
            close();
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
