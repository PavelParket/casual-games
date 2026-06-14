import type { ReactNode } from "react";
import { Box, Typography } from "../../../ui";
import "../style/historyitem.css";

interface HistoryItemProps {
    variant: 'income' | 'expense' | 'neutral';
    iconText: string;
    title: string;
    date: string;
    time?: string;
    rightText: string;
    rightSubText?: ReactNode;
}

export function HistoryItem({ variant, iconText, title, date, time, rightText, rightSubText }: HistoryItemProps) {
    const colorType = variant;

    return (
        <Box
            className={`history-item ${rightSubText ? 'has-subtext' : ''}`}
            style={{
                background: `var(--color-${colorType}-bg)`,
                borderColor: `var(--color-${colorType}-border)`
            }}
        >
            <Box
                className="history-item-icon"
                style={{
                    background: `var(--color-${colorType}-icon-bg)`,
                    color: `var(--color-${colorType}-text)`
                }}
            >
                {iconText}
            </Box>

            <Box className="history-item-center">
                <Typography variant="body" className="history-item-title">
                    {title.trim()}
                </Typography>

                <Box className="history-item-date-time">
                    <span className="history-item-date">{date}</span>
                    {time && (
                        <>
                            <span className="history-item-separator">&nbsp;•&nbsp;</span>
                            <span className="history-item-time">{time}</span>
                        </>
                    )}
                </Box>
            </Box>

            <Typography variant="body" className="history-item-amount" style={{ color: `var(--color-${colorType}-text)` }}>
                <span className="mobile-sign">{iconText}</span>
                {rightText}
            </Typography>

            {rightSubText && (
                <Box className="history-item-subtext-stack">
                    <Typography variant="caption" className="history-item-subtext">
                        {rightSubText}
                    </Typography>
                </Box>
            )}
        </Box>
    );
}
