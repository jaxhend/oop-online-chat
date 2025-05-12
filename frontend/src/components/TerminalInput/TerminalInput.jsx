import {useEffect, useRef, useState} from "react";
import styles from "./TerminalInput.module.css"
import EmojiPicker from "@/components/ui/EmojiPicker";

export default function TerminalInput({onSubmit, isActive, showEmojiButton = false, placeholder = "Sisesta sõnum..."}) {
    const inputRef = useRef(null);
    const [isEmpty, setIsEmpty] = useState(true);

    useEffect(() => {
        const handleClick = (e) => {
            const isInsideEmoji = e.target.closest('.emoji-trigger') || e.target.closest('.emoji-picker');
            const isInsideAIChat = e.target.closest('textarea');
            if (isActive && inputRef.current && !isInsideEmoji && !isInsideAIChat) {
                inputRef.current.focus();
            }
        };
        document.addEventListener("click", handleClick);
        return () => {
            document.removeEventListener("click", handleClick);
        };

    }, [isActive]);

    const insertEmojiAtCaret = (emoji) => {
        const el = inputRef.current;
        el.focus();
        const selection = window.getSelection();
        const range = selection.getRangeAt(0);
        range.deleteContents();
        range.insertNode(document.createTextNode(emoji));
        range.collapse(false);
        selection.removeAllRanges();
        selection.addRange(range);

        setIsEmpty(el.innerText.trim() === "");
    };

    const handleKeyDown = (e) => {
        if (!isActive) return;

        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            const text = inputRef.current.innerText.trim();
            if (text) {
                onSubmit(text);
                inputRef.current.innerText = "";
                setIsEmpty(true);
            }
        }
    };

    return (
        <div className={styles.wrapper}>
            <div
                ref={inputRef}
                contentEditable={isActive}
                suppressContentEditableWarning
                onKeyDown={handleKeyDown}
                onInput={() => setIsEmpty(inputRef.current.innerText.trim() === "")}
                className={`${styles.input} ${isEmpty ? styles.empty : ''}`}
                data-placeholder={placeholder}
            />
            {showEmojiButton && <EmojiPicker onSelect={(emoji) => insertEmojiAtCaret(emoji.native)}/>}
        </div>
    );
}