import {useEffect, useRef, useState, forwardRef, useImperativeHandle} from "react";
import styles from "./TerminalInput.module.css"
import EmojiPicker from "@/components/ChatPanelComponents/EmojiPicker";
import { HelpCircle } from "lucide-react";
import {motion, AnimatePresence} from "framer-motion";

const TerminalInput = forwardRef(function TerminalInput({
                                          onSubmit,
                                          isActive,
                                          showEmojiButton = false,
                                          showHelpIcon = false,
                                          placeholder = "Sisesta sõnum...",

                                      }, ref) {
    const inputRef = useRef(null);
    const [isEmpty, setIsEmpty] = useState(true);
    useEffect(() => {
        if (window.innerWidth <= 800) return;
        const handleClick = (e) => {
            const isInsideEmoji = e.target.closest(".emoji-trigger") || e.target.closest(".emoji-picker");
            const isInsideAIChat = e.target.closest("textarea");
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

   useImperativeHandle(ref, () => ({
       setText: (text) => {
           if (inputRef.current) {
               inputRef.current.innerText = text;
               inputRef.current.focus();
               const range = document.createRange();
               range.selectNodeContents(inputRef.current);
               range.collapse(false);
               const selection = window.getSelection();
               selection.removeAllRanges();
               selection.addRange(range);

               setIsEmpty(text.trim() === "");
           }
       }
   }));

    return (
        <div className={styles.wrapper}>
            <div
                ref={inputRef}
                contentEditable={isActive}
                suppressContentEditableWarning
                onKeyDown={handleKeyDown}
                onInput={() => setIsEmpty(inputRef.current.innerText.trim() === "")}
                className={`${styles.input} ${isEmpty ? styles.empty : ""}`}
                data-placeholder={placeholder}
            />
            {showEmojiButton && <EmojiPicker onSelect={(emoji) => insertEmojiAtCaret(emoji.native)} />}
        </div>
    );
});

export default TerminalInput;

