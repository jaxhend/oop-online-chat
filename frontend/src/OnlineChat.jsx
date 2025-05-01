import React, {
    useEffect,
    useLayoutEffect,
    useRef,
    useState,
    useMemo
} from "react";
import "./index.css";


const NewsTicker = React.memo(({ newsList, animate }) => {
    const tickerRef = useRef(null);
    const repeatedNews = useMemo(() => [...newsList, ...newsList], [newsList]);

    useLayoutEffect(() => {
        if (!tickerRef.current || newsList.length === 0) return;
        const contentWidth = tickerRef.current.scrollWidth;
        const containerWidth = tickerRef.current.parentElement.offsetWidth;
        const speed = 100;
        const duration = (contentWidth + containerWidth) / speed;
        tickerRef.current.style.animationDuration = `${duration}s`;
    }, [newsList]);

    return (
        <div className="news-ticker">
            <div
                ref={tickerRef}
                className={`news-wrapper ${animate ? 'animate-marquee' : ''}`}
            >
                {repeatedNews.map((item, idx) => {
                    const content = (
                        <>
                            {item.sourceName && item.link ? (
                                <>
                                    <span className="news-source-link">{item.sourceName}</span>
                                    {" - "}
                                    {item.title}
                                </>
                            ) : (
                                item.title
                            )}
                        </>
                    );

                    return item.link ? (
                        <a
                            key={idx}
                            className="news-item"
                            href={item.link}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            {content}
                        </a>
                    ) : (
                        <span key={idx} className="news-item">
                            {content}
                        </span>
                    );
                })}
            </div>
        </div>
    );
});

export default function OnlineChat() {
    const [chatMessages, setChatMessages] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [botInput, setBotInput] = useState("");
    const [dailyDeals, setDailyDeals] = useState([]);
    const [weatherInfo, setWeatherInfo] = useState({});
    const [newsList, setNewsList] = useState([]);
    const [chatHistory, setChatHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [username, setUsername] = useState("");
    const [usernameAccepted, setUsernameAccepted] = useState(false);
    const [usernameError, setUsernameError] = useState("");
    const [theme, setTheme] = useState("light");

    const chatLogRef = useRef(null);
    const socketRef = useRef(null);
    const pingIntervalRef = useRef(null);
    const reconnectTimeoutRef = useRef(null);
    const sessionId = useRef(crypto.randomUUID());

    const API_URL = "https://api.utchat.ee";
    const initialConnectDone = useRef(false);
    const isConnectingRef = useRef(false);
    const isReconnectingRef = useRef(false);

    useEffect(() => {
        const saved = localStorage.getItem("theme");
        const systemPrefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;

        const initialTheme = saved || (systemPrefersDark ? "dark" : "light");
        setTheme(initialTheme);
        document.documentElement.setAttribute("data-theme", initialTheme);
    }, []);

    const toggleTheme = () => {
        const newTheme = theme === "dark" ? "light" : "dark";
        setTheme(newTheme);
        document.documentElement.setAttribute("data-theme", newTheme);
        localStorage.setItem("theme", newTheme);
    };

    const connectWebSocket = () => {
        if (isConnectingRef.current) return;
        isConnectingRef.current = true;

        const protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
        const socket = new WebSocket(`${protocol}api.utchat.ee/ws?sessionId=${sessionId.current}`);
        socketRef.current = socket;

        socket.onopen = () => {
            initialConnectDone.current = true;
            clearInterval(pingIntervalRef.current);
            pingIntervalRef.current = setInterval(() => {
                if (socket.readyState === WebSocket.OPEN) socket.send('ping');
            }, 20000);
            isConnectingRef.current = false;
            isReconnectingRef.current = false;
        };

        socket.onmessage = (e) => {
            if (e.data !== 'pong') {
                try {
                    const parsed = JSON.parse(e.data);
                    const msg = parsed.text || e.data;

                    if (msg.toLowerCase().includes("kasutajanimi on keelatud")) {
                        setUsernameError(msg);
                        return;
                    }
                    if (msg.includes("Tere tulemast")) {
                        setUsernameAccepted(true);
                        setUsernameError("");
                    }
                    addChatMessage(parsed);
                } catch {
                    if (e.data.toLowerCase().includes("kasutajanimi on keelatud")) {
                        setUsernameError(e.data);
                        return;
                    }
                    if (e.data.includes("Tere tulemast")) {
                        setUsernameAccepted(true);
                        setUsernameError("");
                    }
                    addChatMessage({ text: e.data });
                }
            }
        };

        socket.onerror = (err) => console.error('WebSocket viga:', err);
        socket.onclose = () => {
            if (initialConnectDone.current) addChatMessage('Ühendus suleti.');
            clearInterval(pingIntervalRef.current);
            socketRef.current = null;
            isConnectingRef.current = false;
            if (!isReconnectingRef.current) {
                isReconnectingRef.current = true;
                reconnectTimeoutRef.current = setTimeout(connectWebSocket, 1000);
            }
        };
    };

    useEffect(() => {
        let loadedCount = 0;
        const totalToLoad = 3;
        connectWebSocket();

        const fetchContent = async (endpoint, setter) => {
            try {
                const res = await fetch(`${API_URL}${endpoint}`);
                const data = await res.json();
                setter(data);
            } catch {
                setter([]);
            } finally {
                loadedCount += 1;
                if (loadedCount >= totalToLoad) setLoading(false);
            }
        };

        fetchContent('/uudised', setNewsList);
        fetchContent('/paevapakkumised', setDailyDeals);
        fetchContent('/ilm', setWeatherInfo);

        return () => {
            clearInterval(pingIntervalRef.current);
            clearTimeout(reconnectTimeoutRef.current);
            socketRef.current?.close();
        };
    }, []);

    const addChatMessage = (message) => {
        setChatMessages(prev => [...prev, message]);
        setTimeout(() => {
            if (chatLogRef.current) chatLogRef.current.scrollTop = chatLogRef.current.scrollHeight;
        }, 100);
    };

    const sendChatMessage = () => {
        const msg = chatInput.trim();
        if (!msg) return;
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(msg);
            setChatInput('');
        } else addChatMessage('WebSocket pole ühendatud');
    };

    const sendToBot = async (text) => {
        try {
            const res = await fetch(`${API_URL}/chatbot`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ user_id: sessionId.current, prompt: text })
            });
            const data = await res.json();
            if (data.history) setChatHistory(data.history);
            else setChatHistory(prev => [...prev, { sender: 'Sina', text }, { sender: 'Bot', text: data.response }]);
        } catch {
            setChatHistory(prev => [...prev, { sender: 'Sina', text }, { sender: 'Bot', text: 'Flask viga' }]);
        }
    };

    const handleBotSend = () => {
        const msg = botInput.trim();
        if (!msg) return;
        sendToBot(msg);
        setBotInput('');
    };

    return (
        <>
            {loading && <div className="loading-overlay"><div className="loader"></div></div>}

            {!usernameAccepted && !loading && (
                <div className="overlay">
                    <div className="username-dialog">
                        <h2>Sisestage kasutajanimi</h2>
                        <input
                            className="username-input"
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === "Enter" && socketRef.current?.readyState === WebSocket.OPEN) {
                                    socketRef.current.send(username);
                                }
                            }}
                            placeholder="Sisesta kasutajanimi"
                            autoFocus
                        />
                        {usernameError && <p className="username-error">{usernameError}</p>}
                    </div>
                </div>
            )}

            {/* Teema nupu konteiner */}
            <div className="theme-toggle-container">
                <button onClick={toggleTheme} className="theme-toggle">
                    {theme === "dark" ? "Light Mode" : "Dark Mode"}
                </button>
            </div>

            <div className="flex flex-col min-h-screen">
                <NewsTicker newsList={newsList} animate={!loading} />

                <div className="container mx-auto flex flex-1 p-5 gap-5 font-sans flex-row">
                    {/* Päevapakkumised ja ilm */}
                    <div className="flex flex-col fixed-flex-1 border p-3 overflow-y-auto">
                        <div className="flex-1 border-b mb-2">
                            <h4 className="font-bold mb-1">Päevapakkumised</h4>
                            <ul>{dailyDeals.length > 0 ? dailyDeals.map((deal, i) => (
                                <li key={i}>
                                    <strong>{deal.restaurant}</strong>: {deal.offer}
                                </li>
                            )) : <li>Ei ole saadaval lõunapakkumisi</li>}
                            </ul>
                        </div>
                        <div className="flex-1">
                            <h4 className="font-bold mb-1">Ilm</h4>
                            {weatherInfo.temperature ? <p>Temperatuur: {weatherInfo.temperature}</p> : <p>Ilma andmeid ei ole saadaval</p>}
                            {weatherInfo.feelsLike ? <p>Tundub nagu: {weatherInfo.feelsLike}</p> : <p>Ilma andmeid ei ole saadaval</p>}
                            {weatherInfo.precipitation ? <p>Sademed: {weatherInfo.precipitation}</p> : <p>Ilma andmeid ei ole saadaval</p>}
                            {weatherInfo.iconUrl && <img src={weatherInfo.iconUrl} alt="Ilma ikoon" />}
                        </div>
                    </div>

                    {/* Vestlusplats */}
                    <div className="flex flex-col fixed-flex-2 border p-3 flex-1 chat-pane">
                        <h2 className="text-xl font-semibold mb-2">Vestlusplats</h2>
                        <div ref={chatLogRef} className="chat-log-fixed whitespace-pre-wrap mb-2">
                            {chatMessages.map((msg, i) => (
                                <div key={i} style={{ color: msg.color || "#000" }}>
                                    {msg.text}
                                </div>
                            ))}
                        </div>
                        <div className="flex gap-2">
                        <textarea
                            rows={1}
                            value={chatInput}
                            onChange={e => setChatInput(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === 'Enter' && !e.shiftKey) {
                                    e.preventDefault();
                                    sendChatMessage();
                                }
                            }}
                            className="border p-2 resize-none flex-1 h-10"
                            placeholder="Sisesta sõnum..."
                        />
                            <button onClick={sendChatMessage} className="border px-3 py-1 h-10">Saada</button>
                        </div>
                    </div>

                    {/* AI juturobot */}
                    <div className="flex flex-col fixed-flex-1-right border p-3 flex-1 overflow-y-auto">
                        <h3 className="font-semibold mb-2">AI juturobot</h3>
                        <div className="chat-log-fixed whitespace-pre-wrap mb-2">
                            {chatHistory.map((entry, i) => (
                                <div key={i}><strong>{entry.sender}:</strong> {entry.text}</div>
                            ))}
                        </div>
                        <div className="flex gap-2">
                        <textarea
                            rows={1}
                            value={botInput}
                            onChange={e => setBotInput(e.target.value)}
                            onKeyDown={e => {
                                if (e.key === 'Enter' && !e.shiftKey) {
                                    e.preventDefault();
                                    handleBotSend();
                                }
                            }}
                            className="border p-2 resize-none flex-1 h-10"
                            placeholder="Sisesta küsimus..."
                        />
                            <button onClick={handleBotSend} className="border px-3 py-1 h-10">Saada botile</button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
