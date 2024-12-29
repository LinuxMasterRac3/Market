package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PortfolioManager portfolioManager;
    private UserManager userManager; // Assicurati che UserManager sia implementato per utilizzare DatabaseManager
    private String currentUser = null; // Utente corrente

    public ClientHandler(Socket socket, PortfolioManager portfolioManager, UserManager userManager) {
        this.clientSocket = socket;
        this.portfolioManager = portfolioManager;
        this.userManager = userManager;
    }

    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        ) {
            String requestLine;
            while ((requestLine = in.readLine()) != null) {
                // Parsing della richiesta
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length < 2) {
                    sendResponse(out, 400, "Bad Request", "{\"error\": \"Bad Request\"}");
                    continue;
                }
                String method = requestParts[0];
                String path = requestParts[1];

                // Consuma le intestazioni
                String line;
                int contentLength = 0;
                while (!(line = in.readLine()).isEmpty()) {
                    if (line.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
                    }
                }

                // Lettura del corpo della richiesta se POST
                String body = "";
                if (method.equals("POST")) {
                    char[] bodyChars = new char[contentLength];
                    int totalRead = 0;
                    while (totalRead < contentLength) {
                        int read = in.read(bodyChars, totalRead, contentLength - totalRead);
                        if (read == -1) break;
                        totalRead += read;
                    }
                    body = new String(bodyChars);
                }

                // Parsing dei parametri
                Map<String, String> params = parseParameters(body);

                // Gestione delle richieste
                if (method.equals("GET") && path.equals("/portfolio")) {
                    handleGetPortfolio(out);
                } else if (method.equals("GET") && path.equals("/transactionHistory")) {
                    handleTransactionHistory(out);
                } else if (method.equals("POST") && path.equals("/login")) {
                    handleLogin(params, out);
                } else if (method.equals("POST") && path.equals("/register")) {
                    handleRegister(params, out);
                } else if (method.equals("POST") && path.equals("/addStock")) {
                    handleAddStock(params, out);
                } else if (method.equals("POST") && path.equals("/removeStock")) {
                    handleRemoveStock(params, out);
                } else if (method.equals("POST") && path.equals("/modifyStock")) {
                    handleModifyStock(params, out);
                } else if (method.equals("POST") && path.equals("/updatePrices")) {
                    handleUpdatePrices(out);
                } else if (method.equals("GET")) {
                    // Se la richiesta è "/", reindirizza a "/index.html".
                    if (path.equals("/")) {
                        path = "/index.html";
                    }

                    // Tenta di servire un file statico dalla cartella "web"
                    serveStaticFile(path, out);
                } else {
                    // Handle unsupported methods
                    sendResponse(out, 405, "Method Not Allowed", "{\"error\": \"Method Not Allowed\"}");
                    System.out.println("Metodo non supportato: " + method + " " + path);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella gestione del client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLogin(Map<String, String> params, BufferedWriter out) throws IOException {
        String username = params.get("username");
        String password = params.get("password");

        try {
            if (userManager.authenticate(username, password)) {
                currentUser = username;
                userManager.setUserLoggedIn(username, true);
                sendResponse(out, 200, "OK", "{\"success\":true,\"message\":\"Login successful\"}");
                System.out.println("Utente autenticato: " + username);
            } else {
                sendResponse(out, 401, "Unauthorized", "{\"success\":false,\"message\":\"Invalid credentials\"}");
                System.out.println("Tentativo di login fallito per utente: " + username);
            }
        } catch (SQLException e) {
            sendResponse(out, 500, "Internal Server Error", "{\"error\":\"" + e.getMessage() + "\"}");
            System.err.println("Errore nell'autenticazione: " + e.getMessage());
        }
    }

    private void handleRegister(Map<String, String> params, BufferedWriter out) throws IOException {
        String username = params.get("username");
        String password = params.get("password");

        try {
            if (userManager.register(username, password)) {
                sendResponse(out, 200, "OK", "{\"success\":true,\"message\":\"Registration successful\"}");
            } else {
                sendResponse(out, 409, "Conflict", "{\"success\":false,\"message\":\"User already exists\"}");
            }
        } catch (SQLException e) {
            sendResponse(out, 500, "Internal Server Error", "{\"error\":\"" + e.getMessage() + "\"}");
            System.err.println("Errore nella registrazione: " + e.getMessage());
        }
    }

    private void handleGetPortfolio(BufferedWriter out) throws IOException {
        if (currentUser == null) {
            String errorJson = "{\"error\": \"Unauthorized - Please login first\"}";
            sendResponse(out, 401, "Unauthorized", errorJson);
            return;
        }

        Portfolio portfolio = portfolioManager.getPortfolio(currentUser);
        String portfolioJson = portfolio.toJsonString(); // Implementa questo metodo nel modello Portfolio
        sendResponse(out, 200, "OK", portfolioJson);
    }

    private void handleTransactionHistory(BufferedWriter out) throws IOException {
        if (currentUser == null) {
            sendResponse(out, 401, "Unauthorized", "{\"error\": \"Unauthorized - Please login first\"}");
            return;
        }

        try {
            List<Transaction> transactions = portfolioManager.getTransactions(currentUser);
            StringBuilder jsonArray = new StringBuilder("[");
            for (int i = 0; i < transactions.size(); i++) {
                if (i > 0) jsonArray.append(",");
                jsonArray.append(transactions.get(i).toJsonString());
            }
            jsonArray.append("]");
            sendResponse(out, 200, "OK", jsonArray.toString());
        } catch (SQLException e) {
            sendResponse(out, 500, "Internal Server Error", "{\"error\": \"Database error\"}");
            System.err.println("Error in handleTransactionHistory: " + e.getMessage());
        }
    }

    private void handleAddStock(Map<String, String> params, BufferedWriter out) throws IOException {
        if (currentUser == null) {
            sendResponse(out, 401, "Unauthorized", "{\"error\": \"Please login first\"}");
            System.out.println("Tentativo di aggiungere stock senza autenticazione.");
            return;
        }

        String ticker = params.get("ticker");
        int quantity;
        double purchasePrice;
        boolean isBond = Boolean.parseBoolean(params.get("isBond"));

        try {
            quantity = Integer.parseInt(params.get("quantity"));
            purchasePrice = Double.parseDouble(params.get("purchasePrice"));
        } catch (NumberFormatException e) {
            sendResponse(out, 400, "Bad Request", "{\"error\": \"Invalid number format\"}");
            System.out.println("Formato numerico non valido nei parametri di addStock.");
            return;
        }

        Stock stock = new Stock(ticker, quantity, purchasePrice, isBond);
        portfolioManager.addStock(currentUser, stock);
        sendResponse(out, 200, "OK", "{\"success\":true,\"message\":\"Stock aggiunta con successo\"}");
        System.out.println("Stock aggiunta per utente: " + currentUser);
    }

    private void handleRemoveStock(Map<String, String> params, BufferedWriter out) throws IOException {
        if (currentUser == null) {
            sendResponse(out, 401, "Unauthorized", "{\"error\": \"Please login first\"}");
            System.out.println("Tentativo di rimuovere stock senza autenticazione.");
            return;
        }

        String ticker = params.get("ticker");
        if (ticker == null || ticker.isEmpty()) {
            sendResponse(out, 400, "Bad Request", "{\"error\": \"Ticker mancante\"}");
            System.out.println("Ticker mancante nella richiesta di removeStock.");
            return;
        }

        boolean success = portfolioManager.removeStock(currentUser, ticker);
        if (success) {
            sendResponse(out, 200, "OK", "{\"success\":true,\"message\":\"Stock rimossa con successo\"}");
            System.out.println("Stock rimossa per utente: " + currentUser + " - Ticker: " + ticker);
        } else {
            sendResponse(out, 404, "Not Found", "{\"success\":false,\"message\":\"Stock non trovata\"}");
            System.out.println("Stock non trovata per utente: " + currentUser + " - Ticker: " + ticker);
        }
    }

    private void handleModifyStock(Map<String, String> params, BufferedWriter out) throws IOException {
        if (currentUser == null) {
            sendResponse(out, 401, "Unauthorized", "{\"error\": \"Please login first\"}");
            System.out.println("Tentativo di modificare stock senza autenticazione.");
            return;
        }

        String ticker = params.get("ticker");
        int newQuantity;
        double newPurchasePrice;
        boolean isBond = Boolean.parseBoolean(params.get("isBond"));

        try {
            newQuantity = Integer.parseInt(params.get("quantity"));
            newPurchasePrice = Double.parseDouble(params.get("purchasePrice"));
        } catch (NumberFormatException e) {
            sendResponse(out, 400, "Bad Request", "{\"error\": \"Invalid number format\"}");
            System.out.println("Formato numerico non valido nei parametri di modifyStock.");
            return;
        }

        boolean success = portfolioManager.modifyStock(currentUser, ticker, newQuantity, newPurchasePrice, isBond);
        if (success) {
            sendResponse(out, 200, "OK", "{\"success\":true,\"message\":\"Stock modificata con successo\"}");
            System.out.println("Stock modificata per utente: " + currentUser + " - Ticker: " + ticker);
        } else {
            sendResponse(out, 404, "Not Found", "{\"success\":false,\"message\":\"Stock non trovata\"}");
            System.out.println("Stock non trovata per modifica per utente: " + currentUser + " - Ticker: " + ticker);
        }
    }

    private void handleUpdatePrices(BufferedWriter out) throws IOException {
        if (currentUser == null) {
            sendResponse(out, 401, "Unauthorized", "{\"error\": \"Please login first\"}");
            System.out.println("Tentativo di aggiornare prezzi senza autenticazione.");
            return;
        }

        portfolioManager.updatePrices(currentUser);
        sendResponse(out, 200, "OK", "{\"success\":true,\"message\":\"Prezzi aggiornati con successo\"}");
        System.out.println("Prezzi aggiornati per utente: " + currentUser);
    }

    private Map<String, String> parseParameters(String body) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], "UTF-8");
            String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
            params.put(key, value);
        }
        return params;
    }

    private void sendResponse(BufferedWriter out, int statusCode, String statusText, String body) throws IOException {
        out.write("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.write("Access-Control-Allow-Origin: *\r\n");
        out.write("Content-Type: application/json\r\n");
        out.write("Content-Length: " + body.getBytes("UTF-8").length + "\r\n");
        out.write("\r\n");
        out.write(body);
        out.flush();
        System.out.println("Sending response: " + statusCode + " " + statusText);
        System.out.println("Response body: " + body);
    }

    private void serveStaticFile(String path, BufferedWriter out) throws IOException {
        if (path.equals("/")) {
            path = "/index.html";
        }

        String contentType;
        if (path.endsWith(".html")) contentType = "text/html";
        else if (path.endsWith(".css")) contentType = "text/css";
        else if (path.endsWith(".js")) contentType = "application/javascript";
        else contentType = "text/plain";

        try {
            String content = Files.readString(Path.of("web" + path));
            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: " + contentType + "; charset=UTF-8\r\n");
            out.write("Content-Length: " + content.getBytes("UTF-8").length + "\r\n");
            out.write("\r\n");
            out.write(content);
            out.flush();
        } catch (IOException e) {
            sendResponse(out, 404, "Not Found", "{\"error\": \"File not found\"}");
        }
    }
}