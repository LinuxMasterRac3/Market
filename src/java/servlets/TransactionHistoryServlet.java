// Se non utilizzi i servlet standard, puoi eliminare questo file per evitare conflitti.
package servlets;

import com.example.PortfolioManager;
import com.example.Transaction;
import org.json.JSONArray;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class TransactionHistoryServlet extends HttpServlet {
    private PortfolioManager portfolioManager;

    @Override
    public void init() throws ServletException {
        portfolioManager = new PortfolioManager();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ottenere l'username dall'utente loggato
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String username = (String) session.getAttribute("username");

        // Recuperare le transazioni
        List<Transaction> transactions = portfolioManager.getTransactions(username);

        // Convertire in JSON
        JSONArray jsonArray = new JSONArray();
        for (Transaction tx : transactions) {
            jsonArray.put(tx.toJson());
        }

        // Impostare la risposta
        response.setContentType("application/json");
        response.getWriter().write(jsonArray.toString());
    }
}
