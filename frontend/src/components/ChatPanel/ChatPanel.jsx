import React, {useRef, useEffect, useState} from "react";
import styles from "./ChatPanel.module.css";
import TerminalInput from "../TerminalInput/TerminalInput";
import {AnimatePresence, motion} from "framer-motion";
import ThemeToggle from "@/components/ThemeToggle/ThemeToggle";
import {HelpCircle} from "lucide-react";
import EmojiPicker from "@/components/ChatPanelComponents/EmojiPicker";

export default function ChatPanel({chatMessages, onSend, chatLogRef, isActive, theme}) {
    const [userScrolledUp, setUserScrolledUp] = useState(false);
    const terminalInputRef = useRef(null);
    const [showTooltip, setShowTooltip] = useState(false);

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
            className="wide-div"
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
        >

            <div className={styles.container}>
                <div className={styles["header-row"]}>
                    <div className={styles["title-bar"]}>
                        <div className={styles["title-left"]}>
                            <h2 className={styles.title}>Vestlusplats</h2>
                            <div
                                className={styles["help-wrapper"]}
                                onMouseEnter={() => setShowTooltip(true)}
                                onMouseLeave={() => setShowTooltip(false)}
                            >
                                <HelpCircle className={styles["help-icon"]} />
                                <AnimatePresence>
                                    {showTooltip && (
                                        <motion.div
                                            className={styles["tool-tip"]}
                                            initial={{ opacity: 0, x: 10 }}
                                            animate={{ opacity: 1, x: 0 }}
                                            exit={{ opacity: 0, x: 10 }}
                                            transition={{ duration: 0.2 }}
                                        >
                                            <b>Tere tulemast UTchat veebilehele!</b> <br />
                                            Siin saad suhelda nii teiste üliõpilastega kui ka AI juturobotiga. <br />
                                            Vestlusplatsil võid luua uusi vestlusruume, liituda olemasolevatega <br />
                                            või pidada privaatselt vestlust oma sõbraga. Sõnumid säilivad <br />
                                            avalikes vestlusruumides 24 tundi, pärast mida need kustutatakse. <br />
                                            Privaatvestluse sõnumeid ei salvestata. Palume jääda suhtlemisel <br />
                                            viisakaks ning seetõttu asendatakse enim levinud vulgaarsused <br />
                                            automaatselt tärniga (*). Head suhtlemist!
                                        </motion.div>
                                    )}
                                </AnimatePresence>
                            </div>
                        </div>
                        <ThemeToggle/>
                    </div>
                </div>
                <div ref={chatLogRef} className={styles.chatLog}>
                    {chatMessages.map((msg, i) => {
                        const uniqueKey = msg.id || `msg-${i}-${msg.text.substring(0,10)}`;

                        function formatLinks(text) {
                            const urlRegex = /https:\/\/[^\s]+/g;
                            const parts = [];
                            let lastIndex = 0;
                            let match;
                            let keyIndex = 0;
                            while ((match = urlRegex.exec(text)) !== null) {
                                if (match.index > lastIndex)
                                    parts.push(text.slice(lastIndex, match.index));

                                parts.push(
                                    <a
                                        key={`link-${uniqueKey}-${keyIndex++}`}
                                        href={match[0]}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        style={{ textDecoration: "underline" }}
                                    >
                                        {match[0]}
                                    </a>
                                );
                                lastIndex = urlRegex.lastIndex;
                            }
                            if (lastIndex < text.length)
                                parts.push(text.slice(lastIndex));
                            return parts;
                        }

                        return (
                            <div
                                key={uniqueKey}
                                className={styles.message}
                                style={{
                                    color: resolveColor(msg.color)
                                }}
                            >
                                {formatLinks(msg.text)}
                            </div>
                        );
                    })}
                </div>
                <div className={styles["command-bar"]}>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => setInputText("/liitu ")}
                    >
                        Liitu ruumiga
                    </motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => setInputText("/privaat ")}
                    >
                        Privaatvestlus
                    </motion.button>
                    <motion.button
                        whileHover={{scale: 1.05}}
                        whileTap={{scale: 0.95}}
                        className={styles["command-button"]}
                        onClick={() => onSend("/ruumid")}
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
                        onClick={() => onSend("/lahku")}
                    >
                        Lahku
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