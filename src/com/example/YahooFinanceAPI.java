package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class YahooFinanceAPI {
    private static final String API_KEY = "HgDrf9tJOqfYZC88GahTLGHEoBZzIQhs"; // Replace with your actual API key

    public static double getCurrentPrice(String ticker) {
        try {
            String urlString = "https://financialmodelingprep.com/api/v3/quote/"
                + URLEncoder.encode(ticker, "UTF-8")
                + "?apikey=" + API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                if (jsonArray.length() > 0) {
                    JSONObject quote = jsonArray.getJSONObject(0);
                    double currentPrice = quote.getDouble("price");
                    System.out.println("Prezzo corrente per " + ticker + ": " + currentPrice);
                    return currentPrice;
                } else {
                    System.err.println("Nessun dato trovato per il ticker: " + ticker);
                }
            } else {
                System.err.println("Errore nella risposta dell'API per " + ticker + ": " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Eccezione durante la richiesta per " + ticker + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    // Metodo di test per il titolo AAPL
//    public static void testApi() {
//        System.out.println("Test dell'API per AAPL");
//        double price = getCurrentPrice("AAPL");
//        if (price > 0) {
//            System.out.println("Prezzo AAPL: " + price);
//        } else {
//            System.err.println("Errore nel recupero del prezzo di AAPL");
//        }
//    }
}