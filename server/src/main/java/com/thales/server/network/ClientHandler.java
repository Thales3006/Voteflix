package com.thales.server.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

import com.thales.server.service.ServerService;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientHandler extends Thread {

    private ServerService serverService;
    private Socket connection;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(ServerService serverService, Socket connection){
        this.connection = connection;
        this.serverService = serverService;
    }

    public void run(){
        try{
            connection.setSoTimeout(2000);
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new PrintWriter(connection.getOutputStream(), true);

            String line = in.readLine();
            if (line != null) {
                receiveMessage(line);
            }
        } catch(Exception e) {
            System.err.println(e);
        } finally {
            close();
        }
    }

    private void receiveMessage(String message){
        serverService.log("Received:\n" + prettyJson(message));
        serverService.logMessage("Received", message);
        serverService.handleMessage(message, this);
    }

    public void sendMessage(String message){
        if(out == null){
            return;
        }
        serverService.log("Send:\n" + prettyJson(message));
        serverService.logMessage("Send", message);
        out.println(message);
    }

    private String prettyJson(String json) {
        try { return new JSONObject(json).toString(2); } catch (Exception e) { return json; }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
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
