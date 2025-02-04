package com.example.servlets;

import com.example.UserManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CashflowServlet extends HttpServlet {
    private UserManager userManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) pathInfo = "/";

        switch (pathInfo) {
            case "/summary":
                getSummary(request, response);
                break;
            case "/transactions":
                getTransactions(request, response);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void getSummary(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        ObjectNode json = mapper.createObjectNode();
        // Per ora restituiamo dati di esempio
        json.put("cashBalance", 1000.00);
        json.put("investedCapital", 5000.00);
        json.put("totalCapital", 6000.00);
        json.put("lastUpdate", System.currentTimeMillis());

        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    private void getTransactions(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        ObjectNode json = mapper.createObjectNode();
        ArrayNode transactions = json.putArray("transactions");
        
        // Per ora restituiamo dati di esempio
        ObjectNode transaction = mapper.createObjectNode();
        transaction.put("date", System.currentTimeMillis());
        transaction.put("type", "INCOME");
        transaction.put("amount", 1000.00);
        transaction.put("description", "Stipendio");
        transactions.add(transaction);

        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = request.getPathInfo();
        if ("/add".equals(pathInfo)) {
            addTransaction(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void addTransaction(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        ObjectNode json = mapper.createObjectNode();
        // Per ora solo simuliamo l'aggiunta
        json.put("success", true);
        json.put("message", "Transaction added successfully");

        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }
}
