import React, { useEffect, useRef, useState } from "react";
import { useCookies } from "react-cookie";
import NewsTicker from "./components/NewsTicker/NewsTicker";
import DailyDeals from "./components/DailyDeals/DailyDeals";
import WeatherInfo from "./components/WeatherInfo/WeatherInfo";
import UsernameDialog from "./components/UsernameDialog/UsernameDialog";
import ChatPanel from "./components/ChatPanel/ChatPanel";
import AIChatPanel from "./components/AIChatPanel/AIChatPanel";
import ThemeToggle from "./components/ThemeToggle/ThemeToggle";
import "./index.css";

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
    const sessionId = useRef(crypto.randomUUID());

    const API_URL = "https://api.utchat.ee";
    const [cookies, setCookie] = useCookies(["username"]);

    useEffect(() => {
        const savedUsername = cookies.username;
        if (savedUsername) {
            setUsername(savedUsername);
            setUsernameAccepted(true);

            if (socketRef.current?.readyState === WebSocket.OPEN) {
                socketRef.current.send(savedUsername);
            } else {
                const interval = setInterval(() => {
                    if (socketRef.current?.readyState === WebSocket.OPEN) {
                        socketRef.current.send(savedUsername);
                        clearInterval(interval);
                    }
                }, 100);
            }
        }

        const savedTheme = localStorage.getItem("theme");
        const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
        const initialTheme = savedTheme || (prefersDark ? "dark" : "light");
        setTheme(initialTheme);
        document.documentElement.setAttribute("data-theme", initialTheme);

    }, [cookies]);

    const toggleTheme = () => {
        const newTheme = theme === "dark" ? "light" : "dark";
        setTheme(newTheme);
        document.documentElement.setAttribute("data-theme", newTheme);
        localStorage.setItem("theme", newTheme);
    };

    const connectWebSocket = () => {
        const protocol = location.protocol === "https:" ? "wss://" : "ws://";
        const socket = new WebSocket(`${protocol}api.utchat.ee/ws?sessionId=${sessionId.current}`);
        socketRef.current = socket;

        socket.onopen = () => {
            console.log("WebSocket connected");

            const savedUsername = cookies.username;
            if (savedUsername) {
                socket.send(savedUsername);
            }
        };

        socket.onmessage = (e) => {
            if (e.data !== "pong") {
                try {
                    const msg = JSON.parse(e.data);
                    if (msg.text?.toLowerCase().includes("kasutajanimi on keelatud")) {
                        setUsernameError(msg.text);
                        return;
                    }
                    if (msg.text?.includes("Tere tulemast")) {
                        setUsernameAccepted(true);
                        setUsernameError("");
                    }
                    setChatMessages((prev) => [...prev, msg]);
                } catch {
                    setChatMessages((prev) => [...prev, { text: e.data }]);
                }
            }
        };

        socket.onerror = (err) => console.error("WebSocket error:", err);
        socket.onclose = () => console.warn("WebSocket closed");
    };

    useEffect(() => {
        connectWebSocket();

        const fetchData = async (endpoint, setter) => {
            try {
                const res = await fetch(`${API_URL}${endpoint}`);
                const data = await res.json();
                setter(data);
            } catch {
                setter([]);
            }
        };

        Promise.all([
            fetchData("/uudised", setNewsList),
            fetchData("/paevapakkumised", setDailyDeals),
            fetchData("/ilm", setWeatherInfo),
        ]).finally(() => setLoading(false));

        return () => socketRef.current?.close();
    }, []);

    const sendChatMessage = () => {
        const msg = chatInput.trim();
        if (!msg) return;
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(msg);
            setChatInput("");
        }
    };

    const sendToBot = async (text) => {
        try {
            const res = await fetch("https://llm.utchat.ee/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ query: text }),
            });
            const data = await res.json();
            setChatHistory((prev) => [
                ...prev,
                { sender: "Sina", text },
                { sender: "Robot", text: data.response || "..." },
            ]);
        } catch {
            setChatHistory((prev) => [
                ...prev,
                { sender: "Sina", text },
                { sender: "Robot", text: "Viga serveriga." },
            ]);
        }
    };

    const handleBotSend = () => {
        const msg = botInput.trim();
        if (!msg) return;
        sendToBot(msg);
        setBotInput("");
    };

    const handleUsernameSubmit = () => {
        if (!username) {
            setUsernameError("Kasutajanimi ei saa olla tühi.");
            return;
        }

        setCookie("username", username, { maxAge: 7 * 24 * 60 * 60 });
        setUsernameAccepted(true);
        setUsernameError("");

        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(username);
        } else {
            setUsernameError("WebSocket ei ole veel ühendatud.");
        }
    };

    return (
        <>
            {!loading && (
                <>
                    {!usernameAccepted && (
                        <UsernameDialog
                            username={username}
                            onChange={(e) => setUsername(e.target.value)}
                            onSubmit={handleUsernameSubmit}
                            error={usernameError}
                        />
                    )}
                    <ThemeToggle theme={theme} onToggle={toggleTheme} />
                </>
            )}

            <div className="flex flex-col h-screen">
                <NewsTicker newsList={newsList} animate={!loading} />

                <div className="container flex-1 p-5 gap-5 font-sans flex-row">
                    <div className="fixed-flex-1 border p-3 overflow-y-auto flex flex-col">
                        <DailyDeals deals={dailyDeals} />
                        <WeatherInfo weather={weatherInfo} />
                    </div>

                    <ChatPanel
                        chatMessages={chatMessages}
                        chatInput={chatInput}
                        onSend={sendChatMessage}
                        onInputChange={(e) => setChatInput(e.target.value)}
                        chatLogRef={chatLogRef}
                    />

                    <AIChatPanel
                        chatHistory={chatHistory}
                        botInput={botInput}
                        onBotInputChange={(e) => setBotInput(e.target.value)}
                        onBotSend={handleBotSend}
                    />
                </div>
            </div>
        </>
    );
}