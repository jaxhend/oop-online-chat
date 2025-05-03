import React from "react";
import styles from "./WeatherInfo.module.css";

export default function WeatherInfo({ weather }) {
    return (
        <div className={styles.weatherCard}>
            <h4 className={styles.title}>Ilm</h4>
            {weather.temperature ? (
                <p>Temperatuur: {weather.temperature}</p>
            ) : (
                <p>Ilma andmed puuduvad</p>
            )}
        </div>
    );
}