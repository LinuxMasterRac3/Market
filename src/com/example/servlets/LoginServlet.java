package com.example.servlets;

import com.example.UserManager;
import com.example.SessionManager;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        System.out.println("Login attempt for: " + username);

        try {
            if (userManager.authenticate(username, password)) {
                SessionManager.createSession(request, username);
                // Include redirect URL in response
                String jsonResponse = "{\"success\":true,\"message\":\"Login successful\",\"redirect\":\"/portfolio.html\"}";
                response.getWriter().write(jsonResponse);
                System.out.println("Login successful, redirecting to: /portfolio.html");
            } else {
                String jsonResponse = "{\"success\":false,\"message\":\"Invalid credentials\"}";
                response.getWriter().write(jsonResponse);
                System.out.println("Login failed: " + jsonResponse);
            }
        } catch (SQLException e) {
            String jsonResponse = "{\"success\":false,\"message\":\"Database error\"}";
            response.getWriter().write(jsonResponse);
            System.err.println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
