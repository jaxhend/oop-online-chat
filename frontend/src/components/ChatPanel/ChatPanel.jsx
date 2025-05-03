import React from "react";
import styles from "./ChatPanel.module.css";
import TerminalInput from "../TerminalInput/TerminalInput";

export default function ChatPanel({ chatMessages, onSend, chatLogRef, isActive }) {
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
            <TerminalInput onSubmit={onSend} isActive={isActive} />
        </div>
    );
}