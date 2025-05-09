import React, {useEffect, useRef, useState} from "react";
import styles from "./ChatPanel.module.css";
import TerminalInput from "../TerminalInput/TerminalInput";

export default function ChatPanel({ chatMessages, onSend, chatLogRef, isActive }) {
    const [userScrolledUp, setUserScrolledUp] = useState(false);

    useEffect(() => { // Kontrollib, kas kasutaja on üles scrollinud
        const chatLog = chatLogRef.current;
        if (!chatLog) return;

        const handleScroll = () => {
            const nearBottom = chatLog.scrollHeight - chatLog.scrollTop - chatLog.clientHeight < 50;
            setUserScrolledUp(!nearBottom);
        };
        chatLog.addEventListener("scroll", handleScroll);
        return () => {
            chatLog.removeEventListener("scroll", handleScroll())
        };
    }, [chatLogRef]);

    useEffect(() => { // Kerib ise alla, kui kasutaja ei scrolli üles
        const chatLog = chatLogRef.current;
        if (!chatLog) return;
        chatLog.scrollTop = chatLog.scrollHeight;
    }, [chatMessages]);

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
            <TerminalInput
                onSubmit={onSend}
                isActive={isActive}
                showEmojiButton={true}
                placeholder={"Sisesta sõnum..."}
            />
        </div>
    );
}