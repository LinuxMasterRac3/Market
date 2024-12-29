package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class YahooFinanceAPI {
    private static final String API_KEY = "HgDrf9tJOqfYZC88GahTLGHEoBZzIQhs"; // Sostituisci con la tua API key
    private static final ObjectMapper mapper = new ObjectMapper();

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
                return parsePriceFromResponse(response.body());
            } else {
                System.err.println("Errore nella risposta dell'API per " + ticker + ": " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Eccezione durante la richiesta per " + ticker + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    private static double parsePriceFromResponse(String responseBody) throws IOException {
        JsonNode root = mapper.readTree(responseBody);
        if (root.isArray() && root.size() > 0) {
            JsonNode firstQuote = root.get(0);
            if (firstQuote.has("price")) {
                return firstQuote.get("price").asDouble();
            }
        }
        throw new IOException("Price not found in response");
    }
}