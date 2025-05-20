import styles from "./Footer.module.css";

export default function Footer() {
    return (
        <div className={styles["footer"]}>
            <div className={styles["footer-title"]}>
                <strong> AI-mudeli info</strong>
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
                    NB! Mudel võib TÜ-välise info ja küsimuste korral hallutsineerida.
                </p>
            </div>
        </div>
    );
}