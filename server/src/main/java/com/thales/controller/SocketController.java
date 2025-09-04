package com.thales.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketController extends Thread {
    
    private Socket connection;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running;

    public SocketController(Socket connection){
        this.running = false;
        this.connection = connection;
    }

    public void run(){
        try{
            this.in = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
            this.out = new PrintWriter(this.connection.getOutputStream(), true);
            this.running = true;

            String line;
            while (this.running && (line = this.in.readLine()) != null) {
                handleMessage(line);
            }
        } catch(Exception e) {
            System.err.println(e);
        } finally {
            close();
        }
    } 

    private void handleMessage(String message){
        System.out.println("Received: " + message);
        AppController.getInstance().handleMessage(message);

        sendMessage(message.toUpperCase());
    }

    public void sendMessage(String message){
        if(this.out == null){
            return;
        }
        System.out.println("Send: " + message);
        this.out.println(message);
    }

    public void close() {
        this.running = false;
        try {
            if (this.in != null){
                this.in.close();
            }
            if (this.out != null){
                this.out.close();
            }
            if (this.connection != null && !this.connection.isClosed()){ 
                this.connection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
