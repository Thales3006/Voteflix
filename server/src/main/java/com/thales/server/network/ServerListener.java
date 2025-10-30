package com.thales.server.network;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
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
        serverService.log("Server Online!");
        try {
            String ip = getLocalAddress();
            serverService.log("IP: " + ip + " Port: " + port);
        } catch (Exception e) {
            serverService.log("Port: " + port);
        }

        new Thread(() -> listen()).start();
    }

    private String getLocalAddress() {
        try (DatagramSocket sock = new DatagramSocket()) {
            sock.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress local = sock.getLocalAddress();
            if (local instanceof Inet4Address && !local.isLoopbackAddress()) {
                return local.getHostAddress();
            }
        } catch (Exception ignored) {
        }
        try {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while (ifs.hasMoreElements()) {
                NetworkInterface nif = ifs.nextElement();
                if (!nif.isUp() || nif.isLoopback() || nif.isVirtual()) continue;
                Enumeration<InetAddress> addrs = nif.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress() && !addr.isLinkLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private void listen(){
        while (true) {
            try {
                Socket connection = listener.accept();
                ClientHandler socket = new ClientHandler(serverService, connection);
                clients.add(socket);
                socket.start();

                serverService.log("A client has connected");
            } catch (Exception e) {
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
