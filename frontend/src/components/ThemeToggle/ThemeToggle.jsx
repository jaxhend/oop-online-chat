import {Moon, Sun} from "lucide-react";
import useTheme from "@/hooks/useTheme";
import styles from "../ThemeToggle/ThemeToggle.module.css"
import {AnimatePresence, motion} from "framer-motion";

export default function ThemeToggle() {
    const [theme, toggleTheme] = useTheme();

    return (
        <motion.button
            onClick={toggleTheme}
            className={styles["button"]}
            title="Toggle teema"
            whileHover={{ scale: 1.1, rotate: 5, transition: { type: "spring", stiffness: 300, damping: 20 } }}
            whileTap={{ scale: 0.95, transition: { type: "spring", stiffness: 300, damping: 20 } }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
        >
            <AnimatePresence mode="wait" initial={false}>
                <motion.div
                    className={styles["flip-container"]}
                    key={theme}
                    initial={{rotateY: 90, opacity: 0}}
                    animate={{rotateY: 0, opacity: 1}}
                    exit={{rotateY: -90, opacity: 0}}
                    transition={{duration: 0.3}}

                >
                    {theme === "dark" ? <Sun/> : <Moon/>}
                </motion.div>
            </AnimatePresence>
        </motion.button>
    );
}