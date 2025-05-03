import React, { useState, useEffect } from "react";
import { ProgressBar } from "./ProgressBar";
import { Textarea } from "@/components/ui/chat/textarea";
import styles from "./AIChatPanel.module.css";

export default function AIChatPanel({
                                        botInput,
                                        onBotInputChange,
                                        onBotSend,
                                        chatHistory,
                                        isThinking,
                                    }) {
    const [thinkingTime, setThinkingTime] = useState(0);
    const [progress, setProgress] = useState(0);


    useEffect(() => {
        if (isThinking) {
            const timer = setInterval(() => {
                setThinkingTime((prev) => prev + 1);
                setProgress((prev) => Math.min(prev + 10, 100));
            }, 1000);


            return () => clearInterval(timer);
        } else {
            setProgress(100);
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
                {isThinking ? (
                    <div className="flex flex-col items-center">
                        <p>AI mõtleb... ({thinkingTime}s)</p>
                        <ProgressBar progress={progress} />
                    </div>
                ) : (
                    <Textarea
                        value={botInput}
                        onChange={onBotInputChange}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" && !e.shiftKey) {
                                e.preventDefault();
                                onBotSend();
                            }
                        }}
                        className={styles.textarea}
                        placeholder="Sisesta küsimus..."
                    />
                )}

                <button onClick={onBotSend} className={styles.button}>
                    Saada botile
                </button>
            </div>
        </div>
    );
}