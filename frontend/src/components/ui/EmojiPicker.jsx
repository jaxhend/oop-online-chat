import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import Picker from "@emoji-mart/react"
import data from "@emoji-mart/data"
import {Component} from "react";

export default function EmojiPicker ({onSelect}) {
    return (
        <Popover>
            <PopoverTrigger asChild>
                <button
                type="button"
                className="text-xl ml-2 px-2 hover:scale-110 transition"
                title="Lisa emoji"
                >
                    😁
                </button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0 z-50" side="top" align="end">
                <Picker data={data} onEmojiSelect={onSelect} theme="dark" />
            </PopoverContent>
        </Popover>
    )
}