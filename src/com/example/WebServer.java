package com.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import java.sql.SQLException;
import java.net.InetSocketAddress;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.component.LifeCycle;

// Import servlets from correct package
import com.example.servlets.*;

public class WebServer {
    public static void main(String[] args) {
        try {
            // Get port from environment variable first, then system property
            String portEnv = System.getenv("PORT");
            int port = portEnv != null ? Integer.parseInt(portEnv) : 8080;
            
            System.out.println("Environment PORT: " + portEnv);
            System.out.println("Using port: " + port);
            
            DatabaseManager dbManager = new DatabaseManager();
            PortfolioManager portfolioManager = new PortfolioManager(dbManager);
            UserManager userManager = new UserManager(dbManager);
            
            Server server = new Server();
            
            // Configure the connector with explicit host and port
            ServerConnector connector = new ServerConnector(server);
            connector.setHost("0.0.0.0");  // Listen on all interfaces
            connector.setPort(port);
            server.addConnector(connector);
            
            // Create resource handler for static files first
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(false);
            resourceHandler.setWelcomeFiles(new String[]{"registration.html"});
            resourceHandler.setResourceBase("web");

            // Create servlet context handler
            ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletHandler.setContextPath("/");

            // Add health check servlet with more detailed status
            servletHandler.addServlet(new ServletHolder(new javax.servlet.http.HttpServlet() {
                @Override
                protected void doGet(javax.servlet.http.HttpServletRequest req, 
                                   javax.servlet.http.HttpServletResponse resp) 
                        throws javax.servlet.ServletException, java.io.IOException {
                    try {
                        // Test database connection
                        if (dbManager != null && dbManager.testConnection()) {
                            resp.setStatus(200);
                            resp.getWriter().write("OK - Database Connected");
                        } else {
                            resp.setStatus(500);
                            resp.getWriter().write("ERROR - Database Not Connected");
                        }
                    } catch (Exception e) {
                        resp.setStatus(500);
                        resp.getWriter().write("ERROR - " + e.getMessage());
                    }
                }
            }), "/_ah/health");
            
            // Configure LoginServlet with UserManager
            LoginServlet loginServlet = new LoginServlet();
            loginServlet.setUserManager(userManager);
            servletHandler.addServlet(new ServletHolder(loginServlet), "/login");

            // Configure RegisterServlet
            RegisterServlet registerServlet = new RegisterServlet();
            registerServlet.setUserManager(userManager);
            servletHandler.addServlet(new ServletHolder(registerServlet), "/register");

            // Configure CheckSessionServlet
            CheckSessionServlet checkSessionServlet = new CheckSessionServlet();
            servletHandler.addServlet(new ServletHolder(checkSessionServlet), "/checkSession");
            
            // Configure PortfolioServlet with both managers and all mappings
            PortfolioServlet portfolioServlet = new PortfolioServlet(portfolioManager);
            portfolioServlet.setUserManager(userManager);
            ServletHolder portfolioHolder = new ServletHolder(portfolioServlet);
            servletHandler.addServlet(portfolioHolder, "/portfolio/*");  // Changed to catch all portfolio endpoints
            
            // Configure LogoutServlet
            LogoutServlet logoutServlet = new LogoutServlet();
            servletHandler.addServlet(new ServletHolder(logoutServlet), "/logout");
            
            // Aggiungi CashflowServlet
            CashflowServlet cashflowServlet = new CashflowServlet();
            cashflowServlet.setUserManager(userManager);
            ServletHolder cashflowHolder = new ServletHolder(cashflowServlet);
            servletHandler.addServlet(cashflowHolder, "/cashflow/*");
            
            // Configure handler order - static files first, then servlets
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{resourceHandler, servletHandler});
            
            server.setHandler(handlers);
            
            // Add startup complete message
            server.addLifeCycleListener(new LifeCycle.Listener() {
                @Override
                public void lifeCycleStarting(LifeCycle event) {}

                @Override
                public void lifeCycleStarted(LifeCycle event) {
                    System.out.println("Server is now started and listening on port " + port);
                }

                @Override
                public void lifeCycleStopping(LifeCycle event) {}

                @Override
                public void lifeCycleStopped(LifeCycle event) {}

                @Override
                public void lifeCycleFailure(LifeCycle event, Throwable cause) {}
            });

            try {
                System.out.println("Starting server initialization...");
                server.start();
                System.out.println("Server started successfully on port " + port);
                server.join();
            } catch (Exception e) {
                System.err.println("Server startup failed: " + e.getMessage());
                e.printStackTrace();
                System.exit(1); // Ensure the container exits on failure
            }
        } catch (Exception e) {
            System.err.println("Critical initialization error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
