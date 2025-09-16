package com.thales.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

@Data
public class ClientSocket {
    private static ClientSocket instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BooleanProperty running;
    private StringProperty lastMessage;

    private ClientSocket(){
        running = new SimpleBooleanProperty(false);
        lastMessage = new SimpleStringProperty();
    }

    public static ClientSocket getInstance(){
        if(instance == null){
            instance = new ClientSocket();
        }
        return instance;
    }

    public void connect(String serverIP, int serverPort) throws IOException {
        System.out.println("Trying to connect to server");
        if (running.get()) {
            throw new IOException("A socket connection is still on");
        }
        try{
            socket = new Socket(serverIP, serverPort);
            //socket.setSoTimeout(2000);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            running.set(true);
            System.out.println("You are connected");
        } catch(Exception e){
            System.err.println(e);
        }

        new Thread(()->{
            try{
                String line;
                while (running.get() && (line = in.readLine()) != null) {
                    handleMessage(line);
                }
            } catch(SocketException e){
                if(!running.get()){
                    return;
                }
                System.err.println(e);
            } catch(Exception e){
                System.err.println(e);
            } finally {
                close();
            }
        }).start();
    }

    private void handleMessage(String message){
        System.out.println("Received: " + message);
        Platform.runLater(() -> lastMessage.set(message));
    }

    public void sendMessage(String message) throws IOException{
        if(out == null){
            throw new IOException("Socket not connected");
        }
        System.out.println("Send: " + message);
        out.println(message);
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
