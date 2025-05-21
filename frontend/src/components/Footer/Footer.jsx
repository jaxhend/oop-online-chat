import styles from "./Footer.module.css";
import React from "react";

export default function Footer() {
    return (
        <div className={styles["footer"]}>
            <div className={styles["footer-title"]}>
                <strong> Tehisaru info</strong>
            </div>
            <div className={styles["text-block"]}>
                <div className={styles["main-footer"]}>
                    <p>
                        Mudel: Qwen3-3B <br/>
                        Server: Tartu Ülikooli HPC <br/>
                        Teie sõnumeid ei salvestata! <br/> <br/>
                    </p>
                    <p>
                        Hetke teadmised:
                        <div className={styles["list-content"]}>
                            <ul className={styles.list}>
                                <li>TÜ õppekavad</li>
                                <li>TÜ õppeained</li>
                                <li>Lehekülje <a href="https://ut.ee" target="_blank" rel="noreferrer">ut.ee</a> sisu</li>
                                <li>Lehekülje <a href="https://cs.ut.ee" target="_blank" rel="noreferrer">cs.ut.ee</a> sisu</li>
                            </ul>
                        </div>
                    </p>
                </div>
                <p className={styles.line}>
                    NB! Palun arvestage, et tehisaru võib anda ebatäpseid vastuseid. <br/>
                    Vigade või ettepanekute korral kirjutage palun aadressile <a className={styles["mail-text"]} href="mailto:info@utchat.ee">info@utchat.ee</a>
                </p>
            </div>
        </div>
    );
}