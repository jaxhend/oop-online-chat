import React, { useEffect, useState } from "react";
import styles from "./WeatherInfo.module.css";

export default function WeatherInfo() {
    const [weather, setWeather] = useState({});

    useEffect(() => {
        fetchFlaskContent("ilm", setWeather);
    }, []);

    return (
        <div className="section">
            <h4 className="title">Ilm</h4>
            {weather.temperature ? (
                <>
                    <p className="paragraph">Temperatuur: {weather.temperature}°C</p>
                    {weather.feelsLike && <p className="paragraph">Tundub nagu: {weather.feelsLike}°C</p>}
                    {weather.precipitation && <p className="paragraph">Sademed: {weather.precipitation}mm</p>}
                    {weather.iconUrl && <img src={weather.iconUrl} alt="Ilma ikoon" className="icon" />}
                </>
            ) : (
                <p className="paragraph">Ilma andmeid ei ole saadaval</p>
            )}
        </div>
    );
}