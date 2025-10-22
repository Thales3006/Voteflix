package com.thales.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;

@Data
public class ClientSocket {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BooleanProperty running;

    public ClientSocket(){
        running = new SimpleBooleanProperty(false);
    }

    public void connect(String serverIP, int serverPort) throws IOException, UnknownHostException {
        System.out.println("Trying to connect to server");
        if (running.get()) {
            throw new IOException("A socket connection is still on");
        }
        
        socket = new Socket(serverIP, serverPort);
        socket.setSoTimeout(2000);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        running.set(true);
        System.out.println("You are connected");
    }

    public void sendMessage(String message) throws IOException{
        if(out == null){
            throw new IOException("Socket not connected");
        }
        System.out.println("Send: " + message);
        out.println(message);
    }

    public String waitMessage() throws IOException {
        String message = in.readLine();
        if(message == null){
            close();
            throw new IOException("Received Null, Connection was Closed");
        }
        System.out.println("Received: " + message);
        return message;
    }

    public void close() {
        running.set(false);
        try {
            if (socket != null && !socket.isClosed()){ 
                socket.close();
                System.out.println("You were disconnected");
            }
            if (in != null){
                in.close();
            }
            if (out != null){
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
