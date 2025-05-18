import {useEffect, useState} from "react";

export default function useInitialData(apiUrl) {
    const [newsList, setNewsList] = useState([]);
    const [dailyDeals, setDailyDeals] = useState([]);
    const [weatherInfo, setWeatherInfo] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async (endpoint, setter) => {
            try {
                const res = await fetch(`${apiUrl}${endpoint}`);
                const data = await res.json();
                setter(data);
            } catch {
                setter([]);
            }
        };

        Promise.all([
            fetchData("/uudised", setNewsList),
            fetchData("/paevapakkumised", setDailyDeals),
            fetchData("/ilm", setWeatherInfo),
        ]).finally(() => setLoading(false));
    }, [apiUrl]);

    return {newsList, dailyDeals, weatherInfo, loading};
}