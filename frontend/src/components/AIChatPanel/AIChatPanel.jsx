import React, {useEffect, useRef, useState} from "react";
import {Textarea} from "@/components/ChatPanelComponents/textarea";
import styles from "./AIChatPanel.module.css";
import { HelpCircle } from "lucide-react";
import {AnimatePresence, motion} from "framer-motion";


export default function AIChatPanel({
                                        botInput,
                                        onBotInputChange,
                                        onBotSend,
                                        chatHistory,
                                        isActive,
                                        isThinking
                                    }) {
    const chatLogRef = useRef(null);
    const lastInputRef = useRef("");
    const [dots, setDots] = useState("");
    const [charCount, setCharCount] = useState(0);
    const [needsRestoration, setNeedsRestoration] = useState(false);
    const MAX_MESSAGE_LENGTH = 300;
    const [showTooltip, setShowTooltip] = useState(false);
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 800);

    useEffect(() => {
        const handleResize = () => setIsMobile(window.innerWidth <= 800);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    useEffect(() => {
        if(isThinking)
            lastInputRef.current = botInput;
    }, [botInput]);


    useEffect(() => {
        if (isThinking) {
            setNeedsRestoration(true);
            // Input välja eelmise seisundi taastamine
        } else if (needsRestoration && botInput === '') {
            if (lastInputRef.current) {
                onBotInputChange({target: {value: lastInputRef.current}});
            }
            setNeedsRestoration(false);
        }
    }, [isThinking, botInput, needsRestoration, onBotInputChange]);


    const handleSend = async () => {
        if (!botInput.trim() || isThinking || charCount > MAX_MESSAGE_LENGTH) return;
        const processedMsg = botInput.replace(/\n/g, " ").replace(/\r/g, " ");
        onBotSend(processedMsg);
    };

    useEffect(() => {
        setCharCount(botInput.length);
    }, [botInput]);


    const getCharCountColor = () => {
        if (charCount > MAX_MESSAGE_LENGTH) return styles.charCountExceeded;
        if (charCount > MAX_MESSAGE_LENGTH * 0.9) return styles.charCountWarning;
        return styles.charCount;
    };

    useEffect(() => {
        const el = chatLogRef.current;
        if (!el) return;
        el.scrollTop = el.scrollHeight;
    }, [chatHistory, isThinking])


    useEffect(() => {
        if (!isThinking) {
            setDots("");
            return;
        }
        const interval = setInterval(() => {
            setDots(prev => {
                if (prev.length >= 5) return "";
                return prev + ".";
            });
        }, 500);
        return () => clearInterval(interval);
    }, [isThinking]);


    return (
        <div className={styles.container}>
            <div className={styles["title-row"]}>
                <h3 className={styles.titleWithIcon}>
                    AI Juturobot
                    <span
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
                        Mudel: Qwen3-3B <br />
                        Server: Tartu Ülikooli HPC <br />
                        Teie sõnumeid ei salvestata! <br />
                        Hetke teadmised:
                        <ul className={styles.listItems}>
                            <li>TÜ õppekavad</li>
                            <li>TÜ õppeained</li>
                            <li>Lehekülje <a href="https://ut.ee" target="_blank">ut.ee</a> sisu</li>
                            <li>Lehekülje <a href="https://cs.ut.ee" target="_blank">cs.ut.ee</a> sisu</li>
                        </ul>
                        NB! Palun arvestage, et tehisaru <br/>
                        võib anda ebatäpseid vastuseid.
                    </motion.div>
                )}
            </AnimatePresence>
        </span>
                </h3>
            </div>

            <div className={styles.chatLog} ref={chatLogRef}>
                {chatHistory.map((entry, i) => (
                    <div key={i} className={styles.message}>
                        <strong>{entry.sender}:</strong> {entry.text}
                    </div>
                ))}

                {isThinking && (
                    <div className="flex flex-col gap-2 mt-2">
                        <span className="text-muted-foreground text-sm">AI mõtleb{dots}</span>
                    </div>
                )}
            </div>

            <div className={styles.inputGroup}>
                <div className={styles.textareaWrapper}>
                    <Textarea
                        value={botInput}
                        onChange={(e) => isActive && onBotInputChange(e)}
                        onKeyDown={(e) => {
                            if (e.key === "Enter" && !e.shiftKey && isActive) {
                                e.preventDefault();
                                handleSend();
                            }
                        }}
                        className={styles.textarea}
                        placeholder="Sisesta küsimus..."
                    />

                    <div className={getCharCountColor()}>
                        {charCount}/{MAX_MESSAGE_LENGTH}
                    </div>
                </div>

                {!isMobile && (
                    <div className={styles.sendArea}>
                        <button
                            onClick={handleSend}
                            className={`${styles.button} ${(isThinking || charCount > MAX_MESSAGE_LENGTH) ? styles.notAllowed : ''}`}
                            disabled={!isActive || isThinking || !botInput.trim() || charCount > MAX_MESSAGE_LENGTH}
                        >
                            Saada
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}