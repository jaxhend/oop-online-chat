import React from "react";
import TerminalInput from "../TerminalInput/TerminalInput";
import styles from "./UsernameDialog.module.css";
import utLogo from '../../assets/ut-logo.webp';

export default function UsernameDialog({onSubmit, error}) {
    return (
        <div className={styles.overlay}>
            <div className={styles["center-wrapper"]}>
                <div className={styles["branding"]}>
                    <h1 className={styles["branding-title"]}>UTchat</h1>
                    <img
                        className={styles["branding-logo"]}
                        src={utLogo}
                        alt="UT logo"
                    />
                </div>
                <div className={`${styles["username-entry"]} ${styles.visible}`}>
                    <h2 className={styles["username-title"]}>Sisesta kasutajanimi</h2>
                    <TerminalInput
                        isActive={true}
                        onSubmit={onSubmit}
                        showEmojiButton={false}
                        showTooltip = {false}
                        placeholder={"..."}
                    />
                </div>

                {error && <p className={styles["error-text"]}>{error}</p>}
            </div>
        </div>
    );
}