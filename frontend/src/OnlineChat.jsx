import React, {useEffect, useRef, useState} from "react";
import NewsTicker from "./components/NewsTicker/NewsTicker";
import DailyDeals from "./components/DailyDeals/DailyDeals";
import WeatherInfo from "./components/WeatherInfo/WeatherInfo";
import UsernameDialog from "./components/UsernameDialog/UsernameDialog";
import ChatPanel from "./components/ChatPanel/ChatPanel";
import useTheme from "./hooks/useTheme";
import useInitialData from "./hooks/useInitialData";
import AIChatPopover from "./components/AIChatPanel/AIChatPopover";
import useWebSocket from "./hooks/useWebSocket";
import "./index.css";
import "./main.css";
import utLogo from "./assets/ut-logo.webp";
import Footer from "@/components/Footer/Footer";

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
    //const API_URL = "http://localhost:8080";
    const API_URL = "https://api.utchat.ee";
    const LLM_URL = "https://llm.utchat.ee/chatbot";
    const {newsList, dailyDeals, weatherInfo, loading} = useInitialData(API_URL);

    const originalTitleRef = useRef(document.title);
    const isTabActiveRef = useRef(true);

    useEffect(() => {
        const handleVisibility = () => {
            isTabActiveRef.current = document.visibilityState === "visible";
            if (isTabActiveRef.current) {
                document.title = originalTitleRef.current;
            }
        };
        document.addEventListener("visibilitychange", handleVisibility);
        return () => document.removeEventListener("visibilitychange", handleVisibility);
    }, []);


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
        if (e.data === "__heartbeat_pong__") return;

        try {
            const msg = JSON.parse(e.data);

            if (msg.text?.includes("Tere tulemast"))
                setUsernameAccepted(true);

            if (!isTabActiveRef.current) {
                document.title = "🔔 Uus sõnum!";
            }

            setUsernameError("");
            setChatMessages((prev) => [...prev, msg]);
        } catch {
            const text = e.data;
            setUsernameError(text);
            setUsernameAccepted(false);
        }
    });

    const handleUsernameSubmit = (value) => {
        if (!ws?.current) {
            setUsernameError("Puudub sessiooni ID. Proovi lehte värskendada.");
            return;
        }
        const socket = ws.current;
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(value);
        } else {
            setUsernameError("Puudub serveriga ühendus. Proovi uuesti!");
        }
    };

    const sendToBot = async () => {
        const trimmed = botInput.trim();
        if (!trimmed) return;

        setChatHistory((prev) => [...prev, {sender: "Sina", text: trimmed}]);
        setBotInput("");
        setIsThinking(true);

        try {
            const res = await fetch(LLM_URL, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({query: trimmed}),
            });
            const data = await res.json();
            setChatHistory((prev) => [...prev, {sender: "Robot", text: data.response || "..."}]);
        } catch (err) {
            setChatHistory((prev) => [...prev, {
                sender: "Robot",
                text: "Serveri viga. Proovi mõne aja pärast uuesti."
            }]);
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


            <div className="flex flex-col">
                <div className="header-info">
                    <div className="header-text">
                        <strong>Tere tulemast UTchat veebilehele!</strong><br />
                        <div className="footer-content">
                            <p>
                                Siin saad suhelda nii teiste üliõpilastega kui ka AI juturobotiga.
                                Vestlusplatsil võid luua uusi vestlusruume, liituda olemasolevatega või pidada privaatset vestlust oma sõbraga.
                                Sõnumid säilivad avalikes vestlusruumides 24 tundi, pärast mida need kustutatakse.
                                Privaatvestluse sõnumeid ei salvestata.
                                Palume jääda suhtlemisel viisakaks ning seetõttu asendatakse enim levinud vulgaarsused automaatselt tärniga (*).
                                Head suhtlemist!
                            </p>
                        </div>
                    </div>
                    <div className="header-logo">
                        <img src={utLogo} alt="TÜ logo"/>
                    </div>
                </div>
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
                                theme={theme}
                            />
                        )}
                    </div>

                    <div className="info-panel">
                        <div className="container">
                            {usernameAccepted && (
                                <DailyDeals deals={dailyDeals}/>
                            )}
                        </div>
                        <div className="container">
                            {usernameAccepted && (
                                <WeatherInfo weather={weatherInfo}/>
                            )}
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
                <Footer/>

            </div>
        </>
    );
}