import { Box, Stack, Typography } from "../../../../ui";

interface DeadlockOverlayProps {
    secondsLeft: number;
}

export function DeadlockOverlay({ secondsLeft }: DeadlockOverlayProps) {
    return (
        <Box style={{
            position: "absolute",
            inset: 0,
            background: "rgba(0,0,0,0.6)",
            backdropFilter: "blur(4px)",
            zIndex: 1000,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            borderRadius: "inherit"
        }}>
            <Stack gap="1rem" align="center" style={{ 
                background: "var(--color-bg)", 
                padding: "2rem", 
                borderRadius: "var(--radius-lg)",
                boxShadow: "var(--shadow-lg)"
            }}>
                <Typography variant="h2" style={{ color: "var(--color-expense-text)" }}>
                    No Moves Left
                </Typography>
                <Typography variant="body" style={{ opacity: 0.8, textAlign: "center" }}>
                    Waiting for opponent to finish or reach a deadlock.
                </Typography>
                <Typography variant="h3" style={{ fontWeight: "bold" }}>
                    {secondsLeft}s
                </Typography>
            </Stack>
        </Box>
    );
}
