import { useRef, useEffect } from "react";

export default function TerminalInput({ onSubmit, isActive }) {
    const inputRef = useRef(null);

    useEffect(() => {
        if (isActive) {
            inputRef.current?.focus();
        }
    }, [isActive]);

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
        <div className="flex items-center mt-2 text-white font-mono bg-black p-2 rounded">
            <span className="text-green-400 mr-2">$</span>
            <div
                ref={inputRef}
                contentEditable={isActive}
                suppressContentEditableWarning
                onKeyDown={handleKeyDown}
                className={`outline-none whitespace-pre break-all flex-1 ${!isActive ? "opacity-50 pointer-events-none" : ""}`}
                onBlur={() => isActive && inputRef.current?.focus()}
            />
        </div>
    );
}