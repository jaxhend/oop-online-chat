import React from "react";
import styles from '../DailyDeals/DailyDeals.module.css';

export default function DailyDeals({deals}) {
    const extractParts = (text) => {
        const match = text.match(/^(.*?)(?:\s[-–]\s)?(\d+,\d{2}(?:\/\d+,\d{2})?€)$/);
        if (match) {
            return { description: match[1].trim(), price: match[2].trim() };
        }
        return { description: text, price: null };
    };

    return (
        <div className={styles.section}>
            <h4 className={styles.title}>
                {deals.length > 0 ? `${deals[0].restaurant} Päevapakkumised` : "Päevapakkumised"}
            </h4>
            <ul>
                {deals.length > 0 ? deals.map((deal, i) => {
                    const { description, price } = extractParts(deal.offer);
                    return (
                        <li key={i} className={styles.item}>
                            {description} {price && <span className={styles.price}>– {price}</span>}
                        </li>
                    );
                }) : <li className={styles.item}>Hetkel päevapakkumised puuduvad.</li>}
            </ul>
        </div>
    );
}