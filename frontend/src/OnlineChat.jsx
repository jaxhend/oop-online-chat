import React, { useState, useRef, useEffect } from "react";
import NewsTicker from "./components/NewsTicker/NewsTicker";
import DailyDeals from "./components/DailyDeals/DailyDeals";
import WeatherInfo from "./components/WeatherInfo/WeatherInfo";
import UsernameDialog from "./components/UsernameDialog/UsernameDialog";
import ChatPanel from "./components/ChatPanel/ChatPanel";
import AIChatPanel from "./components/AIChatPanel/AIChatPanel";
import ThemeToggle from "./components/ThemeToggle/ThemeToggle";
import useTheme from "./hooks/useTheme";
import useWebSocket from "./hooks/useWebSocket";
import useInitialData from "./hooks/useInitialData";
import { useCookies } from "react-cookie";
import "./index.css";

export default function OnlineChat() {
    const [username, setUsername] = useState("");
    const [usernameAccepted, setUsernameAccepted] = useState(false);
    const [usernameError, setUsernameError] = useState("");
    const [chatMessages, setChatMessages] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [botInput, setBotInput] = useState("");
    const [chatHistory, setChatHistory] = useState([]);
    const [initializing, setInitializing] = useState(true);
    const [cookies, setCookie] = useCookies(["sessionId"]);

    const sessionId = useRef(
        localStorage.getItem("sessionId") || (() => {
            const newId = crypto.randomUUID();
            localStorage.setItem("sessionId", newId);
            return newId;
        })()
    );
    
    useEffect(() => {
        if (!cookies.sessionId) {
            setCookie("sessionId", sessionId.current, { path: "/", maxAge: 7 * 24 * 60 * 60 });
        }
    }, [cookies, setCookie]);

    const chatLogRef = useRef(null);
    const [theme, toggleTheme] = useTheme();
    const { newsList, dailyDeals, weatherInfo, loading } = useInitialData("https://api.utchat.ee");

    const socketRef = useWebSocket(
        sessionId.current,
        (e) => {
            if (e.data === "pong") return;
            try {
                const msg = JSON.parse(e.data);

                if (msg.text?.includes("Tere tulemast")) {
                    const match = msg.text.match(/Tere tulemast,\s*(.+?)!/);
                    const extractedName = match?.[1]?.trim();

                    if (extractedName) {
                        setUsername(extractedName);
                        setUsernameAccepted(true);
                    }
                    setUsernameError("");
                }

                if (msg.text?.toLowerCase().includes("kasutajanimi on keelatud")) {
                    setUsernameError(msg.text);
                    setUsernameAccepted(false);
                }

                setChatMessages((prev) => [...prev, msg]);
            } catch {
                setChatMessages((prev) => [...prev, { text: e.data }]);
            }
        }
    );

    useEffect(() => {
        const socket = socketRef.current;

        if (usernameAccepted) {
            setInitializing(false);
            return;
        }

        const trySessionLogin = () => {
            if (socket?.readyState === WebSocket.OPEN) {
                socket.send(sessionId.current);
                setTimeout(() => setInitializing(false), 500);
            } else {
                const interval = setInterval(() => {
                    if (socket?.readyState === WebSocket.OPEN) {
                        socket.send(sessionId.current);
                        clearInterval(interval);
                        setTimeout(() => setInitializing(false), 500);
                    }
                }, 100);
                return () => clearInterval(interval);
            }
        };

        trySessionLogin();
    }, [usernameAccepted]);

    const handleUsernameSubmit = () => {
        if (!username) {
            setUsernameError("Kasutajanimi ei saa olla tühi.");
            return;
        }
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(username);
        } else {
            setUsernameError("WebSocket ei ole veel ühendatud.");
        }
    };

    const sendChatMessage = () => {
        const msg = chatInput.trim();
        if (msg && socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(msg);
            setChatInput("");
        }
    };

    const handleBotSend = async () => {
        const text = botInput.trim();
        if (!text) return;
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
        setBotInput("");
    };

    return (
        <>
            {!loading && !initializing && (
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
