import React, { useState, useEffect } from "react";
import { Textarea } from "@/components/ui/chat/textarea";
import styles from "./AIChatPanel.module.css";

export default function AIChatPanel({
                                        botInput,
                                        onBotInputChange,
                                        onBotSend,
                                        chatHistory,
                                    }) {
    const [thinkingTime, setThinkingTime] = useState(0);
    const [isThinking, setIsThinking] = useState(false);
    const [response, setResponse] = useState("");
    const [dots, setDots] = useState("");

    const handleBotSend = () => {
        setIsThinking(true);
        setThinkingTime(0);
        setResponse("");
        setDots("");

        const dotTimer = setInterval(() => {
            setDots((prev) => (prev.length < 5 ? prev + "." : prev));
        }, 1000);

        sendToFlask(botInput);


        setTimeout(() => {
            clearInterval(dotTimer);
            setIsThinking(false);
        }, 10000);
    };

    const sendToFlask = async (text) => {
        try {
            const response = await fetch("https://llm.utchat.ee/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ query: text }),
            });
            const data = await response.json();
            setResponse(data.response || "Viga vastuse saamisel");
        } catch {
            setResponse("Flask viga: Serveriga ühenduse loomisel tekkis viga.");
        }
    };

    useEffect(() => {
        if (!isThinking) {
            setThinkingTime(0);
        }
    }, [isThinking]);

    return (
        <div className={styles.container}>
            <h3 className={styles.title}>AI Juturobot</h3>
            <div className={styles.chatLog}>
                {chatHistory.map((entry, i) => (
                    <div key={i} className={styles.message}>
                        <strong>{entry.sender}:</strong> {entry.text}
                    </div>
                ))}
            </div>
            <div className={styles.inputGroup}>
                <Textarea
                    value={botInput}
                    onChange={onBotInputChange}
                    onKeyDown={(e) => {
                        if (e.key === "Enter" && !e.shiftKey) {
                            e.preventDefault();
                            handleBotSend();
                        }
                    }}
                    className={styles.textarea}
                    placeholder="Sisesta küsimus..."
                />
                <button onClick={handleBotSend} className={styles.button}>
                    Saada botile
                </button>
            </div>

            {isThinking && (
                <div className={styles.thinkingContainer}>
                    <p>AI mõtleb{dots}</p>
                </div>
            )}

            {response && !isThinking && (
                <div className={styles.responseContainer}>
                    <p>Bot: {response}</p>
                </div>
            )}
        </div>
    );
}