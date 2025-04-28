import { useEffect, useLayoutEffect, useRef, useState } from "react";

export default function OnlineChat() {
    const [chatMessages, setChatMessages] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [botInput, setBotInput] = useState("");
    const [dailyDeals, setDailyDeals] = useState([]);
    const [weatherInfo, setWeatherInfo] = useState([]);
    const [newsList, setNewsList] = useState([]);
    const [chatHistory, setChatHistory] = useState([]);

    const chatLogRef = useRef(null);
    const socketRef = useRef(null);
    const pingIntervalRef = useRef(null);
    const reconnectTimeoutRef = useRef(null);
    const sessionId = useRef(crypto.randomUUID());
    const marqueeRef = useRef(null);

    const API_URL = "https://api.utchat.ee";

    const initialConnectDone = useRef(false);
    const isConnectingRef = useRef(false);
    const isReconnectingRef = useRef(false);

    const connectWebSocket = () => {
        if (isConnectingRef.current) return;

        isConnectingRef.current = true;

        const protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
        const url = protocol + 'api.utchat.ee/ws?sessionId=' + sessionId.current;
        const socket = new WebSocket(url);
        socketRef.current = socket;

        socket.onopen = () => {
            addChatMessage('Ühendus loodud. Sisestage oma nimi:');
            initialConnectDone.current = true;
            if (pingIntervalRef.current) clearInterval(pingIntervalRef.current);
            pingIntervalRef.current = setInterval(() => {
                if (socket.readyState === WebSocket.OPEN) {
                    socket.send('ping');
                }
            }, 20000);
            isConnectingRef.current = false;
            isReconnectingRef.current = false;
        };

        socket.onmessage = (e) => {
            if (e.data !== 'pong') {
                addChatMessage(e.data);
            }
        };

        socket.onerror = (err) => {
            console.error('WebSocket viga:', err);
        };

        socket.onclose = () => {
            if (initialConnectDone.current) {
                addChatMessage('Ühendus suleti.');
            }
            if (pingIntervalRef.current) clearInterval(pingIntervalRef.current);
            if (socketRef.current) socketRef.current = null;
            isConnectingRef.current = false;
            if (!isReconnectingRef.current) {
                isReconnectingRef.current = true;
                reconnectTimeoutRef.current = setTimeout(() => {
                    connectWebSocket();
                }, 1000);
            }
        };
    };

    useEffect(() => {
        connectWebSocket();

        const fetchNews = async () => {
            try {
                const response = await fetch(`${API_URL}/uudised`);
                const news = await response.json();
                setNewsList(news);
            } catch {
                console.error("Uudiste laadimine ebaõnnestus");
            }
        };

        const fetchContent = async (endpoint, setResult) => {
            try {
                const response = await fetch(`${API_URL}${endpoint}`);
                const content = await response.json();
                setResult(content);
            } catch {
                setResult(["Viga"]);
            }
        };

        fetchContent("/paevapakkumised", setDailyDeals);
        fetchContent("/ilm", setWeatherInfo);
        fetchNews();

        return () => {
            if (pingIntervalRef.current) clearInterval(pingIntervalRef.current);
            if (reconnectTimeoutRef.current) clearTimeout(reconnectTimeoutRef.current);
            if (socketRef.current) {
                socketRef.current.close();
                socketRef.current = null;
            }
        };
    }, []);

    useLayoutEffect(() => {
        if (!marqueeRef.current || newsList.length === 0) return;

        const contentWidth = marqueeRef.current.scrollWidth;
        const containerWidth = marqueeRef.current.parentElement.offsetWidth;
        const speed = 50;
        const duration = (contentWidth + containerWidth) / speed;

        marqueeRef.current.style.animationDuration = `${duration}s`;
    }, [newsList]);

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
            const response = await fetch(`${API_URL}/chatbot`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ user_id: sessionId.current, prompt: text })
            });
            const data = await response.json();
            if (data.history) {
                setChatHistory(data.history);
            } else {
                setChatHistory(prev => [...prev, { sender: "Sina", text }, { sender: "Bot", text: data.response }]);
            }
        } catch (error) {
            setChatHistory(prev => [...prev, { sender: "Sina", text }, { sender: "Bot", text: "Flask viga" }]);
        }
    };

    const handleBotSend = () => {
        const trimmed = botInput.trim();
        if (!trimmed) return;
        sendToBot(trimmed);
        setBotInput("");
    };

    const NewsList = ({ newsItems }) => {
        const repeatedNews = [...newsItems, ...newsItems];

        const duration = repeatedNews.length * 5;

        return (
            <div
                className="news-wrapper animate-marquee"
                style={{ animationDuration: `${duration}s` }}
            >
                {repeatedNews.map((newsItem, index) => (
                    <span key={index} className="news-item">
                    {newsItem.sourceName && newsItem.link ? (
                        <>
                            <a
                                href={newsItem.link}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="news-source-link"
                            >
                                {newsItem.sourceName}
                            </a>
                            {" - "}
                            {newsItem.title}
                        </>
                    ) : (
                        newsItem.title
                    )}
                </span>
                ))}
            </div>
        );
    };

    return (
        <div className="flex flex-col min-h-screen">
            {newsList.length > 0 ? (
                <div className="news-ticker">
                    <div className="animate-marquee text-lg font-semibold">
                        <NewsList newsItems={newsList} />
                    </div>
                </div>
            ) : (
                <div className="text-lg font-semibold">Laen uudiseid...</div>
            )}

            <div className="container mx-auto flex flex-1 p-5 gap-5 font-sans flex-row">
                <div className="flex flex-col fixed-flex-1 border p-3 overflow-y-auto">
                    <div className="flex-1 border-b mb-2">
                        <h4 className="font-bold mb-1">Päevapakkumised</h4>
                        <ul>
                            {dailyDeals.length > 0 ? (
                                dailyDeals.map((deal, i) => (
                                    <li key={i}>{deal}</li>
                                ))
                            ) : (
                                <li>Ei ole saadaval lõunapakkumisi</li>
                            )}
                        </ul>
                    </div>
                    <div className="flex-1">
                        <h4 className="font-bold mb-1">Ilm</h4>
                        {weatherInfo?.temperatuur && weatherInfo?.icon ? (
                            <div>
                                <p>Temperatuur: {weatherInfo.temperatuur}</p>
                                <img src={weatherInfo.icon} alt="Ilma ikoon" />
                            </div>
                        ) : (
                            <p>Ilma andmeid ei ole saadaval</p>
                        )}
                    </div>
                </div>


                <div className="flex flex-col fixed-flex-2 border p-3 flex-1 chat-pane">
                    <h2 className="text-xl font-semibold mb-2">Vestlusplats</h2>
                    <div ref={chatLogRef} className="chat-log-fixed whitespace-pre-wrap mb-2">
                        {chatMessages.map((line, i) => <div key={i}>{line}</div>)}
                    </div>
                    <div className="flex gap-2">
                        <input
                            value={chatInput}
                            onChange={e => setChatInput(e.target.value)}
                            className="flex-1 border px-2 py-1 h-10"
                            placeholder="Sisesta sõnum..."
                            onKeyDown={(e) => { if (e.key === "Enter") sendChatMessage(); }}
                        />
                        <button onClick={sendChatMessage} className="border px-3 py-1 h-10">Saada</button>
                    </div>
                </div>

                <div className="flex flex-col fixed-flex-1-right border p-3 flex-1 overflow-y-auto">
                    <h3 className="font-semibold mb-2">AI juturobot</h3>
                    <div className="chat-log-fixed whitespace-pre-wrap mb-2">
                        {chatHistory.map((entry, i) => (
                            <div key={i}>
                                <strong>{entry.sender}:</strong> {entry.text}
                            </div>
                        ))}
                    </div>
                    <div className="flex gap-2">
                    <textarea
                        rows={1}
                        value={botInput}
                        onChange={e => setBotInput(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" && !e.shiftKey) {
                                e.preventDefault();
                                handleBotSend();
                            }
                        }}
                        className="border p-2 mb-2 resize-none flex-1 h-10"
                        placeholder="Sisesta küsimus..."
                    />
                        <button onClick={handleBotSend} className="border px-3 py-1 h-10 mb-2">Saada botile</button>
                    </div>
                </div>
            </div>
        </div>
    );
}
