import React from "react";
import TerminalInput from "../TerminalInput/TerminalInput";
import styles from "../UsernameDialog/UsernameDialog.module.css";
import utLogo from '../../assets/ut-logo.png';

export default function UsernameDialog({ onSubmit, error }) {
    return (
        <div className={styles.overlay}>
            <div className={styles["center-wrapper"]}>
                <div className={styles["branding"]}>
                    <h1 className={styles["branding-title"]}>UTCHAT</h1>
                    <img
                        className={styles.utlogo}
                        src={utLogo}
                        alt="UT logo"
                    />
                </div>
                <div className={styles["username-entry"]}>
                    <h2 className={styles["username-title"]}>Sisesta kasutajanimi</h2>
                    <TerminalInput
                        isActive={true}
                        onSubmit={onSubmit}
                        showEmojiButton={false}
                        placeholder={"Sisesta kasutajanimi..."}
                    />
                </div>

                {error && <p className={styles["error-text"]}>{error}</p>}
            </div>
        </div>
    );
}