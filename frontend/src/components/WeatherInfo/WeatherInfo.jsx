import React, { useEffect, useState } from "react";
import styles from "./WeatherInfo.module.css";

const fetchFlaskContent = async (endpoint, setter) => {
    try {
        const response = await fetch(`https://api.utchat.ee/${endpoint}`);
        const data = await response.json();
        setter(data);
    } catch (error) {
        console.error("Error fetching data:", error);
    }
};

export default function WeatherInfo() {
    const [weather, setWeather] = useState({});

    useEffect(() => {
        fetchFlaskContent("ilm", setWeather);
    }, []);

    return (
        <div className={styles.section}>
            <h4 className={styles.title}>Ilm</h4>
            {weather.temperature ? (
                <>
                    <p className={styles.paragraph}>Temperatuur: {weather.temperature}</p>
                    {weather.feelsLike && <p className={styles.paragraph}>Tundub nagu: {weather.feelsLike}</p>}
                    {weather.precipitation && <p className={styles.paragraph}>Sademed: {weather.precipitation}</p>}
                    {weather.iconUrl && <img src={weather.iconUrl} alt="Ilma ikoon" className={styles.icon} />}
                </>
            ) : (
                <p className={styles.paragraph}>Ilma andmeid ei ole saadaval</p>
            )}
        </div>
    );
}
