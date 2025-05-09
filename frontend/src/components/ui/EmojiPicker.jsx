import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import Picker from "@emoji-mart/react";
import data from "@emoji-mart/data";


export default function EmojiPicker({ onSelect }) {
    return (
        <Popover>
            <PopoverTrigger asChild>
                <button
                    type="button"
                    className="emoji-trigger ml-2 p-2 rounded hover:bg-muted/40 transition"
                    title="Lisa emoji"
                >
                    😀
                </button>
            </PopoverTrigger>
            <PopoverContent className="emoji-picker w-auto p-0 z-[1000]" side="top" align="end" sideOffset={8}>
                <Picker data={data} onEmojiSelect={onSelect} theme="dark" />
            </PopoverContent>
        </Popover>
    );
}