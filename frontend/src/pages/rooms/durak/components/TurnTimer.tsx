import { useEffect, useRef, useState } from "react";
import { Box, Typography } from "../../../../ui";

interface TurnTimerProps {
    remainingSeconds: number | null;
    isMyTurn: boolean;
}

function formatTime(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, "0")}`;
}

export function TurnTimer({ remainingSeconds, isMyTurn }: TurnTimerProps) {
    const [display, setDisplay] = useState<number | null>(remainingSeconds);
    const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

    useEffect(() => {
        // Always clear previous interval first
        if (intervalRef.current) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
        }

        if (remainingSeconds === null) {
            setDisplay(null);
            return;
        }

        // Reset display to the new server value immediately
        setDisplay(remainingSeconds);

        if (remainingSeconds <= 0) return;

        intervalRef.current = setInterval(() => {
            setDisplay(prev => {
                if (prev === null || prev <= 1) {
                    clearInterval(intervalRef.current!);
                    intervalRef.current = null;
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
                intervalRef.current = null;
            }
        };
    }, [remainingSeconds]);

    if (display === null) return null;

    const isLow = display <= 10;

    return (
        <Box style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: "0.25rem",
            padding: "0.5rem 0.75rem",
            borderRadius: "var(--radius-md)",
            border: `1px solid ${isLow ? "rgba(231,76,60,0.5)" : "var(--color-border)"}`,
            background: isLow ? "rgba(231,76,60,0.08)" : "var(--color-bg-glass)",
            minWidth: "80px",
            transition: "border-color 0.3s, background 0.3s",
        }}>
            <Typography variant="caption" style={{
                fontSize: "0.65rem",
                opacity: 0.6,
                textTransform: "uppercase",
                letterSpacing: "0.05em",
            }}>
                Turn
            </Typography>
            <Typography variant="caption" style={{
                fontSize: "0.7rem",
                fontWeight: 600,
                color: isMyTurn ? "#2ecc71" : "#e74c3c",
            }}>
                {isMyTurn ? "Your turn" : "Opponent's turn"}
            </Typography>
            <Typography variant="h3" style={{
                fontVariantNumeric: "tabular-nums",
                color: isLow ? "#e74c3c" : "var(--color-text)",
                fontWeight: 700,
                transition: "color 0.3s",
            }}>
                {formatTime(display)}
            </Typography>
        </Box>
    );
}
