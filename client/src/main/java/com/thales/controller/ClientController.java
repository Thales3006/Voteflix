package com.thales.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.Data;

@Data
public class ClientController {
    private static final int PORT = 20616;
    private static ClientController instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running;

    private ClientController(String serverIP,int serverPort){
        this.running = false;
        this.connect(serverIP, serverPort);
    }

    public static ClientController getInstance(){
        if(instance == null){
            instance = new ClientController("localhost", PORT);
        }
        return instance;
    }

    private void connect(String serverIP, int serverPort){
        System.out.println("Trying to connect to server");
        new Thread(()->{
            try{
                this.socket = new Socket(serverIP, serverPort);
                //this.socket.setSoTimeout(2000);
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.running = true;
                System.out.println("You are connected");

                String line;
                while (this.running && (line = this.in.readLine()) != null) {
                    handleMessage(line);
                }
            } catch(Exception e){
                System.err.println(e);
            } finally {
                close();
            }
        }).start();
    }

    private void handleMessage(String message){
        System.out.println("Received: " + message);
        AppController.getInstance().handleMessage(message);
    }

    public void sendMessage(String message){
        if(this.out == null){
            return;
        }
        System.out.println("Send: " + message);
        out.println(message);
    }

    public void close() {
        this.running = false;
        try {
            if (this.socket != null && !socket.isClosed()){ 
                this.socket.close();
                System.out.println("You were disconnected");
            }
            if (this.in != null){
                this.in.close();
            }
            if (this.out != null){
                this.out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
