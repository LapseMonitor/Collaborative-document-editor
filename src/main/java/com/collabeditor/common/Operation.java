package com.collabeditor.common;

public class Operation {
    private OperationType type;
    private int position;
    private String text;
    private int length;
    private long baseRevision;
    private String authorId;

    public Operation() {
    }

    public Operation(OperationType type, int position, String text, int length, long baseRevision, String authorId) {
        this.type = type;
        this.position = position;
        this.text = text;
        this.length = length;
        this.baseRevision = baseRevision;
        this.authorId = authorId;
    }

    public static Operation insert(int position, String text, long baseRevision, String authorId) {
        return new Operation(OperationType.INSERT, position, text, 0, baseRevision, authorId);
    }

    public static Operation delete(int position, int length, long baseRevision, String authorId) {
        return new Operation(OperationType.DELETE, position, null, length, baseRevision, authorId);
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getBaseRevision() {
        return baseRevision;
    }

    public void setBaseRevision(long baseRevision) {
        this.baseRevision = baseRevision;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public int insertedLength() {
        return text == null ? 0 : text.length();
    }

    @Override
    public String toString() {
        return "Operation{" +
            "type=" + type +
            ", position=" + position +
            ", text='" + text + '\'' +
            ", length=" + length +
            ", baseRevision=" + baseRevision +
            ", authorId='" + authorId + '\'' +
            '}';
    }
}
