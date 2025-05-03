import React, { useState, useRef, useEffect } from "react";
import NewsTicker from "./components/NewsTicker/NewsTicker";
import DailyDeals from "./components/DailyDeals/DailyDeals";
import WeatherInfo from "./components/WeatherInfo/WeatherInfo";
import UsernameDialog from "./components/UsernameDialog/UsernameDialog";
import ChatPanel from "./components/ChatPanel/ChatPanel";
import AIChatPanel from "./components/AIChatPanel/AIChatPanel";
import ThemeToggle from "./components/ThemeToggle/ThemeToggle";
import useTheme from "./hooks/useTheme";
import useInitialData from "./hooks/useInitialData";
import {Button} from "@/components/ui/Button";
import "./index.css";

export default function OnlineChat() {
    const [username, setUsername] = useState("");
    const [usernameAccepted, setUsernameAccepted] = useState(false);
    const [usernameError, setUsernameError] = useState("");
    const [chatMessages, setChatMessages] = useState([]);
    const [chatInput, setChatInput] = useState("");
    const [botInput, setBotInput] = useState("");
    const [chatHistory, setChatHistory] = useState([]);
    const [sessionId, setSessionId] = useState(null);
    const [activeTarget, setActiveTarget] = useState("chat");

    const socketRef = useRef(null);
    const chatLogRef = useRef(null);
    const [theme, toggleTheme] = useTheme();
    const { newsList, dailyDeals, weatherInfo, loading } = useInitialData("https://api.utchat.ee");


    useEffect(() => {
        const match = document.cookie.match(/(?:^|;\s*)sessionId=([^;]+)/);
        if (match) {
            setSessionId(match[1]);
        }
    }, []);

    useEffect(() => {
        if (!sessionId) return;

        const ws = new WebSocket(`wss://api.utchat.ee/ws?sessionId=${sessionId}`);
        socketRef.current = ws;

        ws.onmessage = (e) => {
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
        };

        return () => ws.close();
    }, [sessionId]);

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

                    <div className="fixed-flex-2 flex flex-col gap-4">
                        <div className="flex gap-4 mb-2">
                            <Button
                                variant={activeTarget === "chat" ? "default" : "outline"}
                                onClick={() => setActiveTarget("chat")}
                                className={activeTarget === "chat" ? "ring-2 ring-primary" : ""}
                            >
                                Vestlusplats
                            </Button>
                            <Button
                                variant={activeTarget === "ai" ? "default" : "outline"}
                                onClick={() => setActiveTarget("ai")}
                                className={activeTarget === "ai" ? "ring-2 ring-primary" : ""}
                            >
                                AI Juturobot
                            </Button>
                        </div>


                        <ChatPanel
                            chatMessages={chatMessages}
                            onSend={(msg) => {
                                if (activeTarget === "chat" && socketRef.current?.readyState === WebSocket.OPEN) {
                                    socketRef.current.send(msg);
                                }
                            }}
                            chatLogRef={chatLogRef}
                            isActive={activeTarget === "chat"}
                        />

                        <AIChatPanel
                            chatHistory={chatHistory}
                            botInput={botInput}
                            onBotInputChange={(e) => {
                                if (activeTarget === "ai") setBotInput(e.target.value);
                            }}
                            onBotSend={() => {
                                if (activeTarget === "ai") handleBotSend();
                            }}
                            isActive={activeTarget === "ai"}
                        />
                    </div>
                </div>
            </div>
        </>
    );
}
