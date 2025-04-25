import { useEffect, useRef, useState } from "react";

export default function OnlineChat() {
    const [log, setLog] = useState([]);
    const [msg, setMsg] = useState("");
    const [chatbotInput, setChatbotInput] = useState("");
    const [chatbotOutput, setChatbotOutput] = useState("");
    const [deals, setDeals] = useState("");
    const [weather, setWeather] = useState("");

    const logRef = useRef(null);
    const socketRef = useRef(null);
    const sessionId = useRef(crypto.randomUUID());

    useEffect(() => {
        const protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
        const url = protocol + location.host + '/ws?sessionId=' + sessionId.current;
        socketRef.current = new WebSocket(url);

        socketRef.current.onopen = () => addLog('Ühendus loodud');
        socketRef.current.onmessage = e => addLog(e.data);
        socketRef.current.onerror = e => addLog('Viga: ' + e.message);
        socketRef.current.onclose = e => addLog('Ühendus suleti: kood ' + e.code);

        fetchFlaskContent("päevapakkumised", setDeals);
        fetchFlaskContent("ilm", setWeather);

        return () => socketRef.current?.close();
    }, []);

    const addLog = (message) => {
        setLog(prev => [...prev, message]);
        setTimeout(() => {
            if (logRef.current) {
                logRef.current.scrollTop = logRef.current.scrollHeight;
            }
        }, 100);
    };

    const send = () => {
        const trimmed = msg.trim();
        if (!trimmed) return;
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(trimmed);
            setMsg("");
        } else {
            addLog("WebSocket pole ühendatud");
        }
    };

    const sendToFlask = async (text) => {
        try {
            const response = await fetch("http://localhost:5001/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    user_id: sessionId.current,
                    prompt: text
                })
            });
            const data = await response.json();
            setChatbotOutput("Bot: " + data.response);
        } catch (error) {
            setChatbotOutput("Flask viga: " + error.message);
        }
    };

    const fetchFlaskContent = async (prompt, setResult) => {
        try {
            const response = await fetch("/api/chat/flask", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: "info_" + prompt,
                    prompt: prompt
                })
            });
            const data = await response.json();
            setResult(data.response || "Viga vastuse saamisel");
        } catch (err) {
            setResult("Viga: " + err.message);
        }
    };

    const sendToChatbot = () => {
        const trimmed = chatbotInput.trim();
        if (!trimmed) return;
        sendToFlask(trimmed);
    };

    return (
        <div className="flex h-screen p-5 gap-5 font-sans">
            <div className="flex flex-col flex-1 border p-3 overflow-y-auto">
                <div className="flex-1 border-b mb-2">
                    <h4 className="font-bold mb-1">Päevapakkumised</h4>
                    <div>{deals}</div>
                </div>
                <div className="flex-1">
                    <h4 className="font-bold mb-1">Ilm</h4>
                    <div>{weather}</div>
                </div>
            </div>

            <div className="flex flex-col flex-[2]">
                <h2 className="text-xl font-semibold mb-2">Online-chat</h2>
                <div
                    ref={logRef}
                    className="flex-1 border p-2 overflow-y-auto whitespace-pre-wrap mb-2"
                >
                    {log.map((line, i) => <div key={i}>{line}</div>)}
                </div>
                <div className="flex gap-2">
                    <input
                        value={msg}
                        onChange={e => setMsg(e.target.value)}
                        className="flex-1 border px-2 py-1"
                        placeholder="Sisesta sõnum..."
                    />
                    <button onClick={send} className="border px-3 py-1">Saada</button>
                </div>
            </div>

            <div className="flex flex-col flex-1 border p-3">
                <h3 className="font-semibold">Chatbot</h3>
                <textarea
                    rows={6}
                    value={chatbotInput}
                    onChange={e => setChatbotInput(e.target.value)}
                    className="border p-2 mb-2 resize-none"
                    placeholder="Sisesta küsimus..."
                />
                <button onClick={sendToChatbot} className="border px-3 py-1 mb-2">Saada botile</button>
                <div>{chatbotOutput}</div>
            </div>
        </div>
    );
}
