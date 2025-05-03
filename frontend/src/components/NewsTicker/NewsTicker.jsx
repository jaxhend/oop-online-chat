import React, { useLayoutEffect, useMemo, useRef } from "react";
import styles from './NewsTicker.module.css';

const NewsTicker = React.memo(({ newsList, animate }) => {
    const tickerRef = useRef(null);
    const repeatedNews = useMemo(() => [...newsList, ...newsList], [newsList]);

    useLayoutEffect(() => {
        if (!tickerRef.current || newsList.length === 0) return;
        const contentWidth = tickerRef.current.scrollWidth;
        const containerWidth = tickerRef.current.parentElement.offsetWidth;
        const speed = 100;
        const duration = (contentWidth + containerWidth) / speed;
        tickerRef.current.style.animationDuration = `${duration}s`;
    }, [newsList]);

    return (
        <div className={styles.newsTicker}>
            <div
                ref={tickerRef}
                className={`${styles.newsWrapper} ${animate ? styles.animateMarquee : ""}`}
            >
                {repeatedNews.map((item, idx) => {
                    const content = item.sourceName && item.link ? (
                        <>
                            <span className={styles.newsSource}>{item.sourceName}</span> - {item.title}
                        </>
                    ) : (
                        item.title
                    );

                    return item.link ? (
                        <a
                            key={idx}
                            className={styles.newsItem}
                            href={item.link}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            {content}
                        </a>
                    ) : (
                        <span key={idx} className={styles.newsItem}>
              {content}
            </span>
                    );
                })}
            </div>
        </div>
    );
});

export default NewsTicker;