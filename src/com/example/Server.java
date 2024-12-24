package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 8080; // You can change the port number as needed

    public static void main(String[] args) {
        PortfolioManager portfolioManager = new PortfolioManager();
        UserManager userManager = new UserManager();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server avviato sulla porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connessione accettata da " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, portfolioManager, userManager);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Errore nel server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}