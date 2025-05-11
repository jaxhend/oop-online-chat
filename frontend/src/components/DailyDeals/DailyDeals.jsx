import React from "react";
import styles from './DailyDeals.module.css';

export default function DailyDeals({ deals }) {
    return (
        <div className={styles.section}>
            <h4 className={styles.title}>
                {deals.length > 0 ? `${deals[0].restaurant} Päevapakkumised` : "Päevapakkumised"}
            </h4>
            <ul>
                {deals.length > 0 ? deals.map((deal, i) => (
                    <li key={i} className={styles.item}>
                        {deal.offer}
                    </li>
                )) : <li className={styles.item}>Hetkel päevapakkumised puuduvad.</li>}
            </ul>
        </div>
    );
}