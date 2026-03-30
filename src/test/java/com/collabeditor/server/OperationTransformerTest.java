package com.collabeditor.server;

import com.collabeditor.common.Operation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationTransformerTest {

    private final OperationTransformer transformer = new OperationTransformer();

    @Test
    void shiftsInsertWhenConcurrentInsertAtSamePosition() {
        Operation incoming = Operation.insert(10, "hello", 5, "B");
        Operation applied = Operation.insert(10, "world", 6, "A");

        Operation transformed = transformer.transform(incoming, List.of(applied));

        assertEquals(15, transformed.getPosition());
    }

    @Test
    void adjustsDeleteAroundAppliedInsert() {
        Operation incoming = Operation.delete(5, 3, 2, "B");
        Operation applied = Operation.insert(4, "XX", 3, "A");

        Operation transformed = transformer.transform(incoming, List.of(applied));

        assertEquals(7, transformed.getPosition());
        assertEquals(3, transformed.getLength());
    }
}
