import React from "react";
import styles from "./ChatPanel.module.css";

export default function ChatPanel({
                                      chatMessages,
                                      chatInput,
                                      onSend,
                                      onInputChange,
                                      chatLogRef,
                                  }) {
    return (
        <div className={`${styles.container} fixed-flex-2 flex flex-col border p-3`}>
            <h2 className={styles.title}>Vestlusplats</h2>
            <div ref={chatLogRef} className={styles.chatLog}>
                {chatMessages.map((msg, i) => (
                    <div key={i} className={styles.message} style={{ color: msg.color || "#000" }}>
                        {msg.text}
                    </div>
                ))}
            </div>
            <div className={styles.inputGroup}>
        <textarea
            rows={1}
            value={chatInput}
            onChange={onInputChange}
            onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                    e.preventDefault();
                    onSend();
                }
            }}
            className={styles.textarea}
            placeholder="Sisesta sõnum..."
        />
                <button onClick={onSend} className={styles.button}>
                    Saada
                </button>
            </div>
        </div>
    );
}