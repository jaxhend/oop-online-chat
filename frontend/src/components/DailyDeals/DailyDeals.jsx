import React, {useState} from "react";
import styles from '../DailyDeals/DailyDeals.module.css';
import {AnimatePresence, motion} from "framer-motion";

export default function DailyDeals({deals}) {
    const [selectedCafe, setSelectedCafe] = useState(Object.keys(deals)[0]);
    const extractParts = (text) => {
        const match = text.match(/^(.*?)(?:\s[-–]\s)?(\d+,\d{2}(?:\/\d+,\d{2})?€)$/);
        if (match) {
            return {description: match[1].trim(), price: match[2].trim()};
        }
        return {description: text, price: null};
    };

    const currentDeals = deals[selectedCafe] || [];

    return (
        <motion.div
            className={styles[""]}
            initial={{opacity: 0, y: 20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.6}}
        >
            <div className={styles.section}>
                <div className={styles["buttons"]}>
                    {Object.keys(deals).map((cafe) => (
                        <motion.button
                            whileHover={{scale: 1.05}}
                            whileTap={{scale: 0.95}}
                            key={cafe}
                            className={`${styles["button"]} ${selectedCafe === cafe ? styles["active"] : ""}`}
                            onClick={() => setSelectedCafe(cafe)}
                            animate={{
                                scale: selectedCafe === cafe ? 1.05 : 1,
                                boxShadow: selectedCafe === cafe ? "0 0 0 1px var(--border-color)" : "none"
                            }}
                        >
                            {cafe}
                        </motion.button>
                    ))}
                </div>
                <h4 className={styles.title}>
                    {currentDeals.length > 0 ? `${selectedCafe} päevapakkumised` : "Päevapakkumised"}
                </h4>
                <AnimatePresence mode="wait">
                    <motion.ul
                        className={styles.list}
                        key={selectedCafe}
                        initial={{opacity: 0, y: 10}}
                        animate={{opacity: 1, y: 0}}
                        exit={{opacity: 0, y:-10}}
                        transition={{duration: 0.5}}

                    >
                        {currentDeals.length > 0 ? currentDeals.map((deal, i) => {
                            const {description, price} = extractParts(deal.offer);
                            return (
                                <li key={i} className={styles.item}>
                                    <span className={styles.description}>{description}</span>
                                    {price && <span className={styles.price}>{price}</span>}
                                </li>
                            );
                        }) : <li className={styles.item}>Hetkel päevapakkumised puuduvad.</li>}
                    </motion.ul>
                </AnimatePresence>
            </div>
        </motion.div>
    );
}