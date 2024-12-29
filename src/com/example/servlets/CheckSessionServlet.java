package com.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/checkSession")
public class CheckSessionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        String username = SessionManager.getUserFromSession(request);

        if (username != null) {
            System.out.println("Valid session found for user: " + username);
            response.getWriter().write("{\"authenticated\":true,\"username\":\"" + username + "\"}");
        } else {
            System.out.println("No valid session found");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"authenticated\":false}");
        }
    }
}
