package com.collabeditor.client;

import com.collabeditor.common.Operation;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChangeListener implements DocumentListener {
    private final Supplier<String> textSupplier;
    private final Supplier<Long> revisionSupplier;
    private final Supplier<String> authorSupplier;
    private final Supplier<Boolean> remoteApplyGuard;
    private final Consumer<Operation> operationConsumer;
    private String previousText = "";

    public ChangeListener(Supplier<String> textSupplier,
                          Supplier<Long> revisionSupplier,
                          Supplier<String> authorSupplier,
                          Supplier<Boolean> remoteApplyGuard,
                          Consumer<Operation> operationConsumer) {
        this.textSupplier = textSupplier;
        this.revisionSupplier = revisionSupplier;
        this.authorSupplier = authorSupplier;
        this.remoteApplyGuard = remoteApplyGuard;
        this.operationConsumer = operationConsumer;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        emitDiff();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        emitDiff();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        emitDiff();
    }

    public void initialize(String initialText) {
        this.previousText = initialText;
    }

    private void emitDiff() {
        if (remoteApplyGuard.get()) {
            previousText = textSupplier.get();
            return;
        }

        String current = textSupplier.get();
        int prefix = commonPrefix(previousText, current);
        int oldSuffix = commonSuffix(previousText, current, prefix);

        String removed = previousText.substring(prefix, previousText.length() - oldSuffix);
        String added = current.substring(prefix, current.length() - oldSuffix);

        if (!removed.isEmpty()) {
            operationConsumer.accept(Operation.delete(prefix, removed.length(), revisionSupplier.get(), authorSupplier.get()));
        }
        if (!added.isEmpty()) {
            operationConsumer.accept(Operation.insert(prefix, added, revisionSupplier.get(), authorSupplier.get()));
        }

        previousText = current;
    }

    private int commonPrefix(String a, String b) {
        int len = Math.min(a.length(), b.length());
        int i = 0;
        while (i < len && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return i;
    }

    private int commonSuffix(String a, String b, int prefix) {
        int aLen = a.length();
        int bLen = b.length();
        int i = 0;
        while (aLen - 1 - i >= prefix && bLen - 1 - i >= prefix
            && a.charAt(aLen - 1 - i) == b.charAt(bLen - 1 - i)) {
            i++;
        }
        return i;
    }
}
