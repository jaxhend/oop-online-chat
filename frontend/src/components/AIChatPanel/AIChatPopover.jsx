import React, {useEffect, useRef, useState} from "react";
import AIChatPanel from "@/components/AIChatPanel/AIChatPanel";
import styles from "./AIChatPopover.module.css";
import {AnimatePresence, motion} from "framer-motion";

export default function AIChatPopover({
                                          chatHistory,
                                          botInput,
                                          onBotInputChange,
                                          isThinking,
                                          onBotSend,
                                      }) {
    const [visible, setVisible] = useState(false);
    const popoverRef = useRef();
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 800);

    useEffect(() => {
        const handler = () => setIsMobile(window.innerWidth <= 800);
        window.addEventListener("resize", handler);
        return () => window.removeEventListener("resize", handler);
    }, []);


    useEffect(() => {
        const handleClickOutside = (e) => {
            setTimeout(() => {
                if (popoverRef.current && !popoverRef.current.contains(e.target)) {
                    setVisible(false);
                }
            }, 0);
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);


    const togglePopover = () => {
        setVisible((prev => !prev));
    }
    return isMobile ?  (
        <div className={styles["mobile-layout"]}>
            <AIChatPanel
                isThinking={isThinking}
                isActive={true}
                chatHistory={chatHistory}
                botInput={botInput}
                onBotInputChange={onBotInputChange}
                onBotSend={onBotSend}
            />
        </div>
    ) : (
        <div className={styles.wrapper}>
            <AnimatePresence>
                {!visible && (
                    <motion.button
                        className={styles["bot-button"]}
                        onClick={togglePopover}
                        title="Ava AI Juturobot"
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        transition={{duration: 0.6, ease: "easeOut"}}
                        whileHover={{scale: 1.1}}

                    >
                        🤖
                    </motion.button>
                )}
            </AnimatePresence>

            <AnimatePresence>
                {visible && (
                    <motion.div
                        className={styles["popover"]}
                        ref={popoverRef}
                        initial={{opacity: 0, y: 10}}
                        animate={{opacity: 1, y: 0}}
                        exit={{opacity: 0, y: 10}}
                        transition={{duration: 0.3}}
                    >
                        <AIChatPanel
                            isThinking={isThinking}
                            isActive={visible}
                            chatHistory={chatHistory}
                            botInput={botInput}
                            onBotInputChange={onBotInputChange}
                            onBotSend={onBotSend}
                        />
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}