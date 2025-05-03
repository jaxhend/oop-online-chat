import React from "react";

export function ProgressBar({ progress }: { progress: number }) {
    return (
        <div className="w-full h-2 bg-gray-200 rounded-full">
            <div
                className="h-full bg-blue-500 rounded-full transition-all"
                style={{ width: `${progress}%` }}
            ></div>
        </div>
    );
}