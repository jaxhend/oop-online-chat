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
import { Button } from "@/components/ui/Button";
import "./index.css";
import AIChatPopover from "@/components/AIChatPanel/AIChatPopover";

export default function OnlineChat() {
    const [username, setUsername] = useState("");
    const [usernameAccepted, setUsernameAccepted] = useState(false);
    const [usernameError, setUsernameError] = useState("");
    const [chatMessages, setChatMessages] = useState([]);
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
        } else {
            const newId = crypto.randomUUID();
            document.cookie = `sessionId=${newId}; path=/`;
            setSessionId(newId);
        }
    }, []);

    useEffect(() => {
        if (!sessionId) return;

        const ws = new WebSocket(`wss://api.utchat.ee/ws?sessionId=${sessionId}`);
        //const ws = new WebSocket(`ws://localhost:8080/ws?sessionId=${sessionId}`);
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

    const handleUsernameSubmit = (value) => {
        if (!value) {
            setUsernameError("Kasutajanimi ei saa olla tühi.");
            return;
        }
        if (socketRef.current?.readyState === WebSocket.OPEN) {
            socketRef.current.send(value);
        } else {
            setUsernameError("WebSocket ei ole veel ühendatud.");
        }
    };


    const sendToBot = async () => {
        const trimmed = botInput.trim();
        if (!trimmed) return;

        try {
            const res = await fetch("https://llm.utchat.ee/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ query: trimmed }), 
            });
            const data = await res.json();

            setChatHistory((prev) => [
                ...prev,
                { sender: "Sina", text: trimmed },
                { sender: "Robot", text: data.response || "..." },
            ]);
        } catch (err) {
            console.error("Bot fetch error:", err);
            setChatHistory((prev) => [
                ...prev,
                { sender: "Sina", text: trimmed },
                { sender: "Robot", text: "Flask viga: Serveriga ühenduse loomisel tekkis viga." },
            ]);
        } finally {
            setBotInput("");
        }
    };

    return (
        <>
            {!loading && !usernameAccepted && (
                <UsernameDialog onSubmit={handleUsernameSubmit} error={usernameError} />
            )}

            {!loading && usernameAccepted && (
                <div className="absolute top-4 right-4">
                    <ThemeToggle theme={theme} onToggle={toggleTheme} />
                </div>
            )}

            <div className="flex flex-col h-screen">
                <NewsTicker newsList={newsList} animate={!loading} />

                <div className="container flex-1 p-5 gap-5 font-sans flex-row">
                    <div className="fixed-flex-1 border p-3 overflow-y-auto flex flex-col">
                        <DailyDeals deals={dailyDeals} />
                        <WeatherInfo weather={weatherInfo} />
                    </div>


                    <div className="fixed-flex-2 flex flex-col gap-4">


                        {activeTarget === "chat" && usernameAccepted && (
                            <ChatPanel
                                chatMessages={chatMessages}
                                onSend={(msg) => {
                                    if (
                                        socketRef.current?.readyState === WebSocket.OPEN
                                    ) {
                                        socketRef.current.send(msg);
                                    }
                                }}
                                chatLogRef={chatLogRef}
                                isActive={true}
                            />
                        )}

                    </div>
                </div>

                { /*activeTarget === "ai" &&*/ usernameAccepted && (
                    <AIChatPopover
                        chatHistory={chatHistory}
                        botInput={botInput}
                        onBotInputChange={(e) => setBotInput(e.target.value)}
                        onBotSend={sendToBot}
                        isActive={true}
                    />
                )}
            </div>
        </>
    );
}
