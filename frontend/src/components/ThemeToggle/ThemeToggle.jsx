import styles from './ThemeToggle.module.css';

export default function ThemeToggle({ theme, onToggle }) {
    return (
        <div className={styles.container}>
            <button onClick={onToggle} className={styles.button}>
                {theme === "dark" ? "Light Mode" : "Dark Mode"}
            </button>
        </div>
    );
}