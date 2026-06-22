import { forwardRef } from "react";
import { Box, Typography } from "../../../../ui";
import { PlayingCard } from "./PlayingCard";
interface DiscardPileProps {
    discardCount: number;
}

export const DiscardPile = forwardRef<HTMLDivElement, DiscardPileProps>(
    function DiscardPile({ discardCount }, ref) {
        const layers = Math.min(discardCount, 3);

        return (
            <div ref={ref}
                style={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    gap: "0.25rem",
                    minHeight: "80px",
                    justifyContent: "center",
                }}>
                {discardCount > 0 && (
                    <>
                        <Typography variant="caption" style={{
                            fontSize: "0.65rem",
                            opacity: 0.6,
                            letterSpacing: "0.05em",
                        }}>
                            Discards
                        </Typography>

                        {/* Stacked pile */}
                        <Box className="discard-pile-container">
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
                    </>
                )}
            </div>
        );
    }
);
