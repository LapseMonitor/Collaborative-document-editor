package com.collabeditor.client;

import com.collabeditor.common.Operation;
import com.google.gson.Gson;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;
import java.util.function.Consumer;

public class EditorClient extends Endpoint {
    private static final Gson GSON = new Gson();

    private final URI uri;
    private final Consumer<SyncMessage> syncConsumer;
    private Session session;

    public EditorClient(String uri, Consumer<SyncMessage> syncConsumer) {
        this.uri = URI.create(uri);
        this.syncConsumer = syncConsumer;
    }

    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, ClientEndpointConfig.Builder.create().build(), uri);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to connect to " + uri, ex);
        }
    }

    public void send(Operation operation) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(GSON.toJson(operation));
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler((MessageHandler.Whole<String>) msg -> {
            SyncMessage decoded = GSON.fromJson(msg, SyncMessage.class);
            syncConsumer.accept(decoded);
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        this.session = null;
    }

    public record SyncMessage(String kind, String document, long revision, Operation operation) {}
}
