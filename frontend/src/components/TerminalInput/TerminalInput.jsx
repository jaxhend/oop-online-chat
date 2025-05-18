import {useEffect, useRef, useState} from "react";
import styles from "./TerminalInput.module.css"
import EmojiPicker from "@/components/ChatPanelComponents/EmojiPicker";
import { HelpCircle } from "lucide-react";
import {motion, AnimatePresence} from "framer-motion";

export default function TerminalInput({
                                          onSubmit,
                                          isActive,
                                          showEmojiButton = false,
                                          showHelpIcon = false,
                                          placeholder = "Sisesta sõnum...",
                                      }) {
    const inputRef = useRef(null);
    const [isEmpty, setIsEmpty] = useState(true);
    const [showTooltip, setShowTooltip] = useState(false);

    useEffect(() => {
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
            {showHelpIcon && (
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
                                Tere tulemast UTchat veebilehele! <br />
                                Siin saad suhelda nii teiste üliõpilastega kui ka AI juturobotiga. <br />
                                Vestlusplatsil võid luua uusi vestlusruume, liituda olemasolevatega <br />
                                või pidada privaatselt vestlust oma sõbraga. Sõnumid säilivad <br />
                                vestlusruumides 24 tundi, pärast mida need kustutatakse. Palume jääda <br />
                                suhtlemisel viisakaks ning seetõttu asendatakse enim levinud vulgaarsused <br />
                                automaatselt tärniga (*). <b>Head suhtlemist!</b>
                            </motion.div>
                        )}
                    </AnimatePresence>
                </div>
            )}
            {showEmojiButton && <EmojiPicker onSelect={(emoji) => insertEmojiAtCaret(emoji.native)} />}
        </div>
    );
}