import React, { useEffect, useState } from "react";
import styles from "./WeatherInfo.module.css";

export default function WeatherInfo({ weather }) {
    if (!weather || !weather.temperature) {
        return <p>Ilma andmeid ei ole saadaval</p>;
    }

    return (
        <div className={styles.section}>
            <h4 className={styles.title}>Ilm</h4>
            <p className={styles.paragraph}>Temperatuur: {weather.temperature}</p>
            {weather.feelsLike && <p className={styles.paragraph}>Tundub nagu: {weather.feelsLike}</p>}
            {weather.precipitation && <p className={styles.paragraph}>Sademed: {weather.precipitation}</p>}
            {weather.iconUrl && <img src={weather.iconUrl} alt="Ilma ikoon" className={styles.icon} />}
        </div>
    );
}
