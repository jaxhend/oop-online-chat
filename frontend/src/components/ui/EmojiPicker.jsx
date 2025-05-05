import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import Picker from "@emoji-mart/react";
import data from "@emoji-mart/data";
import { Smile } from "lucide-react";

export default function EmojiPicker ({onSelect}) {
    return (
        <Popover>
            <PopoverTrigger asChild>
                <button
                type="button"
                className="ml-2 p-2 rounded hover:bg-muted/40 transition"
                title="Lisa emoji"
                >
                    <Smile className="2-5 h-5 text-white" />
                </button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0 z-50" side="top" align="end" sideOffset={8}>
                <Picker data={data} onEmojiSelect={onSelect} theme="dark" />
            </PopoverContent>
        </Popover>
    )
}