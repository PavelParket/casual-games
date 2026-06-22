import { motion } from "framer-motion";
import { Box, Button, Stack, Typography } from "../../../../ui";

interface EndGameOverlayProps {
    isWin: boolean;
    winnerName?: string;
    jackpot: number;
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

export function EndGameOverlay({ isWin, winnerName, jackpot, onLeave }: EndGameOverlayProps) {
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
                height: "100%",
                minHeight: "300px",
            }}
        >
            <Stack gap="1.5rem" align="center">

                <motion.div variants={itemVariants}>
                    <Typography variant="h2" style={{
                        color: isWin ? "#2ecc71" : "var(--color-text)",
                    }}>
                        {isWin ? "You Cracked the Code!" : `${winnerName || "Someone"} won!`}
                    </Typography>
                </motion.div>

                {!isWin && (
                    <motion.div variants={itemVariants}>
                        <Typography variant="body" style={{ opacity: 0.7 }}>
                            Better luck next time. The code has been deciphered.
                        </Typography>
                    </motion.div>
                )}

                <motion.div variants={itemVariants}>
                    <Box style={{
                        padding: "0.5rem 1.5rem",
                        borderRadius: "var(--radius-md)",
                        border: `1px solid ${isWin ? "rgba(46,204,113,0.4)" : "var(--color-border)"}`,
                        background: isWin ? "rgba(46,204,113,0.08)" : "var(--color-bg-glass)",
                    }}>
                        <Typography variant="body" style={{
                            fontWeight: 600,
                            color: isWin ? "#2ecc71" : "var(--color-text)",
                        }}>
                            {isWin ? `Jackpot won: ${jackpot} CG Coins` : `The Jackpot was: ${jackpot} CG Coins`}
                        </Typography>
                    </Box>
                </motion.div>

                <motion.div variants={itemVariants}>
                    <Button onClick={onLeave}>
                        Back to Rooms
                    </Button>
                </motion.div>
            </Stack>
        </motion.div>
    );
}
