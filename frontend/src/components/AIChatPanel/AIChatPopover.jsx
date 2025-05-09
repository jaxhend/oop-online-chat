import React, {useState, useEffect, useRef, use} from "react";
import AIChatPanel from "@/components/AIChatPanel/AIChatPanel";
import styles from "./AIChatPopover.module.css";
import { motion, AnimatePresence } from "framer-motion";

export default function AIChatPopover({
                                          chatHistory,
                                          botInput,
                                          onBotInputChange,
                                          isThinking,
                                          onBotSend,
                                      }) {
    const [visible, setVisible] = useState(false);
    const popoverRef = useRef();


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
    return (
        <div className={styles.wrapper}>
            <AnimatePresence>
                {!visible && (
                <motion.button
                    className={styles["bot-button"]}
                    onClick={togglePopover}
                    title="Ava AI Juturobot"
                    initial={{opacity: 0, y:20}}
                    animate={{opacity:1, y:0}}
                    exit={{opacity: 0, y:20}}
                    transition={{duration:0.6, ease:"easeOut"}}
                    whileHover={{scale:1.1}}

                >
                    🤖
                </motion.button>
                )}
            </AnimatePresence>

            <AnimatePresence>
                {visible && (
                    <motion.div
                        className={styles["ai-chat-popover"]}
                        ref={popoverRef}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: 10 }}
                        transition={{ duration: 0.3 }}
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