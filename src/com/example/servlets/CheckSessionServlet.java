package com.example.servlets;

import com.example.SessionManager;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/checkSession")
public class CheckSessionServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");

        HttpSession session = request.getSession(false);
        String username = SessionManager.getUserFromSession(request);
        String currentPage = request.getHeader("Referer");
        
        System.out.println("Session check for user: " + username + " from page: " + currentPage);

        if (username != null && session != null) {
            // Valid session exists
            response.getWriter().write(String.format(
                "{\"authenticated\": true, \"username\": \"%s\", \"currentPage\": \"%s\"}",
                username,
                currentPage != null ? currentPage : ""
            ));
            System.out.println("Valid session found for: " + username);
        } else {
            // No valid session
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"authenticated\": false}");
            System.out.println("No valid session found");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
