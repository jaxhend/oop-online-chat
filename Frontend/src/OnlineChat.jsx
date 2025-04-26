import { useEffect, useRef, useState } from "react";

export default function OnlineChat() {
    const [chatMessages, setChatMessages] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [botInput, setBotInput] = useState("");
    const [botResponse, setBotResponse] = useState("");
    const [dailyDeals, setDailyDeals] = useState("");
    const [weatherInfo, setWeatherInfo] = useState("");
    const [newsList, setNewsList] = useState([]);
    const [chatHistory, setChatHistory] = useState([]);

    const chatLogRef = useRef(null);
    const socketRef = useRef(null);
    const sessionId = useRef(crypto.randomUUID());

    useEffect(() => {
        const fetchNews = async () => {
            try {
                const response = await fetch("/flask/uudised");
                const news = await response.json();
                setNewsList(news);
            } catch (err) {
                console.error("Uudiste laadimine ebaõnnestus:", err);
            }
        };

        const protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
        const url = protocol + (location.hostname === 'localhost' ? 'localhost:8080' : 'api.utchat.ee') + '/ws?sessionId=' + sessionId.current;
        const socket = new WebSocket(url);
        socketRef.current = socket;

        let connected = false;

        socket.onopen = () => {
            connected = true;
            addChatMessage('Ühendus loodud. Sisestage oma nimi:');
        };

        socket.onmessage = e => addChatMessage(e.data);

        socket.onerror = () => {
            if (connected) addChatMessage('Viga WebSocketi ühenduses');
        };

        socket.onclose = e => {
            if (connected) addChatMessage('Ühendus suleti: kood ' + e.code);
        };

        fetchContent("/flask/päevapakumised", setDailyDeals);
        fetchContent("/flask/ilm", setWeatherInfo);
        fetchNews();

        return () => socket.close();
    }, []);

    const addChatMessage = (message) => {
        setChatMessages(prev => [...prev, message]);
        setTimeout(() => {
            if (chatLogRef.current) {
                chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
            }
        }, 100);
    };

    const sendChatMessage = () => {
        const trimmed = chatInput.trim();
        if (!trimmed) return;
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(trimmed);
            setChatInput("");
        } else {
            addChatMessage("WebSocket pole ühendatud");
        }
    };

    const sendToBot = async (text) => {
        try {
            const response = await fetch("/flask/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    user_id: sessionId.current,
                    prompt: text
                })
            });
            const data = await response.json();
            setBotResponse("Bot: " + data.response);
            if (data.history) {
                setChatHistory(data.history);
            }
        } catch (error) {
            setBotResponse("Flask viga: " + error.message);
        }
    };

    const fetchContent = async (endpoint, setResult) => {
        try {
            const response = await fetch(endpoint);
            const content = await response.text();
            setResult(content);
        } catch (err) {
            setResult("Viga: " + err.message);
        }
    };

    const handleBotSend = () => {
        const trimmed = botInput.trim();
        if (!trimmed) return;
        sendToBot(trimmed);
    };

    return (
        <div className="flex flex-col h-screen">
            <div className="container mx-auto flex flex-1 p-5 gap-5 font-sans overflow-hidden flex-row">
                <div className="flex flex-col fixed-flex-1 border p-3 overflow-y-auto">
                    <div className="flex-1 border-b mb-2">
                        <h4 className="font-bold mb-1">Päevapakkumised</h4>
                        <div>{dailyDeals}</div>
                    </div>
                    <div className="flex-1 border-b mb-2">
                        <h4 className="font-bold mb-1">Ilm</h4>
                        <div>{weatherInfo}</div>
                    </div>
                </div>

                <div className="flex flex-col fixed-flex-2 border p-3 overflow-hidden chat-pane">
                    <h2 className="text-xl font-semibold mb-2">Online-chat</h2>
                    <div ref={chatLogRef} className="chat-log-fixed whitespace-pre-wrap mb-2">
                        {chatMessages.map((line, i) => (
                            <div key={i}>{line}</div>
                        ))}
                    </div>
                    <div className="flex gap-2">
                        <input
                            value={chatInput}
                            onChange={e => setChatInput(e.target.value)}
                            className="flex-1 border px-2 py-1 h-10"
                            placeholder="Sisesta sõnum..."
                            onKeyDown={(e) => {
                                if (e.key === "Enter") sendChatMessage();
                            }}
                        />
                        <button onClick={sendChatMessage} className="border px-3 py-1 h-10">Saada</button>
                    </div>
                </div>

                <div className="flex flex-col fixed-flex-1-right border p-3 overflow-y-auto">
                    <h3 className="font-semibold mb-2">Chatbot</h3>
                    <textarea
                        rows={6}
                        value={botInput}
                        onChange={e => setBotInput(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" && !e.shiftKey) {
                                e.preventDefault();
                                handleBotSend();
                            }
                        }}
                        className="border p-2 mb-2 resize-none"
                        placeholder="Sisesta küsimus..."
                    />
                    <button onClick={handleBotSend} className="border px-3 py-1 mb-2">Saada botile</button>
                    <div className="mb-2">{botResponse}</div>
                    {chatHistory.length >= 2 && (
                        <div className="text-sm">
                            <h4 className="font-bold mt-4 mb-2">Viimased vestlused:</h4>
                            {chatHistory.slice(0, -1).map((entry, index) => (
                                <div key={index} className="mb-2">
                                    <div><strong>Sina:</strong> {entry.user}</div>
                                    <div><strong>Bot:</strong> {entry.bot}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            <div className="news-ticker">
                <div className="animate-marquee text-lg font-semibold">
                    {[...newsList, ...newsList].map((news, index) => (
                        <span key={index} className="news-item">{news}</span>
                    ))}
                </div>
            </div>
        </div>
    );
}