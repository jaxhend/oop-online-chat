import React, { useState, useRef, useEffect } from "react";
import NewsTicker from "./components/NewsTicker/NewsTicker";
import DailyDeals from "./components/DailyDeals/DailyDeals";
import WeatherInfo from "./components/WeatherInfo/WeatherInfo";
import UsernameDialog from "./components/UsernameDialog/UsernameDialog";
import ChatPanel from "./components/ChatPanel/ChatPanel";
import ThemeToggle from "./components/ThemeToggle/ThemeToggle";
import useTheme from "./hooks/useTheme";
import useInitialData from "./hooks/useInitialData";
import "./index.css";
import AIChatPopover from "@/components/AIChatPanel/AIChatPopover";
import useWebSocket from "@/hooks/useWebSocket";
import styles from "./main.css";

export default function OnlineChat() {
    const [username, setUsername] = useState("");
    const [usernameAccepted, setUsernameAccepted] = useState(false);
    const [usernameError, setUsernameError] = useState("");
    const [chatMessages, setChatMessages] = useState([]);
    const [botInput, setBotInput] = useState("");
    const [chatHistory, setChatHistory] = useState([]);
    const [sessionId, setSessionId] = useState(null);
    const [isLoadingSession, setIsLoadingSession] = useState(true);
    const [activeTarget, setActiveTarget] = useState("chat");
    const [isThinking, setIsThinking] = useState(false);
    const chatLogRef = useRef(null);
    const [theme, toggleTheme] = useTheme();
    const { newsList, dailyDeals, weatherInfo, loading } = useInitialData("https://api.utchat.ee");

    // Küsime serverilt sessionId ning küpsise.
    useEffect(() => {
        const fetchSession = async () => {
            setIsLoadingSession(true);
            try {
                const response = await fetch('https://api.utchat.ee/session/init', {
                    method: 'GET',
                    credentials: 'include',
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    console.error('Serveri sessionID saamise viga', response.status, errorText);
                    setIsLoadingSession(false);
                    return;
                }

                const data = await response.json();
                if (data.sessionId)
                    setSessionId(data.sessionId);
                else
                    console.error('SessionID ei leitud serveri vastusest.');
            } catch (error) {
                console.error('Error session ID saamisel:', error);
            } finally {
                setIsLoadingSession(false);
            }
        };

        fetchSession();
    }, []);


    const ws = useWebSocket(sessionId, (e) => {
        if (e.data === "__heartbeat_pong__") return;

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
    }, (socket) => {
        console.log("WebSocket ühendatud");
    });

    const handleUsernameSubmit = (value) => {
        if (!value) {
            setUsernameError("Kasutajanimi ei saa olla tühi.");
            return;
        }
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(value);
        } else {
            setUsernameError("Puudub serveriga ühendus. Proovi uuesti!");
        }
    };


    const sendToBot = async () => {
        const trimmed = botInput.trim();
        if (!trimmed) return;
        setChatHistory((prev) => [
            ...prev,
            {sender: "Sina", text: trimmed},
        ]);
        setBotInput("");
        setIsThinking(true);

        try {
            const res = await fetch("https://llm.utchat.ee/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ query: trimmed }), 
            });
            const data = await res.json();

            setChatHistory((prev) => [
                ...prev,
                { sender: "Robot", text: data.response || "..." },
            ]);
        } catch (err) {
            console.error("LLM API error:", err);
            setChatHistory((prev) => [
                ...prev,
                { sender: "Robot", text: "Serveri viga. Proovi mõne aja pärast uuesti." },
            ]);
        } finally {
            setBotInput("");
            setIsThinking(false);
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
                    <div className="scraper-info">
                        <div className={styles["wrapper"]}>
                            <DailyDeals deals={dailyDeals} />
                        </div>

                        <div className={styles["wrapper"]}>
                            <WeatherInfo weather={weatherInfo} />
                        </div>
                    </div>


                    <div className="fixed-flex-2 flex flex-col gap-4">


                        {activeTarget === "chat" && usernameAccepted && (
                            <ChatPanel
                                chatMessages={chatMessages}
                                onSend={(msg) => {
                                    if (ws && ws.readyState === WebSocket.OPEN) {
                                        ws.send(msg);
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
                        isThinking={isThinking}
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
