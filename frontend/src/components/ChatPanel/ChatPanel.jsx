import React, {useRef, useEffect, useState} from "react";
import styles from "./ChatPanel.module.css";
import TerminalInput from "../TerminalInput/TerminalInput";
import {motion} from "framer-motion";
import ThemeToggle from "@/components/ThemeToggle/ThemeToggle";

export default function ChatPanel({chatMessages, onSend, chatLogRef, isActive, theme}) {
    const [userScrolledUp, setUserScrolledUp] = useState(false);
    const terminalInputRef = useRef(null);

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
        const white = "#eaeaea";
        const black = "#000000";

        if (!msgColor) return theme === "dark" ? white : black;

        const normalized = msgColor.toLowerCase();

        if (theme === "dark") {
            if (normalized === black) return white;
            if (normalized === white) return white;
        } else {
            if (normalized === white) return black;
            if (normalized === black) return black;
        }
        return msgColor; // Muidu tagastab teise värvi.
    };

    const setInputText = (text) => {
        if (terminalInputRef.current?.setText) {
            terminalInputRef.current.setText(text);
        }
    }


    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
        >

            <div className={styles.container}>
                <div className={styles["header-row"]}>
                    <div className={styles["title-bar"]}>
                        <h2 className={styles.title}>Vestlusplats</h2>
                        <ThemeToggle/>
                    </div>
                </div>
                <div ref={chatLogRef} className={styles.chatLog}>
                    {chatMessages.map((msg, i) => {
                        const uniqueKey = msg.id || `msg-${i}-${msg.text.substring(0,10)}`;
                        return (
                            <div
                                key={uniqueKey}
                                className={styles.message}
                                style={{
                                    color: resolveColor(msg.color)
                                }}
                            >
                                {msg.text}
                            </div>
                        );
                    })}
                </div>
                <div className={styles["command-bar"]}>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => onSend("/vestlusruumid")}
                    > Vestlusruumid</motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => onSend("/kasutajad")}
                    >
                        Kasutajad
                    </motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => setInputText("/liitu ")}
                    >
                        Liitu
                    </motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => setInputText("/privaat ")}
                    >
                        Privaatsõnum
                    </motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => onSend("/lahku")}
                    >
                        Lahku ruumist
                    </motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => onSend("/abi")}
                    >
                        Abi
                    </motion.button>
                </div>
                <TerminalInput
                    onSubmit={onSend}
                    isActive={isActive}
                    showEmojiButton={true}
                    showHelpIcon={true}
                    placeholder={"Sisesta sõnum..."}
                    ref={terminalInputRef}
                />
            </div>
        </motion.div>
    );
}