import React from "react";
import styles from "./UsernameDialog.module.css";

export default function UsernameDialog({
                                           username,
                                           onChange,
                                           onSubmit,
                                           error
                                       }) {
    return (
        <div className={styles.overlay}>
            <div className={styles.dialog}>
                <h2 className={styles.title}>Sisesta kasutajanimi</h2>
                <input
                    className={styles.input}
                    value={username}
                    onChange={onChange}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") onSubmit();
                    }}
                    placeholder="Sisesta kasutajanimi"
                    autoFocus
                />
                {error && <p className={styles.error}>{error}</p>}
            </div>
        </div>
    );
}