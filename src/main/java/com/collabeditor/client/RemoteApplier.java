package com.collabeditor.client;

import com.collabeditor.common.Operation;
import com.collabeditor.common.OperationType;

import javax.swing.JTextArea;

public class RemoteApplier {
    private final JTextArea textArea;

    public RemoteApplier(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void apply(Operation operation) {
        String current = textArea.getText();
        if (operation.getType() == OperationType.INSERT) {
            int pos = Math.max(0, Math.min(operation.getPosition(), current.length()));
            String next = current.substring(0, pos) + operation.getText() + current.substring(pos);
            textArea.setText(next);
            textArea.setCaretPosition(Math.min(next.length(), pos + operation.insertedLength()));
        } else {
            int start = Math.max(0, Math.min(operation.getPosition(), current.length()));
            int end = Math.max(start, Math.min(start + operation.getLength(), current.length()));
            String next = current.substring(0, start) + current.substring(end);
            textArea.setText(next);
            textArea.setCaretPosition(Math.min(next.length(), start));
        }
    }
}
