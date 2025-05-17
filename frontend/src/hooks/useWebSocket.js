import {useCallback, useEffect, useRef} from "react";

export default function useWebSocket(sessionId, onMessage, onOpen) {
    const socketRef = useRef(null);
    const pingRef = useRef(null);

    const url = `wss://api.utchat.ee/ws?sessionId=${sessionId}`;
    // Localhost testimiseks
    // const socketUrl = `ws://localhost:8080/ws?sessionId=${sessionId}`;

    const handleOpen = useCallback((socket) => {
        if (pingRef.current) clearInterval(pingRef.current); // Tühistame eelmise intervalli.
        pingRef.current = setInterval(() => {
            if (socket.readyState === WebSocket.OPEN) {
                socket.send("__heartbeat_ping__");
            }
        }, 30000);

        onOpen?.(socket);
    }, [onOpen]);


    const connect = useCallback(() => {
        if (!sessionId) return;
        const socket = new WebSocket(url);
        socketRef.current = socket;

        socket.onopen    = () => handleOpen(socket);
        socket.onmessage = onMessage;
        socket.onerror   = (err) => console.error("WebSocket error: ", err);

        socket.onclose = () => {
            console.warn("WebSocket suletud, ühendan uuesti");
            socket.onopen = socket.onmessage = socket.onerror = socket.onclose = null;
            clearInterval(pingRef.current);
            setTimeout(connect, 5000);
        };
    }, [sessionId, onMessage, handleOpen]);


    useEffect(() => {
        connect();
        return () => {
            clearInterval(pingRef.current);
            socketRef.current?.close();
            socketRef.current = null;
        };
    }, [connect]);


    return socketRef;
}
