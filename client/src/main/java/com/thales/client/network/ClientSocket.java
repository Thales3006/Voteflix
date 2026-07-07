package com.thales.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    public String sendAndReceive(String serverIP, int serverPort, String message) throws IOException {
        try (Socket socket = new Socket(serverIP, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            socket.setSoTimeout(2000);
            System.out.println("Send: " + message);
            out.println(message);

            String response = in.readLine();
            if (response == null) {
                throw new IOException("Connection closed before response was received");
            }
            System.out.println("Received: " + response);
            return response;
        }
    }
}
