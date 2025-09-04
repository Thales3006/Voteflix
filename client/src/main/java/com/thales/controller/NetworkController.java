package com.thales.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkController {
    private ClientController controller;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean running;

    public void connect(String serverIP, int serverPort, ClientController controller){
        this.controller = controller;
        new Thread(()->{
            try{
                socket = new Socket(serverIP, serverPort);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                running = true;

                String line;
                while (running && (line = in.readLine()) != null) {
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
        controller.handleMessage(message);
    }

    public void sendMessage(String message){
        if(out == null){
            return;
        }
        System.out.println("Send: " + message);
        out.println(message);
    }

    public void close() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
