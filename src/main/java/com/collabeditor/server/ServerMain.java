package com.collabeditor.server;

import org.glassfish.tyrus.server.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8025, "/ws", null, DocumentServer.class);
        try {
            server.start();
            System.out.println("Document server started at ws://localhost:8025/ws/document");
            System.out.println("Press Enter to stop...");
            System.in.read();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to start server", ex);
        } finally {
            server.stop();
        }
    }
}
