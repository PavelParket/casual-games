import { motion } from "framer-motion";
import { Box, Button, Stack, Typography } from "../../../../ui";
import type { PlayerResponse } from "../../../../models/Room";

interface GameOverOverlayProps {
    winnerId: string | null;
    myGuid: string | undefined;
    players: Record<string, PlayerResponse> | undefined;
    onLeave: () => void;
}

const easeOut: [number, number, number, number] = [0.22, 1, 0.36, 1];

const containerVariants = {
    hidden: { opacity: 0, scale: 0.95 },
    visible: {
        opacity: 1,
        scale: 1,
        transition: {
            duration: 0.3,
            ease: easeOut,
            staggerChildren: 0.1,
            delayChildren: 0.15,
        },
    },
};

const itemVariants = {
    hidden: { opacity: 0, y: 16 },
    visible: {
        opacity: 1,
        y: 0,
        transition: { duration: 0.28, ease: easeOut },
    },
};

export function GameOverOverlay({ winnerId, myGuid, players, onLeave }: GameOverOverlayProps) {
    const isDraw = winnerId === null;
    const iWon = !isDraw && winnerId === myGuid;
    const winnerName = !isDraw && winnerId && players ? (players[winnerId]?.username ?? "Opponent") : null;

    return (
        <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
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

                {/* Result title */}
                <motion.div variants={itemVariants}>
                    <Typography variant="h2" style={{
                        color: isDraw
                            ? "var(--color-text)"
                            : iWon
                                ? "#2ecc71"
                                : "#e74c3c",
                    }}>
                        {isDraw ? "Draw!" : iWon ? "You Won!" : "You Lost"}
                    </Typography>
                </motion.div>

                {/* Winner name (when opponent won) */}
                {!isDraw && !iWon && winnerName && (
                    <motion.div variants={itemVariants}>
                        <Typography variant="body" style={{ opacity: 0.7 }}>
                            {winnerName} wins this round
                        </Typography>
                    </motion.div>
                )}

                {/* Result badge */}
                <motion.div variants={itemVariants}>
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
                </motion.div>

                {/* Button */}
                <motion.div variants={itemVariants}>
                    <Button onClick={onLeave}>
                        Back to Rooms
                    </Button>
                </motion.div>
            </Stack>
        </motion.div>
    );
}
