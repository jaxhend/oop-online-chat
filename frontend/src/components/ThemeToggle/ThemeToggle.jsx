import { Sun, Moon } from "lucide-react";
import useTheme from "@/hooks/useTheme";

export default function ThemeToggle() {
    const [theme, toggleTheme] = useTheme();

    return (
        <button
            onClick={toggleTheme}
            className="p-2 rounded-lg border"
            title="Toggle teema"
        >
            {theme === "dark" ? <Sun /> : <Moon />}
        </button>
    );
}