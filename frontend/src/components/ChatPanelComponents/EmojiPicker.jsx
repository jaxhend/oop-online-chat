import {useEffect, useRef, useState} from "react";
import Picker from "@emoji-mart/react";
import data from "@emoji-mart/data";
import {AnimatePresence, motion} from "framer-motion";
import styles from ".//EmojiPicker.module.css";

export default function EmojiPicker({onSelect}) {
    const [open, setOpen] = useState(false);
    const wrapperRef = useRef();

    useEffect(() => {
        const handleClickOutside = (e) => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
                setOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <div className={styles["emoji-wrapper"]} ref={wrapperRef}>
            <motion.button
                className={styles["emoji-trigger"]}
                title="Lisa emoji"
                onClick={() => setOpen((prev) => !prev)}
                whileHover={{scale: 1.05}}
                whileTap={{scale: 0.95}}
            >
                😀
            </motion.button>

            <AnimatePresence>
                {open && (
                    <motion.div
                        className={styles["emoji-picker"]}
                        initial={{opacity: 0, scale: 0.3, y: 20, originX: 1, originY: 1}}
                        animate={{opacity: 1, scale: 1, y: 0}}
                        exit={{opacity: 0, scale: 0.3, y: 8}}
                        transition={{duration: 0.25, ease: "easeOut"}}
                    >
                        <Picker data={data} onEmojiSelect={(e) => {
                            onSelect(e);
                            setOpen(false);
                        }} theme="dark"/>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
}