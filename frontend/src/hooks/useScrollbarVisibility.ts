import { useEffect, useRef } from "react";

export function useScrollbarVisibility(hideDelay = 800) {
    const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        const root = document.documentElement;

        const onScroll = () => {
            if (!root.classList.contains("is-scrolling")) {
                root.classList.add("is-scrolling");
            }

            if (timerRef.current) {
                clearTimeout(timerRef.current);
            }

            timerRef.current = setTimeout(() => {
                root.classList.remove("is-scrolling");
                timerRef.current = null;
            }, hideDelay);
        };

        window.addEventListener("scroll", onScroll, { passive: true });

        return () => {
            window.removeEventListener("scroll", onScroll);
            if (timerRef.current) {
                clearTimeout(timerRef.current);
            }
            root.classList.remove("is-scrolling");
        };
    }, [hideDelay]);
}
