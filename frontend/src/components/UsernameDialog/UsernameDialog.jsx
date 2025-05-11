import React from "react";
import TerminalInput from "../TerminalInput/TerminalInput";
import styles from "./UsernameDialog.module.css";
import utLogo from '../../assets/ut-logo.webp';

export default function UsernameDialog({ onSubmit, error }) {
    return (
        <div className={styles.overlay}>
            <div className="bg-white text-black dark:bg-zinc-900 dark:text-white p-6 rounded-2xl shadow-xl w-full max-w-md">
                <h2 className={styles.utname}>UTCHAT</h2>
                <img
                    className={styles.utlogo}
                    src={utLogo}
                    alt="UT logo"
                    loading="eager"
                />
                <h2 className="text-xl font-semibold mb-4 text-center">Sisesta kasutajanimi</h2>

                <TerminalInput
                    isActive={true}
                    onSubmit={onSubmit}
                    showEmojiButton={false}
                    placeholder={"Sisesta kasutajanimi..."}
                />

                {error && <p className="text-red-500 text-sm mt-2 text-center">{error}</p>}
            </div>
        </div>
    );
}