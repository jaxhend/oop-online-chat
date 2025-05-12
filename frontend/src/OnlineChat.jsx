import React, {useEffect, useRef, useState} from "react";
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


export default function OnlineChat() {
    const [usernameAccepted, setUsernameAccepted] = useState(false);
    const [usernameError, setUsernameError] = useState("");
    const [chatMessages, setChatMessages] = useState([]);
    const [botInput, setBotInput] = useState("");
    const [chatHistory, setChatHistory] = useState([]);
    const [sessionId, setSessionId] = useState(null);
    const [isThinking, setIsThinking] = useState(false);
    const chatLogRef = useRef(null);
    const [theme, toggleTheme] = useTheme();
    // const API_URL = "http://localhost:8080";
    const API_URL = "https://api.utchat.ee";
    const LLM_URL = "https://llm.utchat.ee/chatbot";
    const {newsList, dailyDeals, weatherInfo, loading} = useInitialData(API_URL);

    // Küsime serverilt sessionId ning küpsise.
    useEffect(() => {
        const fetchSession = async () => {
            try {
                const response = await fetch(API_URL + '/session/init', {
                    method: 'GET',
                    credentials: 'include',
                });

                if (!response.ok) {
                    const errorText = await response.text();
                    console.error('Serveri sessionID saamise viga', response.status, errorText);
                    return;
                }

                const data = await response.json();
                if (data.sessionId)
                    setSessionId(data.sessionId);
                else
                    console.error('SessionID ei leitud serveri vastusest.');
            } catch (error) {
                console.error('Error session ID saamisel:', error);
            }
        };

        fetchSession();
    }, []);


    const ws = useWebSocket(sessionId, (e) => {
        // Message handler
        if (e.data === "__heartbeat_pong__") return;

        try {
            const msg = JSON.parse(e.data);

            if (msg.text?.includes("Tere tulemast")) {
                setUsernameAccepted(true);
            }
            setUsernameError("");

            setChatMessages((prev) => [...prev, msg]);
        } catch {
            const text = e.data;

            if (text.includes("Kasutajanimi")) {
                setUsernameError(text);
                setUsernameAccepted(false);
                return;
            }

            setChatMessages((prev) => [...prev, {text: e.data}]);
        }
    }, (socket) => {
        console.log("WebSocket ühendatud");
    });

    const handleUsernameSubmit = (value) => {
        if (!ws) {
            setUsernameError("Puudub sessiooni ID. Proovi lehte värskendada.");
            return;
        }
        const socket = ws.current;
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.send(value);
        } else {
            setUsernameError("Puudub serveriga ühendus. Proovi uuesti!");
        }
    };


    const sendToBot = async () => {
        const trimmed = botInput.trim();
        if (!trimmed) return;
        setChatHistory((prev) => [
            ...prev, {sender: "Sina", text: trimmed},]);
        setBotInput("");
        setIsThinking(true);

        try {
            const res = await fetch(LLM_URL, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({query: trimmed}),
            });
            const data = await res.json();
            setIsThinking(false);

            setChatHistory((prev) => [
                ...prev,
                {sender: "Robot", text: data.response || "..."},
            ]);
        } catch (err) {
            console.error("LLM API error:", err);
            setChatHistory((prev) => [
                ...prev,
                {sender: "Robot", text: "Serveri viga. Proovi mõne aja pärast uuesti."},
            ]);
        } finally {
            setBotInput("");
            setIsThinking(false);
        }
    };

    return (
        <>
            {!usernameAccepted && (
                <UsernameDialog onSubmit={handleUsernameSubmit} error={usernameError}/>
            )}

            {!loading && usernameAccepted && (
                <div className="absolute top-4 right-4">
                    <ThemeToggle theme={theme} onToggle={toggleTheme}/>
                </div>
            )}

            <div className="flex flex-col h-screen">
                <NewsTicker newsList={newsList} animate={!loading}/>

                <div className="main-layout">

                    <div className="chat-panel">

                        {usernameAccepted && (
                            <ChatPanel
                                chatMessages={chatMessages}
                                onSend={(msg) => {
                                    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
                                        ws.current.send(msg);
                                    }
                                }}
                                chatLogRef={chatLogRef}
                                isActive={true}
                            />
                        )}

                    </div>

                    <div className="info-panel">
                        <div className={"container"}>
                            <DailyDeals deals={dailyDeals} />
                        </div>
                        <div className={"container"}>
                            <WeatherInfo weather={weatherInfo} />
                        </div>

                    </div>
                </div>

                {usernameAccepted && (
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
