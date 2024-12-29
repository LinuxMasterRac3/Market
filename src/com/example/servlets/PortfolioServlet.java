package com.example.servlets;

import com.example.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

public class PortfolioServlet extends HttpServlet {
    private PortfolioManager portfolioManager;
    private UserManager userManager;

    public PortfolioServlet() {
        // Default constructor needed for servlet instantiation
    }

    public PortfolioServlet(PortfolioManager portfolioManager) {
        this.portfolioManager = portfolioManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setPortfolioManager(PortfolioManager portfolioManager) {
        this.portfolioManager = portfolioManager;
    }

    public void init() {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            portfolioManager = new PortfolioManager(dbManager);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DatabaseManager", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        String username = (String) session.getAttribute("username");
        Portfolio portfolio = portfolioManager.getPortfolio(username);
        String portfolioJson = portfolio.toJsonString();
        response.setContentType("application/json");
        response.getWriter().write(portfolioJson);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        String username = (String) session.getAttribute("username");
        String action = request.getParameter("action");
        String ticker = request.getParameter("ticker");

        if (action == null || ticker == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }

        try {
            switch (action) {
                case "add":
                    int quantity = Integer.parseInt(request.getParameter("quantity"));
                    double purchasePrice = Double.parseDouble(request.getParameter("purchasePrice"));
                    boolean isBond = Boolean.parseBoolean(request.getParameter("isBond"));
                    Stock stock = new Stock(ticker, quantity, purchasePrice, isBond);
                    portfolioManager.addStock(username, stock);
                    break;

                case "remove":
                    boolean success = portfolioManager.removeStock(username, ticker);
                    if (!success) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Stock not found");
                        return;
                    }
                    break;

                case "modify":
                    int newQuantity = Integer.parseInt(request.getParameter("quantity"));
                    double newPrice = Double.parseDouble(request.getParameter("purchasePrice"));
                    boolean newIsBond = Boolean.parseBoolean(request.getParameter("isBond"));
                    success = portfolioManager.modifyStock(username, ticker, newQuantity, newPrice, newIsBond);
                    if (!success) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Stock not found");
                        return;
                    }
                    break;

                case "update":
                    portfolioManager.updatePrices(username);
                    break;

                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                    return;
            }
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true}");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
        }
    }
}
