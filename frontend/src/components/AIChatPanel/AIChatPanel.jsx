import "./AIChatPanel.module.css";

export default function AIChatPanel({ botInput, onBotInputChange, onBotSend, chatHistory }) {
    return (
        <div className="flex flex-col fixed-flex-1-right border p-3 flex-1 overflow-y-auto">
            <h3 className="font-semibold mb-2">AI juturobot</h3>
            <div className="chat-log-fixed whitespace-pre-wrap mb-2">
                {chatHistory.map((entry, i) => (
                    <div key={i}><strong>{entry.sender}:</strong> {entry.text}</div>
                ))}
            </div>
            <div className="flex gap-2">
        <textarea
            rows={1}
            value={botInput}
            onChange={onBotInputChange}
            onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    onBotSend();
                }
            }}
            className="border p-2 resize-none flex-1 h-10"
            placeholder="Sisesta küsimus..."
        />
                <button onClick={onBotSend} className="border px-3 py-1 h-10">Saada botile</button>
            </div>
        </div>
    );
}