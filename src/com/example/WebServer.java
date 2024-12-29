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
            
            ServletContextHandler apiContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
            apiContext.setContextPath("/api");
            apiContext.getSessionHandler().setMaxInactiveInterval(1800); // 30 minutes in seconds
            
            // Add health check servlet
            apiContext.addServlet(new ServletHolder(new javax.servlet.http.HttpServlet() {
                @Override
                protected void doGet(javax.servlet.http.HttpServletRequest req, 
                                   javax.servlet.http.HttpServletResponse resp) 
                        throws javax.servlet.ServletException, java.io.IOException {
                    resp.setStatus(200);
                    resp.getWriter().write("OK");
                }
            }), "/_ah/health");
            
            // Configure LoginServlet with UserManager
            LoginServlet loginServlet = new LoginServlet();
            loginServlet.setUserManager(userManager);
            apiContext.addServlet(new ServletHolder(loginServlet), "/login");

            // Configure RegisterServlet
            RegisterServlet registerServlet = new RegisterServlet();
            registerServlet.setUserManager(userManager);
            apiContext.addServlet(new ServletHolder(registerServlet), "/register");

            // Configure CheckSessionServlet
            CheckSessionServlet checkSessionServlet = new CheckSessionServlet();
            apiContext.addServlet(new ServletHolder(checkSessionServlet), "/checkSession");
            
            // Configure PortfolioServlet with both managers and all mappings
            PortfolioServlet portfolioServlet = new PortfolioServlet(portfolioManager);
            portfolioServlet.setUserManager(userManager);
            ServletHolder portfolioHolder = new ServletHolder(portfolioServlet);
            apiContext.addServlet(portfolioHolder, "/portfolio/*");  // Changed to catch all portfolio endpoints
            
            // Configure LogoutServlet
            LogoutServlet logoutServlet = new LogoutServlet();
            apiContext.addServlet(new ServletHolder(logoutServlet), "/logout");
            
            // Create the ResourceHandler for static files
            ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setDirectoriesListed(false);
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});
            resourceHandler.setResourceBase("web");
            
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{resourceHandler, apiContext});
            
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
