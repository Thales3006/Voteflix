package com.thales.controller;

import java.net.ServerSocket;
import java.net.Socket;

import javafx.concurrent.Task;

public class ServerController {
    private static final int PORT = 20616;
    private static ServerController instance;

    private ServerSocket listener;

    private ServerController(int serverPort){
        Task<Void> connectTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                listen(serverPort);
                return null;
            }
        };
        new Thread(connectTask).start();
    }

    public static ServerController getInstance(){
        if(instance == null){
            instance = new ServerController(PORT);
        }
        return instance;
    }

    private void listen(int serverPort){
        try {
            listener = new ServerSocket(serverPort);
            System.out.println("Server Online!");

            while (true) {
                try {
                    Socket connection = listener.accept();
                    System.out.println("A client has connected");

                    SocketController socket = new SocketController(connection);
                    socket.start();
                } catch (Exception e) {
                    System.out.println("Server Offline!");
                    return;
                } 
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            if (this.listener != null && !listener.isClosed()) {
                listener.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
