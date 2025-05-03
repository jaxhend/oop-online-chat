import { useRef, useEffect, useState } from "react";

export default function TerminalInput({ onSubmit }) {
    const inputRef = useRef(null);
    const [command, setCommand] = useState("");

    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    const handleKeyDown = (e) => {
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
        <div className="flex items-center mt-2 text-white">
            <span className="text-green-400 mr-2"></span>
            <div
                ref={inputRef}
                contentEditable
                suppressContentEditableWarning
                className="outline-none whitespace-pre break-all bg-black w-full"
                onKeyDown={handleKeyDown}
                onBlur={() => inputRef.current?.focus()}
            >
                {command}
            </div>
        </div>
    );
}