import React, {useEffect, useRef, useState} from "react";
import styles from "./ChatPanel.module.css";
import TerminalInput from "../TerminalInput/TerminalInput";
import useTheme from "@/hooks/useTheme";

export default function ChatPanel({ chatMessages, onSend, chatLogRef, isActive }) {
    const [userScrolledUp, setUserScrolledUp] = useState(false);
    const [theme] = useTheme();

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


    const resolveColor = (msgColor) => {
        const fallback = theme === "dark" ? "#eaeaea" : "#000";

        if (!msgColor) return fallback;

        const tooDarkColors = ["#1f2937", "#111827", "#000", "#222", "#43B581", "#34495E"];
        if (theme === "dark" && tooDarkColors.includes(msgColor.toLowerCase())) {
            return fallback;
        }

        return msgColor;
    }


    return (
        <div className={`${styles.container} fixed-flex-2 flex flex-col border p-3`}>
            <h2 className={styles.title}>Vestlusplats</h2>
            <div ref={chatLogRef} className={styles.chatLog}>
                {chatMessages.map((msg, i) => {
                    console.log("msg.color:", msg.color);
                    return(
                    <div
                        key={i} className={styles.message}
                        style={{ color: resolveColor(msg.color)
                    }}
                    >
                        {msg.text}
                    </div>
                    );
                })}
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