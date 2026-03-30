package com.collabeditor.server;

import jakarta.websocket.Session;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void register(Session session) {
        sessions.put(session.getId(), session);
    }

    public void unregister(Session session) {
        sessions.remove(session.getId());
    }

    public Collection<Session> allSessions() {
        return sessions.values();
    }
}
