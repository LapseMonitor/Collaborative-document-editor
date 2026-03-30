# Collaborative Document Editor

A simple Java 17 collaborative editor using a **client-server model**.

## Architecture

- **Server is the source of truth** for the canonical document and revision log.
- Clients communicate in real time via **WebSocket**.
- Operations are transformed on the server using a lightweight **Operational Transformation** strategy.

### Server Components

- `DocumentServer` — WebSocket endpoint (`/document`) for session handling and operation routing.
- `DocumentState` — canonical `StringBuilder` + revision counter + revision log.
- `OperationTransformer` — transforms stale operations against newer revisions.
- `SessionRegistry` — active WebSocket sessions.
- `BroadcastService` — broadcasts transformed operations to all clients except sender.

### Client Components

- `EditorWindow` — Swing UI (`JFrame`, `JTextArea`, user panel).
- `ChangeListener` — computes local text diffs and emits `Operation`s.
- `EditorClient` — JSR-356 WebSocket client.
- `RemoteApplier` — applies remote operations safely on the Swing EDT.

### Shared Components

- `Operation` — edit model (`INSERT`/`DELETE`, position, text/length, revision, author).
- `OperationType` — enum.

## Project Structure

```text
collab-editor/
├── src/main/java/com/collabeditor/
│   ├── server/
│   │   ├── DocumentServer.java
│   │   ├── DocumentState.java
│   │   ├── OperationTransformer.java
│   │   ├── SessionRegistry.java
│   │   ├── BroadcastService.java
│   │   └── ServerMain.java
│   ├── client/
│   │   ├── EditorWindow.java
│   │   ├── ChangeListener.java
│   │   ├── EditorClient.java
│   │   └── RemoteApplier.java
│   └── common/
│       ├── Operation.java
│       └── OperationType.java
└── src/test/java/com/collabeditor/server/
    └── OperationTransformerTest.java
```

## Tech Stack

- Java 17+
- Maven
- Jakarta WebSocket (JSR-356) + Tyrus
- Gson
- Swing
- JUnit 5 + Mockito

## Run

### 1) Start server

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=com.collabeditor.server.ServerMain
```

Server URL: `ws://localhost:8025/ws/document`

### 2) Start client(s)

In another terminal:

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=com.collabeditor.client.EditorWindow
```

Open multiple client windows and edit collaboratively.

## Test

```bash
mvn test
```
