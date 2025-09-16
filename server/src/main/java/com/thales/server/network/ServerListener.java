package com.thales.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.thales.server.service.ServerService;

import lombok.Data;

@Data
public class ServerListener {

    private ServerService serverService;
    private ServerSocket listener;
    private List<ClientHandler> clients;

    public ServerListener(ServerService serverService, int port){
        this.serverService = serverService;
        clients = new ArrayList<>();

        try {
            listener = new ServerSocket(port);
        } catch (IOException e){
            System.err.println(e);
            System.exit(1);
        }
        System.out.println("Server Online!");
        serverService.log("Server Online!");

        new Thread(() -> listen()).start();
    }

    private void listen(){
        while (true) {
            try {
                Socket connection = listener.accept();
                ClientHandler socket = new ClientHandler(serverService, connection);
                clients.add(socket);
                socket.start();

                System.out.println("A client has connected");
                serverService.log("A client has connected");
            } catch (Exception e) {
                System.out.println("Server Offline!");
                serverService.log("Server Offline!");
                return;
            } 
        }
    }

    public void close(){
        try {
            if (listener != null && !listener.isClosed()) {
                listener.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
