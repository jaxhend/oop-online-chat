import React, {useEffect, useState} from "react";
import styles from "./ChatPanel.module.css";
import TerminalInput from "../TerminalInput/TerminalInput";
import useTheme from "@/hooks/useTheme";

export default function ChatPanel({chatMessages, onSend, chatLogRef, isActive}) {
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
            chatLog.removeEventListener("scroll", handleScroll)
        };
    }, [chatLogRef]);

    useEffect(() => { // Kerib ise alla, kui kasutaja ei scrolli üles
        const chatLog = chatLogRef.current;
        if (!chatLog) return;
        chatLog.scrollTop = chatLog.scrollHeight;
    }, [chatMessages]);


    const resolveColor = (msgColor) => {
        const fallbackDark = "#eaeaea";
        const fallbackLight = "#1a1a1a"; // tumedam kui #000, pisut pehmem

        if (!msgColor) return theme === "dark" ? fallbackDark : fallbackLight;

        const normalized = msgColor.toLowerCase();

        const tooLightInLightMode = ["#eaeaea"];
        const tooDarkInDarkMode = ["#34495e"];

        if (theme === "dark" && tooDarkInDarkMode.includes(normalized)) {
            return fallbackDark;
        }

        if (theme === "light" && tooLightInLightMode.includes(normalized)) {
            return fallbackLight;
        }

        return msgColor;
    };


    return (
        <div className={styles.container}>
            <h2 className={styles.title}>Vestlusplats</h2>
            <div ref={chatLogRef} className={styles.chatLog}>
                {chatMessages.map((msg, i) => {
                    return (
                        <div
                            key={i} className={styles.message}
                            style={{
                                color: resolveColor(msg.color)
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
                showTooltip={true}
                placeholder={"Sisesta sõnum..."}
            />
        </div>
    );
}