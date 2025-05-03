import React from "react";
import styles from "./AIChatPanel.module.css";
import { Textarea } from "@/components/ui/chat/textarea";

export default function AIChatPanel({ botInput, onBotInputChange, onBotSend, chatHistory }) {
    return (
        <div className={styles.container}>
            <h3 className={styles.title}>AI juturobot</h3>
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
                        if (e.key === 'Enter' && !e.shiftKey) {
                            e.preventDefault();
                            onBotSend();
                        }
                    }}
                    className={styles.textarea}
                    placeholder="Sisesta küsimus..."
                />
                <button onClick={onBotSend} className={styles.button}>
                    Saada botile
                </button>
            </div>
        </div>
    );
}