import { motion } from "framer-motion";
import { Box, Button, Stack, Typography } from "../../../../ui";

interface GameOverOverlayProps {
    winnerId: string | null;
    myGuid: string | undefined;
    players: Record<string, string> | undefined;
    onLeave: () => void;
}

export function GameOverOverlay({ winnerId, myGuid, players, onLeave }: GameOverOverlayProps) {
    const isDraw = winnerId === null;
    const iWon = !isDraw && winnerId === myGuid;
    const winnerName = !isDraw && winnerId && players ? (players[winnerId] ?? "Opponent") : null;

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.3, ease: "easeOut" }}
            style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                padding: "3rem 2rem",
                minHeight: "300px",
            }}
        >
            <Stack gap="1.5rem" align="center">
                {/* Result icon */}
                <Typography variant="h1" style={{ fontSize: "3rem" }}>
                    {isDraw ? "🤝" : iWon ? "🏆" : "😔"}
                </Typography>

                {/* Result title */}
                <Typography variant="h2" style={{
                    color: isDraw
                        ? "var(--color-text)"
                        : iWon
                            ? "#2ecc71"
                            : "#e74c3c",
                }}>
                    {isDraw ? "Draw!" : iWon ? "You Won!" : "You Lost"}
                </Typography>

                {/* Winner name (when opponent won) */}
                {!isDraw && !iWon && winnerName && (
                    <Typography variant="body" style={{ opacity: 0.7 }}>
                        {winnerName} wins this round
                    </Typography>
                )}

                {/* Result badge */}
                <Box style={{
                    padding: "0.5rem 1.5rem",
                    borderRadius: "var(--radius-md)",
                    border: `1px solid ${isDraw ? "var(--color-border)" : iWon ? "rgba(46,204,113,0.4)" : "rgba(231,76,60,0.4)"}`,
                    background: isDraw
                        ? "var(--color-bg-glass)"
                        : iWon
                            ? "rgba(46,204,113,0.08)"
                            : "rgba(231,76,60,0.08)",
                }}>
                    <Typography variant="body" style={{
                        fontWeight: 600,
                        color: isDraw ? "var(--color-text)" : iWon ? "#2ecc71" : "#e74c3c",
                    }}>
                        {isDraw ? "No winner — stakes returned" : iWon ? "You take the pot!" : "Better luck next time"}
                    </Typography>
                </Box>

                <Button onClick={onLeave}>
                    Back to Rooms
                </Button>
            </Stack>
        </motion.div>
    );
}
