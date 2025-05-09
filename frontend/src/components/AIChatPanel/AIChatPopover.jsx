import React, {useState, useEffect, useRef, use} from "react";
import AIChatPanel from "@/components/AIChatPanel/AIChatPanel";
import styles from "./AIChatPopover.module.css";

export default function AIChatPopover({
                                          chatHistory,
                                          botInput,
                                          onBotInputChange,
                                          onBotSend,
                                      }) {
    const [visible, setVisible] = useState(false);
    const popoverRef = useRef();
    const [isAnimatingOut, setIsAnimatingOut] = useState(false);

    useEffect(() => {
        const handleClickOutside = (e) => {
            setTimeout(() => {
                if (popoverRef.current && !popoverRef.current.contains(e.target)) {
                    handleClose();
                }
            }, 0);
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleClose = () => {
        setIsAnimatingOut(true);
        setVisible(false);
    }

    const togglePopover = () => {
        if (visible) {
            handleClose();
        } else {
            setVisible(true);
        }
    }
    return (
        <div className={styles.wrapper}>
            <button
                className={styles.button}
                onClick={togglePopover}
                title="Ava AI Juturobot"
            >
                🤖
            </button>

            {(visible || isAnimatingOut) && (
                <div
                    className={`${styles["ai-chat-popover"]} ${
                        visible ? styles["fade-in"] : styles["fade-out"]
                    }`}
                    ref={popoverRef}
                    onAnimationEnd={() => {
                        if (!visible) setIsAnimatingOut(false);
                    }}>
                    <AIChatPanel
                        isActive={visible}
                        chatHistory={chatHistory}
                        botInput={botInput}
                        onBotInputChange={onBotInputChange}
                        onBotSend={onBotSend}
                    />
                </div>
            )}
        </div>
    );
}