// Se non utilizzi i servlet standard, puoi eliminare questo file per evitare conflitti.

package com.example.servlets;

import com.example.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.json.JSONArray;

public class TransactionHistoryServlet extends HttpServlet {
    private PortfolioManager portfolioManager;

    public void init() {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            portfolioManager = new PortfolioManager(dbManager);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DatabaseManager", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = request.getParameter("username");
            List<Transaction> transactions = portfolioManager.getTransactions(username);
            
            StringBuilder jsonArray = new StringBuilder("[");
            for (int i = 0; i < transactions.size(); i++) {
                if (i > 0) jsonArray.append(",");
                jsonArray.append(transactions.get(i).toJsonString());
            }
            jsonArray.append("]");
            
            response.setContentType("application/json");
            response.getWriter().write(jsonArray.toString());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}
