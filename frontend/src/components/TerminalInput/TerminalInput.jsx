import { useRef, useEffect, useState } from "react";

export default function TerminalInput({ onSubmit }) {
    const inputRef = useRef(null);
    const [command, setCommand] = useState("");

    useEffect(() => {
        inputRef.current?.focus();
    }, []);

    const handleKeyDown = (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            const trimmed = command.trim();
            if (trimmed) onSubmit(trimmed);
            setCommand("");
        } else if (e.key === "Backspace") {
            setCommand(command.slice(0, -1));
        } else if (e.key.length === 1) {
            setCommand(command + e.key);
        }
    };

    return (
        <div className="flex items-center mt-2 text-white">
            <span className="text-green-400 mr-2">$</span>
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