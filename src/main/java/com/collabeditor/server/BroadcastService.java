package com.collabeditor.server;

import jakarta.websocket.Session;

public class BroadcastService {

    public void broadcastExcept(SessionRegistry registry, String payload, Session excluded) {
        for (Session session : registry.allSessions()) {
            if (!session.isOpen()) {
                continue;
            }
            if (excluded != null && session.getId().equals(excluded.getId())) {
                continue;
            }
            session.getAsyncRemote().sendText(payload);
        }
    }
}
