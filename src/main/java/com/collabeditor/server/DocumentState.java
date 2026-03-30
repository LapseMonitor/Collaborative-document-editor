package com.collabeditor.server;

import com.collabeditor.common.Operation;
import com.collabeditor.common.OperationType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DocumentState {
    private final StringBuilder content = new StringBuilder();
    private final List<Operation> revisionLog = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private long revision = 0;

    public Operation applyOperation(Operation op) {
        lock.writeLock().lock();
        try {
            if (op.getType() == OperationType.INSERT) {
                int pos = Math.max(0, Math.min(op.getPosition(), content.length()));
                content.insert(pos, op.getText());
                op.setPosition(pos);
            } else {
                int pos = Math.max(0, Math.min(op.getPosition(), content.length()));
                int maxDelete = Math.max(0, content.length() - pos);
                int actualDelete = Math.min(op.getLength(), maxDelete);
                if (actualDelete > 0) {
                    content.delete(pos, pos + actualDelete);
                }
                op.setPosition(pos);
                op.setLength(actualDelete);
            }
            revision++;
            op.setBaseRevision(revision);
            revisionLog.add(cloneOp(op));
            return op;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public long getRevision() {
        lock.readLock().lock();
        try {
            return revision;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getSnapshot() {
        lock.readLock().lock();
        try {
            return content.toString();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Operation> operationsSince(long baseRevisionExclusive) {
        lock.readLock().lock();
        try {
            int from = (int) Math.max(0, Math.min(baseRevisionExclusive, revisionLog.size()));
            return new ArrayList<>(revisionLog.subList(from, revisionLog.size()));
        } finally {
            lock.readLock().unlock();
        }
    }

    private Operation cloneOp(Operation op) {
        return new Operation(op.getType(), op.getPosition(), op.getText(), op.getLength(), op.getBaseRevision(), op.getAuthorId());
    }
}
