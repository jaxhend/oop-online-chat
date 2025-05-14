import React, {useEffect, useRef, useState} from "react";
import {Textarea} from "@/components/ui/textarea";
import styles from "./AIChatPanel.module.css";

export default function AIChatPanel({
                                        botInput,
                                        onBotInputChange,
                                        onBotSend,
                                        chatHistory,
                                        isActive,
                                        isThinking
                                    }) {
    const chatLogRef = useRef(null);
    const lastInputRef = useRef("");
    const [dots, setDots] = useState("");
    const [charCount, setCharCount] = useState(0);
    const [needsRestoration, setNeedsRestoration] = useState(false);
    const MAX_MESSAGE_LENGTH = 300;


    useEffect(() => {
        if(isThinking)
            lastInputRef.current = botInput;
    }, [botInput]);


    useEffect(() => {
        if (isThinking) {
            setNeedsRestoration(true);
            // Input välja eelmise seisundi taastamine
        } else if (needsRestoration && botInput === '') {
            if (lastInputRef.current) {
                onBotInputChange({target: {value: lastInputRef.current}});
            }
            setNeedsRestoration(false);
        }
    }, [isThinking, botInput, needsRestoration, onBotInputChange]);


    const handleSend = async () => {
        if (!botInput.trim() || isThinking || charCount > MAX_MESSAGE_LENGTH) return;
        const processedMsg = botInput.replace(/\n/g, " ").replace(/\r/g, " ");
        onBotSend(processedMsg);
    };

    useEffect(() => {
        setCharCount(botInput.length);
    }, [botInput]);


    const getCharCountColor = () => {
        if (charCount > MAX_MESSAGE_LENGTH) return styles.charCountExceeded;
        if (charCount > MAX_MESSAGE_LENGTH * 0.9) return styles.charCountWarning;
        return styles.charCount;
    };

    useEffect(() => {
        const el = chatLogRef.current;
        if (!el) return;
        el.scrollTop = el.scrollHeight;
    }, [chatHistory, isThinking])


    useEffect(() => {
        if (!isThinking) {
            setDots("");
            return;
        }
        const interval = setInterval(() => {
            setDots(prev => {
                if (prev.length >= 5) return "";
                return prev + ".";
            });
        }, 500);
        return () => clearInterval(interval);
    }, [isThinking]);


    return (
        <div className={styles.container}>
            <h3 className={styles.title}>AI Juturobot</h3>

            <div className={styles.chatLog} ref={chatLogRef}>
                {chatHistory.map((entry, i) => (
                    <div key={i} className={styles.message}>
                        <strong>{entry.sender}:</strong> {entry.text}
                    </div>
                ))}

                {isThinking && (
                    <div className="flex flex-col gap-2 mt-2">
                        <span className="text-muted-foreground text-sm">AI mõtleb{dots}</span>
                    </div>
                )}
            </div>

            <div className={styles.inputGroup}>
                <Textarea
                    value={botInput}
                    onChange={(e) => {
                        if (isActive) onBotInputChange(e);
                    }}
                    onKeyDown={(e) => {
                        if (e.key === "Enter" && !e.shiftKey && isActive) {
                            e.preventDefault();
                            handleSend();
                        }
                    }}
                    className={`${styles.textarea}`}
                    placeholder={"Sisesta küsimus..."}
                />
                <div className={getCharCountColor()}>
                    {charCount}/{MAX_MESSAGE_LENGTH}
                </div>
                <button
                    onClick={handleSend}
                    className={`${styles.button} 
                                ${(isThinking || charCount > MAX_MESSAGE_LENGTH) ? styles.notAllowed : ''}`}
                    disabled={!isActive || isThinking || !botInput.trim() || charCount > MAX_MESSAGE_LENGTH}
                >
                    Saada
                </button>
            </div>
        </div>
    );
}
