import React, {useState, useEffect, useRef} from "react";
import { Textarea } from "@/components/ui/chat/textarea";
import styles from "./AIChatPanel.module.css";
import { Skeleton } from "@/components/ui/Skeleton";

export default function AIChatPanel({
                                        botInput,
                                        onBotInputChange,
                                        onBotSend,
                                        chatHistory,
                                        isActive,
                                    }) {
    const [isThinking, setIsThinking] = useState(false);
    const [response, setResponse] = useState("");
    const chatLogRef = useRef(null);

    const handleSend = async () => {
        if (!botInput.trim()) return;


        setIsThinking(true);
        setResponse("");

        await onBotSend(botInput);

        setTimeout(() => {
            setIsThinking(false);
        }, 0);
    };

    useEffect(() => {
        if (!isThinking) {
            setResponse("");
        }
    }, [isThinking]);

    useEffect(() => {
        const el = chatLogRef.current;
        if (!el) return;
        el.scrollTop = el.scrollHeight;
    }, [chatHistory, isThinking])

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
                        <span className="text-muted-foreground text-sm">AI mõtleb...</span>
                        <Skeleton className="h-4 w-3/4" />
                        <Skeleton className="h-4 w-2/3" />
                        <Skeleton className="h-4 w-1/2" />
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
                    className={styles.textarea}
                    placeholder="Sisesta küsimus..."
                />
                <button
                    onClick={handleSend}
                    className={styles.button}
                    disabled={!isActive}
                >
                    Saada
                </button>
            </div>
        </div>
    );
}
