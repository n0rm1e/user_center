package com.normie.user_center.service;

import com.normie.user_center.common.Message;
import com.normie.user_center.model.User;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.*;

@Service
public class ChatServer extends WebSocketServer {

    private static HashMap<Integer,WebSocket> clients = new HashMap<>();
    private static ChatServer server;
    public ChatServer() {
        super(new InetSocketAddress(8888));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Get from handshake:" + handshake.getResourceDescriptor());
        String message = handshake.getResourceDescriptor();
        String[] parts = message.split(",", 2);

        Integer recipientId = Integer.parseInt(parts[0].substring(1));

        clients.put(recipientId,conn);
        System.out.println("New connection from: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        System.out.println("recipientId : " + recipientId);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection to: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message: " + message);
        // 向连接池中除了自己以外的用户发送接收到的信息。
//        broadcast(message,conn);
        String[] split = message.split(":", 3);
        if(split.length != 3){
            return;
        }
        Integer senderId = Integer.parseInt(split[0]);
        String content = split[1];
        Integer recipientId = Integer.parseInt(split[2]);
        System.out.println(senderId + ": " + content + ": " + recipientId);
        // 遍历clients
        Iterator<Map.Entry<Integer, WebSocket>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, WebSocket> entry = iterator.next();
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        WebSocket sender = clients.get(senderId);
        sender.send(senderId + ": " + content);
    }

//    private void broadcast(String message, WebSocket sender) {
//        synchronized (clients) {
//            for (WebSocket client : clients) {
//                if (!client.equals(sender)) {
//                    client.send(message);
//                }
//            }
//        }
//    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("Error on connection: " + ex.getMessage());
    }

    @Override
    public void onStart() {
    }
    public boolean isOpen() {
        return server != null;
    }

    public static void startServer(User user, User toChatUser) {
        int port = 6048;
        if(server == null){
            server = new ChatServer();
            server.start();
            System.out.println("WebSocket Server started on port: " + port);
        }
        System.out.println(user.getUsername() + ": " + toChatUser.getUsername());
    }
}