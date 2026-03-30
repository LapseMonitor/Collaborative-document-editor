package com.collabeditor.server;

import com.collabeditor.common.Operation;
import com.collabeditor.common.OperationType;

import java.util.List;

public class OperationTransformer {

    public Operation transform(Operation incoming, List<Operation> newerOperations) {
        Operation transformed = new Operation(
            incoming.getType(),
            incoming.getPosition(),
            incoming.getText(),
            incoming.getLength(),
            incoming.getBaseRevision(),
            incoming.getAuthorId()
        );

        for (Operation applied : newerOperations) {
            transformed = transformPair(transformed, applied);
        }
        return transformed;
    }

    private Operation transformPair(Operation incoming, Operation applied) {
        if (incoming.getType() == OperationType.INSERT && applied.getType() == OperationType.INSERT) {
            return transformInsertInsert(incoming, applied);
        }
        if (incoming.getType() == OperationType.INSERT && applied.getType() == OperationType.DELETE) {
            return transformInsertDelete(incoming, applied);
        }
        if (incoming.getType() == OperationType.DELETE && applied.getType() == OperationType.INSERT) {
            return transformDeleteInsert(incoming, applied);
        }
        return transformDeleteDelete(incoming, applied);
    }

    private Operation transformInsertInsert(Operation incoming, Operation applied) {
        int shift = applied.insertedLength();
        if (applied.getPosition() < incoming.getPosition()
            || (applied.getPosition() == incoming.getPosition()
            && compareAuthors(applied.getAuthorId(), incoming.getAuthorId()) < 0)) {
            incoming.setPosition(incoming.getPosition() + shift);
        }
        return incoming;
    }

    private int compareAuthors(String left, String right) {
        String a = left == null ? "" : left;
        String b = right == null ? "" : right;
        return a.compareTo(b);
    }

    private Operation transformInsertDelete(Operation incoming, Operation applied) {
        int deleteStart = applied.getPosition();
        int deleteEnd = deleteStart + applied.getLength();
        if (incoming.getPosition() > deleteEnd) {
            incoming.setPosition(incoming.getPosition() - applied.getLength());
        } else if (incoming.getPosition() >= deleteStart) {
            incoming.setPosition(deleteStart);
        }
        return incoming;
    }

    private Operation transformDeleteInsert(Operation incoming, Operation applied) {
        int insertPos = applied.getPosition();
        int insertLen = applied.insertedLength();

        if (insertPos <= incoming.getPosition()) {
            incoming.setPosition(incoming.getPosition() + insertLen);
        } else if (insertPos < incoming.getPosition() + incoming.getLength()) {
            incoming.setLength(incoming.getLength() + insertLen);
        }
        return incoming;
    }

    private Operation transformDeleteDelete(Operation incoming, Operation applied) {
        int inStart = incoming.getPosition();
        int inEnd = inStart + incoming.getLength();
        int appStart = applied.getPosition();
        int appEnd = appStart + applied.getLength();

        if (appEnd <= inStart) {
            incoming.setPosition(inStart - applied.getLength());
            return incoming;
        }

        if (appStart >= inEnd) {
            return incoming;
        }

        int overlapStart = Math.max(inStart, appStart);
        int overlapEnd = Math.min(inEnd, appEnd);
        int overlap = Math.max(0, overlapEnd - overlapStart);

        int newStart = inStart;
        if (appStart < inStart) {
            newStart = appStart;
        }

        incoming.setPosition(newStart);
        incoming.setLength(Math.max(0, incoming.getLength() - overlap));
        return incoming;
    }
}
