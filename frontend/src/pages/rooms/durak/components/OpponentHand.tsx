import { AnimatePresence, motion } from "framer-motion";
import { Box, Typography } from "../../../../ui";
import { PlayingCard } from "./PlayingCard";

interface OpponentHandProps {
    cardCount: number;
    opponentName: string;
}

export function OpponentHand({ cardCount, opponentName }: OpponentHandProps) {
    const displayCount = Math.max(0, cardCount);

    return (
        <Box style={{
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "center",
            gap: "0.75rem",
            padding: "0.5rem 0",
        }}>
            {/* Name + badge */}
            <Box style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "flex-start",
                gap: "0.25rem",
                minWidth: "80px",
                flexShrink: 0,
            }}>
                <Typography variant="caption" style={{
                    fontWeight: 600,
                    fontSize: "0.8rem",
                    whiteSpace: "nowrap",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    maxWidth: "80px",
                }}>
                    {opponentName}
                </Typography>
                <Typography variant="caption" style={{
                    background: "var(--color-bg-glass)",
                    border: "1px solid var(--color-border)",
                    borderRadius: "var(--radius-sm)",
                    padding: "0.1rem 0.4rem",
                    fontSize: "0.7rem",
                    fontWeight: 600,
                }}>
                    {displayCount} cards
                </Typography>
            </Box>

            {/* Face-down card row */}
            <Box style={{
                display: "flex",
                flexDirection: "row",
                alignItems: "center",
            }}>
                <AnimatePresence mode="popLayout">
                    {Array.from({ length: displayCount }).map((_, i) => (
                        <motion.div
                            key={i}
                            initial={{ x: 120, y: -50, opacity: 0, scale: 0.7 }}
                            animate={{ x: 0, y: 0, opacity: 1, scale: 1 }}
                            exit={{ opacity: 0, transition: { duration: 0.1 } }}
                            transition={{ duration: 0.25, ease: "easeOut" }}
                            style={{ marginLeft: i === 0 ? 0 : -28, zIndex: i }}
                        >
                            <PlayingCard faceDown size="sm" />
                        </motion.div>
                    ))}
                </AnimatePresence>

                {displayCount === 0 && (
                    <Typography variant="caption" style={{
                        color: "var(--color-text)",
                        opacity: 0.4,
                        fontSize: "0.75rem",
                    }}>
                        No cards
                    </Typography>
                )}
            </Box>
        </Box>
    );
}
