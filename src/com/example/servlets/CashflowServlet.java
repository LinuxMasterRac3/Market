package com.example.servlets;

import com.example.UserManager;
import com.example.DatabaseManager;
import com.example.CashflowTransaction;
import com.example.SessionManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

public class CashflowServlet extends HttpServlet {
    private UserManager userManager;
    private final DatabaseManager dbManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public CashflowServlet() throws SQLException {
        this.dbManager = new DatabaseManager();
    }

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
        try {
            String username = SessionManager.getUserFromSession(request);
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            ObjectNode json = mapper.createObjectNode();
            double cashBalance = dbManager.getCashBalance(username);
            double investedCapital = 0.0; // TODO: Implement this
            
            json.put("cashBalance", cashBalance);
            json.put("investedCapital", investedCapital);
            json.put("totalCapital", cashBalance + investedCapital);
            json.put("lastUpdate", System.currentTimeMillis());

            response.setContentType("application/json");
            response.getWriter().write(json.toString());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error\"}");
        }
    }

    private void getTransactions(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            String username = SessionManager.getUserFromSession(request);
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            List<CashflowTransaction> transactions = dbManager.getCashflowTransactions(username);
            ObjectNode json = mapper.createObjectNode();
            ArrayNode txArray = json.putArray("transactions");
            for (CashflowTransaction tx : transactions) {
                txArray.add(tx.toJsonNode());
            }

            response.setContentType("application/json");
            response.getWriter().write(json.toString());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error\"}");
        }
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
        try {
            String username = SessionManager.getUserFromSession(request);
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            JsonNode body = mapper.readTree(request.getReader());
            CashflowTransaction tx = new CashflowTransaction(
                body.get("type").asText(),
                body.get("amount").asDouble(),
                body.get("description").asText()
            );

            dbManager.addCashflowTransaction(username, tx);

            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error\"}");
        }
    }
}
