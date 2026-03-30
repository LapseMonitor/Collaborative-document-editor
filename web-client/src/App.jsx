import { useEffect, useMemo, useRef, useState } from 'react';
import './styles.css';

function buildOperations(previous, current, revision, authorId) {
  if (previous === current) {
    return [];
  }

  let prefix = 0;
  const minLen = Math.min(previous.length, current.length);
  while (prefix < minLen && previous[prefix] === current[prefix]) {
    prefix += 1;
  }

  let suffix = 0;
  while (
    previous.length - 1 - suffix >= prefix &&
    current.length - 1 - suffix >= prefix &&
    previous[previous.length - 1 - suffix] === current[current.length - 1 - suffix]
  ) {
    suffix += 1;
  }

  const removed = previous.slice(prefix, previous.length - suffix);
  const added = current.slice(prefix, current.length - suffix);

  const operations = [];
  if (removed.length > 0) {
    operations.push({
      type: 'DELETE',
      position: prefix,
      length: removed.length,
      text: null,
      baseRevision: revision,
      authorId
    });
  }

  if (added.length > 0) {
    operations.push({
      type: 'INSERT',
      position: prefix,
      text: added,
      length: 0,
      baseRevision: revision,
      authorId
    });
  }

  return operations;
}

function applyOperation(content, operation) {
  if (!operation) {
    return content;
  }

  if (operation.type === 'INSERT') {
    const pos = Math.max(0, Math.min(operation.position, content.length));
    return content.slice(0, pos) + (operation.text ?? '') + content.slice(pos);
  }

  const start = Math.max(0, Math.min(operation.position, content.length));
  const end = Math.max(start, Math.min(start + (operation.length ?? 0), content.length));
  return content.slice(0, start) + content.slice(end);
}

export default function App() {
  const [username, setUsername] = useState(`web-user-${Math.floor(Math.random() * 1000)}`);
  const [connected, setConnected] = useState(false);
  const [status, setStatus] = useState('Disconnected');
  const [revision, setRevision] = useState(0);
  const [content, setContent] = useState('');

  const socketRef = useRef(null);
  const previousContentRef = useRef('');
  const applyingRemoteRef = useRef(false);

  const wsUrl = useMemo(() => 'ws://localhost:8025/ws/document', []);

  const connect = () => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      return;
    }

    const ws = new WebSocket(wsUrl);
    socketRef.current = ws;
    setStatus('Connecting...');

    ws.onopen = () => {
      setConnected(true);
      setStatus(`Connected to ${wsUrl}`);
    };

    ws.onclose = () => {
      setConnected(false);
      setStatus('Disconnected');
    };

    ws.onerror = () => {
      setStatus('Connection error');
    };

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (message.kind === 'sync') {
        applyingRemoteRef.current = true;
        const incomingDoc = message.document ?? '';
        setContent(incomingDoc);
        previousContentRef.current = incomingDoc;
        setRevision(message.revision ?? 0);
        applyingRemoteRef.current = false;
      }

      if (message.kind === 'op' && message.operation) {
        applyingRemoteRef.current = true;
        setContent((prev) => {
          const next = applyOperation(prev, message.operation);
          previousContentRef.current = next;
          return next;
        });
        setRevision(message.revision ?? revision);
        applyingRemoteRef.current = false;
      }
    };
  };

  useEffect(() => () => socketRef.current?.close(), []);

  const onChange = (event) => {
    const next = event.target.value;
    setContent(next);

    if (applyingRemoteRef.current) {
      previousContentRef.current = next;
      return;
    }

    const ws = socketRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      previousContentRef.current = next;
      return;
    }

    const ops = buildOperations(previousContentRef.current, next, revision, username);
    ops.forEach((op) => ws.send(JSON.stringify(op)));
    previousContentRef.current = next;
  };

  return (
    <div className="container">
      <header>
        <h1>Collaborative Document Editor (React)</h1>
        <p>{status}</p>
      </header>

      <section className="controls">
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} />
        </label>
        <button onClick={connect} disabled={connected}>
          {connected ? 'Connected' : 'Connect'}
        </button>
        <span>Revision: {revision}</span>
      </section>

      <textarea
        value={content}
        onChange={onChange}
        placeholder="Start typing..."
      />
    </div>
  );
}
