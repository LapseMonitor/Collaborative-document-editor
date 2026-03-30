package com.collabeditor.client;

import com.collabeditor.common.Operation;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class EditorWindow extends JFrame {
    private final JTextArea editor = new JTextArea();
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private final AtomicLong localRevision = new AtomicLong(0);
    private final AtomicBoolean applyingRemote = new AtomicBoolean(false);

    private final String authorId;
    private final EditorClient client;

    public EditorWindow(String wsUri, String authorId) {
        super("Collaborative Editor - " + authorId);
        this.authorId = authorId;
        this.client = new EditorClient(wsUri, this::handleServerMessage);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        usersModel.addElement("You: " + authorId);
        JList<String> usersList = new JList<>(usersModel);
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(editor),
            new JScrollPane(usersList)
        );
        splitPane.setResizeWeight(0.85);
        add(splitPane, BorderLayout.CENTER);

        ChangeListener listener = new ChangeListener(
            editor::getText,
            localRevision::get,
            () -> authorId,
            applyingRemote::get,
            this::sendOperation
        );
        listener.initialize("");
        editor.getDocument().addDocumentListener(listener);

        client.connect();
    }

    private void sendOperation(Operation operation) {
        client.send(operation);
    }

    private void handleServerMessage(EditorClient.SyncMessage syncMessage) {
        SwingUtilities.invokeLater(() -> {
            if ("sync".equals(syncMessage.kind())) {
                applyingRemote.set(true);
                editor.setText(syncMessage.document() == null ? "" : syncMessage.document());
                applyingRemote.set(false);
                localRevision.set(syncMessage.revision());
            } else if ("op".equals(syncMessage.kind()) && syncMessage.operation() != null) {
                applyingRemote.set(true);
                new RemoteApplier(editor).apply(syncMessage.operation());
                applyingRemote.set(false);
                localRevision.set(syncMessage.revision());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String id = JOptionPane.showInputDialog(null, "Enter username", "user-" + System.currentTimeMillis() % 1000);
            if (id == null || id.isBlank()) {
                id = "user-" + System.currentTimeMillis() % 1000;
            }
            EditorWindow window = new EditorWindow("ws://localhost:8025/ws/document", id);
            window.setVisible(true);
        });
    }
}
