import React from "react";
import styles from "./WeatherInfo.module.css";
import { motion } from "framer-motion";

export default function WeatherInfo({weather}) {
    if (!weather || !weather.temperature) {
        return <p>Ilma andmeid ei ole saadaval</p>;
    }

    return (
        <motion.div
            className={styles.card}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
        >
            <div className={styles.header}>
                <h4 className={styles.city}>Tartu</h4>
                <span className={styles.time}>{new Date().toLocaleTimeString("et-EE", { hour: '2-digit', minute: '2-digit' })}</span>
            </div>

            <div className={styles.tempBlock}>
                <h3 className={styles.temperature}>{weather.temperature}</h3>
                <p className={styles.condition}>Hetkeilm</p>
            </div>

            <div className={styles["bottom-info"]}>
                <div className={styles["info-group"]}>
                    <div className={styles["info-item"]}>
                        🌡 <strong>Tundub nagu:</strong>  <span>{weather.feelsLike || "?"}</span>
                    </div>
                    <div className={styles["info-item"]}>
                        💧<strong>Sademed:</strong> <span>{weather.precipitation || "?"}</span>
                    </div>
                </div>
                <motion.img
                    src={weather.iconUrl}
                    alt="Ilma ikoon"
                    className={styles.icon}

                />
            </div>

        </motion.div>
    );
}