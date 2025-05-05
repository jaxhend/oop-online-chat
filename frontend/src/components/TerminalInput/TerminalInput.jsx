import { useRef, useEffect } from "react";
import EmojiPicker from "@/components/ui/EmojiPicker";



export default function TerminalInput({ onSubmit, isActive, showEmojiButton=false }) {
    const inputRef = useRef(null);

    useEffect(() => {
        if (isActive) {
            inputRef.current?.focus();
        }
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
    }

    const handleEmojiSelect = (emoji) => {
        insertEmojiAtCaret(emoji.native);
    }
    const handleKeyDown = (e) => {
        if (!isActive) return;

        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            const text = inputRef.current.innerText.trim();
            if (text) {
                onSubmit(text);
                inputRef.current.innerText = "";
            }
        }
    };

    return (
        <div className="relative flex items-center mt-2 text-white font-mono bg-black p-2 rounded">
            <div
                ref={inputRef}
                contentEditable={isActive}
                suppressContentEditableWarning
                onKeyDown={handleKeyDown}
                className={`outline-none whitespace-pre break-all flex-1 ${!isActive ? "opacity-50 pointer-events-none" : ""}`}
                onBlur={() => isActive && inputRef.current?.focus()}
            />
            {showEmojiButton && (
                <EmojiPicker onSelect={handleEmojiSelect} />
            )}
        </div>
    );
}