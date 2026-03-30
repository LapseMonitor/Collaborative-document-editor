package com.collabeditor.server;

import com.collabeditor.common.Operation;
import com.google.gson.Gson;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.List;

@ServerEndpoint("/document")
public class DocumentServer {
    private static final Gson GSON = new Gson();
    private static final DocumentState STATE = new DocumentState();
    private static final OperationTransformer TRANSFORMER = new OperationTransformer();
    private static final SessionRegistry SESSIONS = new SessionRegistry();
    private static final BroadcastService BROADCAST = new BroadcastService();

    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.register(session);
        SyncMessage sync = new SyncMessage("sync", STATE.getSnapshot(), STATE.getRevision(), null);
        session.getAsyncRemote().sendText(GSON.toJson(sync));
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.unregister(session);
    }

    @OnMessage
    public void onMessage(String json, Session session) {
        Operation incoming = GSON.fromJson(json, Operation.class);
        List<Operation> newer = STATE.operationsSince(incoming.getBaseRevision());
        Operation transformed = TRANSFORMER.transform(incoming, newer);
        Operation applied = STATE.applyOperation(transformed);
        SyncMessage outbound = new SyncMessage("op", null, applied.getBaseRevision(), applied);
        BROADCAST.broadcastExcept(SESSIONS, GSON.toJson(outbound), session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error for session " + (session == null ? "unknown" : session.getId()) + ": " + throwable.getMessage());
    }

    private record SyncMessage(String kind, String document, long revision, Operation operation) {}
}
