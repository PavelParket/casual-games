import { motion } from "framer-motion";
import { Box, Button, Stack, Typography } from "../../../../ui";

interface EndGameOverlayProps {
    won: boolean;
    betAmount: number;
    winAmount: number;
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

export function EndGameOverlay({ won, betAmount, winAmount, onLeave }: EndGameOverlayProps) {
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

                <motion.div variants={itemVariants}>
                    <Typography variant="h2" style={{ color: won ? "#2ecc71" : "#e74c3c" }}>
                        {won ? "You Won!" : "You Lost"}
                    </Typography>
                </motion.div>

                <motion.div variants={itemVariants}>
                    <Box style={{
                        padding: "0.5rem 1.5rem",
                        borderRadius: "var(--radius-md)",
                        border: `1px solid ${won ? "rgba(46,204,113,0.4)" : "rgba(231,76,60,0.4)"}`,
                        background: won ? "rgba(46,204,113,0.08)" : "rgba(231,76,60,0.08)",
                        textAlign: "center"
                    }}>
                        <Typography variant="body" style={{ fontWeight: 600, color: won ? "#2ecc71" : "#e74c3c" }}>
                            {won ? `+ ${winAmount.toFixed(2)} CG Coins` : `- ${betAmount.toFixed(2)} CG Coins`}
                        </Typography>
                        <Typography variant="caption" style={{ opacity: 0.7, color: won ? "#2ecc71" : "#e74c3c" }}>
                            {won ? "Your bet multiplied by odds" : "Better luck next time"}
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
