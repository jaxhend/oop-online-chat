import React, { useState, useEffect } from "react";
import { Textarea } from "@/components/ui/chat/textarea";
import styles from "./AIChatPanel.module.css";

export default function AIChatPanel({
  botInput,
  onBotInputChange,
  onBotSend,
  chatHistory,
}) {
  const [thinkingTime, setThinkingTime] = useState(0);
  const [isThinking, setIsThinking] = useState(false);

  const handleBotSend = () => {
    setIsThinking(true);
    setThinkingTime(0);

    const timer = setInterval(() => {
      setThinkingTime((prev) => prev + 1);
    }, 1000);

    setTimeout(() => {
      clearInterval(timer);
      onBotSend();
      setIsThinking(false);
    }, 10000);
  };

  useEffect(() => {
    if (!isThinking) {
      setThinkingTime(0);
    }
  }, [isThinking]);

  return (
    <div className={styles.container}>
      <h3 className={styles.title}>AI Juturobot</h3>
      <div className={styles.chatLog}>
        {chatHistory.map((entry, i) => (
          <div key={i} className={styles.message}>
            <strong>{entry.sender}:</strong> {entry.text}
          </div>
        ))}
      </div>
      <div className={styles.inputGroup}>
        <Textarea
          value={botInput}
          onChange={onBotInputChange}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              handleBotSend();
            }
          }}
          className={styles.textarea}
          placeholder="Sisesta küsimus..."
        />
        <button onClick={handleBotSend} className={styles.button}>
          Saada botile
        </button>
      </div>

      {isThinking && (
        <div className={styles.thinkingContainer}>
          <p>AI mõtleb...</p>
          <p>{thinkingTime} sekundi pärast...</p>
        </div>
      )}
    </div>
  );
}
