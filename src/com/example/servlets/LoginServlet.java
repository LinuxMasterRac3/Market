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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        System.out.println("Login attempt for: " + username);

        try {
            if (userManager.authenticate(username, password)) {
                SessionManager.createSession(request, username);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":true,\"message\":\"Login successful\"}");
                System.out.println("Login successful for user: " + username);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\":false,\"message\":\"Invalid credentials\"}");
                System.out.println("Login failed for user: " + username);
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
            System.err.println("Database error during login: " + e.getMessage());
        }
    }
}
