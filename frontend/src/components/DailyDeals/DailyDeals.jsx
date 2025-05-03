import React from "react";
import styles from './DailyDeals.module.css';

export default function DailyDeals({ deals }) {
    return (
        <div className={styles.dailyDeals}>
            <h4 className={styles.title}>Päevapakkumised</h4>
            <ul>
                {deals.length > 0 ? deals.map((deal, i) => (
                    <li key={i} className={styles.item}>
                        <strong>{deal.restaurant}</strong>: {deal.offer}
                    </li>
                )) : <li className={styles.item}>Ei ole saadaval lõunapakkumisi</li>}
            </ul>
        </div>
    );
}