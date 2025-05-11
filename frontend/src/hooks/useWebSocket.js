import { useEffect, useRef } from "react";

export default function useWebSocket(sessionId, onMessage, onOpen) {
    const socketRef = useRef(null);

    const socketUrl = `wss://api.utchat.ee/ws?sessionId=${sessionId}`;

    useEffect(() => {
        if (!sessionId) return;

        const socket = new WebSocket(socketUrl);
        socketRef.current = socket;

        const pingInterval = setInterval(() => {
            if (socket.readyState === WebSocket.OPEN) {
                socket.send("__heartbeat_ping__");
            }
        }, 30000); // 30 sekundit

        socket.onopen = () => {
            onOpen?.(socket);
        };

        socket.onmessage = onMessage;

        socket.onerror = (err) => {
            console.error("WebSocket error:", err);
        };

        socket.onclose = () => {
            console.warn("WebSocket suletud, ühendan uuest");
            setTimeout(() => {
                const newSocket = new WebSocket(socketUrl);
                socketRef.current = newSocket;

                newSocket.onmessage = socket.onmessage;
                newSocket.onclose = socket.onclose;
                newSocket.onerror = socket.onerror;
            }, 5000); // 5 sekundi pärast reconnectib
        };

        return () => {
            clearInterval(pingInterval);
            socket.close();
        };
    }, [sessionId]);

    return socketRef.current;
}
