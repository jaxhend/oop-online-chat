import { useEffect, useRef } from "react";

export default function useWebSocket(sessionId, onMessage, onOpen) {
    const socketRef = useRef(null);

    useEffect(() => {
        const socket = new WebSocket(`wss://"api.utchat.ee/ws?sessionId=${sessionId}`);
        socketRef.current = socket;

        socket.onopen = () => {
            console.log("WebSocket connected");
            onOpen?.(socket);
        };

        socket.onmessage = onMessage;
        socket.onerror = (err) => console.error("WebSocket error:", err);
        socket.onclose = () => console.warn("WebSocket closed");

        return () => socket.close();
    }, [sessionId]);

    return socketRef;
}