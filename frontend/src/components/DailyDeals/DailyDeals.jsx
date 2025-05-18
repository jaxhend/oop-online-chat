import React from "react";
import styles from '../DailyDeals/DailyDeals.module.css';
import {motion} from "framer-motion";

export default function DailyDeals({deals}) {
    const extractParts = (text) => {
        const match = text.match(/^(.*?)(?:\s[-–]\s)?(\d+,\d{2}(?:\/\d+,\d{2})?€)$/);
        if (match) {
            return {description: match[1].trim(), price: match[2].trim()};
        }
        return {description: text, price: null};
    };

    return (
        <motion.div
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
        >
            <div className={styles.section}>
                <h4 className={styles.title}>
                    {deals.length > 0 ? `${deals[0].restaurant} päevapakkumised` : "Päevapakkumised"}
                </h4>
                <ul className={styles.list}>
                    {deals.length > 0 ? deals.map((deal, i) => {
                        const {description, price} = extractParts(deal.offer);
                        return (
                            <li key={i} className={styles.item}>
                                <span className={styles.description}>{description}</span>
                                {price && <span className={styles.price}>{price}</span>}
                            </li>
                        );
                    }) : <li className={styles.item}>Hetkel päevapakkumised puuduvad.</li>}
                </ul>
            </div>
        </motion.div>
    );
}