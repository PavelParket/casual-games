import { Box, Typography } from "../../../../ui";
import { PlayingCard } from "./PlayingCard";

interface DiscardPileProps {
    discardCount: number;
}

export function DiscardPile({ discardCount }: DiscardPileProps) {
    if (discardCount === 0) {
        return null;
    }

    const layers = Math.min(discardCount, 3);

    return (
        <Box style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: "0.25rem",
        }}>
            <Typography variant="caption" style={{
                fontSize: "0.65rem",
                opacity: 0.6,
                textTransform: "uppercase",
                letterSpacing: "0.05em",
            }}>
                Бита
            </Typography>

            {/* Stacked pile */}
            <Box style={{ position: "relative", width: 44, height: 62 }}>
                {Array.from({ length: layers }).map((_, i) => (
                    <Box
                        key={i}
                        style={{
                            position: i === 0 ? "relative" : "absolute",
                            top: i === 0 ? undefined : -(i * 2),
                            left: i === 0 ? undefined : i * 2,
                            zIndex: layers - i,
                        }}
                    >
                        <PlayingCard faceDown size="sm" />
                    </Box>
                ))}
            </Box>

            {/* Count badge */}
            <Typography variant="caption" style={{
                background: "var(--color-bg-glass)",
                border: "1px solid var(--color-border)",
                borderRadius: "var(--radius-sm)",
                padding: "0.1rem 0.4rem",
                fontSize: "0.7rem",
                fontWeight: 600,
            }}>
                {discardCount}
            </Typography>
        </Box>
    );
}
