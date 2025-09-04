package com.thales.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.concurrent.Task;

public class NetworkController {
    private ServerController controller;

    private PrintWriter out;
    private BufferedReader in;

    public void create(int serverPort, ServerController controller){
        this.controller = controller;
        Task<Void> connectTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                listen(serverPort);
                return null;
            }
        };
        new Thread(connectTask).start();
    }

    private void listen(int serverPort){
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {

            System.out.println("Aguardando conexao");
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Conectou!");

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    handleMessage(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
    
    }

    private void handleMessage(String message){
        System.out.println("Received: " + message);
        controller.handleMessage(message);
    }

    public void sendMessage(String message){
        if(out == null){
            return;
        }
        System.out.println("Send: " + message);
        out.println(message);
    }
}
