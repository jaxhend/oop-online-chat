import React, {useState} from "react";
import TerminalInput from "../TerminalInput/TerminalInput";
import styles from "./UsernameDialog.module.css";
import utLogo from '../../assets/ut-logo.webp';
import {motion} from "framer-motion";

export default function UsernameDialog({onSubmit, error}) {
    const[clicked, setClicked] = useState(false);
    return (
        <div className={styles.overlay}>
            <div className={styles["center-wrapper"]}>
                <div className={styles["branding"]}>
                    <h1 className={styles["branding-title"]}>UTchat</h1>
                    <p className={styles["username-description"]}>Vestluskeskkond Tartu Ülikooli tudengitele</p>
                    <img
                        className={styles["branding-logo"]}
                        src={utLogo}
                        alt="UT logo"
                    />
                </div>
                <div className={`${styles["username-entry"]} ${styles.visible}`}>
                    <h2 className={styles["username-title"]}>Sisesta kasutajanimi</h2>
                    <p className={styles["username-description"]}>Vali endale sobiv kasutajanimi, <br/> selle järgi tunnevad teised Sind ära.</p>
                   <motion.div
                       initial={{ opacity: 0, y: 300 }}
                       animate={{ opacity: 1, y: 0 }}
                       transition={{ duration: 1.5 }}


                   >
                       <motion.div
                           onClick={() => setClicked(!clicked)}
                           animate={{scale: clicked ? 1.1 : 1}}
                           transition={{type: "spring", stiffness: 300, damping: 20}}
                       >
                           <TerminalInput
                               isActive={true}
                               onSubmit={onSubmit}
                               showEmojiButton={false}
                               showTooltip = {false}
                               placeholder={"Kirjuta kasutajanimi siia ..."}
                           />
                       </motion.div>
                   </motion.div>
                </div>

                {error && <p className={styles["error-text"]}>{error}</p>}
            </div>
        </div>
    );
}