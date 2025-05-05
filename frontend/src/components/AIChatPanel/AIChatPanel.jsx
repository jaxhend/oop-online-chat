import React, { useState } from "react";
import { Textarea } from "@/components/ui/chat/textarea";
import styles from "./AIChatPanel.module.css";
import { Skeleton } from "@/components/ui/Skeleton";

export default function AIChatPanel({
                                        botInput,
                                        onBotInputChange,
                                        onBotSend,
                                        chatHistory,
                                        isActive,
                                        sessionId,
                                    }) {
    const [isThinking, setIsThinking] = useState(false);
    const [response, setResponse] = useState("");

    return (
        <div className={styles.container}>
            <h3 className={styles.title}>AI Juturobot</h3>

            <div className={styles.chatLog}>
                {chatHistory.map((entry, i) => (
                    <div key={i} className={styles.message}>
                        <strong>{entry.sender}:</strong> {entry.text}
                    </div>
                ))}

                {isThinking && (
                    <div className="flex flex-col gap-2 mt-2">
                        <span className="text-muted-foreground text-sm">AI mõtleb...</span>
                        <Skeleton className="h-4 w-3/4" />
                        <Skeleton className="h-4 w-2/3" />
                        <Skeleton className="h-4 w-1/2" />
                    </div>
                )}

                {response && !isThinking && (
                    <div className={styles.responseContainer}>
                        <p>Bot: {response}</p>
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
                            onBotSend(setIsThinking, setResponse);
                        }
                    }}
                    className={styles.textarea}
                    placeholder="Sisesta küsimus..."
                    readOnly={!isActive}
                />
                <button
                    onClick={() => onBotSend(setIsThinking, setResponse)}
                    className={styles.button}
                    disabled={!isActive}
                >
                    Saada botile
                </button>
            </div>
        </div>
    );
}
