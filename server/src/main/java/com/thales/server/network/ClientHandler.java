package com.thales.server.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.thales.server.service.ServerService;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientHandler extends Thread {
    
    private ServerService serverService;
    private Socket connection;
    private PrintWriter out;
    private BufferedReader in;
    private BooleanProperty running;
    private String username;

    public ClientHandler(ServerService serverService, Socket connection){
        running = new SimpleBooleanProperty(false);
        this.connection = connection;
        this.serverService = serverService;
        this.username = "";
    }

    public void run(){
        try{
            //connection.setSoTimeout(2000);
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new PrintWriter(connection.getOutputStream(), true);
            running.set(true);

            String line;
            while (running.get() && (line = in.readLine()) != null) {
                receiveMessage(line);
            }
        } catch(Exception e) {
            System.err.println(e);
        } finally {
            close();
        }
    } 

    private void receiveMessage(String message){
        serverService.log("Received: " + message);
        serverService.handleMessage(message, this);
    }

    public void sendMessage(String message){
        if(out == null){
            return;
        }
        serverService.log("Send: " + message);
        out.println(message);
    }

    public void close() {
        running.set(false);
        try {
            if (connection != null && !connection.isClosed()){ 
                connection.close();
                serverService.log("A client has disconnected");
            }
            if (in != null){
                in.close();
            }
            if (out != null){
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
