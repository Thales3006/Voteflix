package com.thales.server;

import com.thales.server.network.ServerListener;
import com.thales.server.service.ServerService;

public class Main {

    public static void main(String[] args) {
        int port = args.length > 0
            ? Integer.parseInt(args[0])
            : Integer.parseInt(System.getenv().getOrDefault("PORT", "20737"));

        ServerService service = new ServerService();
        ServerListener listener = new ServerListener(service, port);

        Runtime.getRuntime().addShutdownHook(new Thread(listener::close));
    }
}
