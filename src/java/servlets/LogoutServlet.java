package servlets;

import com.example.UserManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})  // Modifica qui
public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Accetta sia GET che POST
        response.setContentType("text/plain");
        System.out.println("LogoutServlet: service method called");
        
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                String username = (String) session.getAttribute("username");
                if (username != null) {
                    UserManager userManager = new UserManager();
                    userManager.logout(username);
                }
                session.invalidate();
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Logout successful");
            
        } catch (Exception e) {
            System.out.println("Errore durante il logout: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_OK); // Cambiato a OK
            response.getWriter().write("Logout completed");
        }
    }
}
