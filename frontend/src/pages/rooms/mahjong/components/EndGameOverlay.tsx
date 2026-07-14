import { Box, Button, Stack, Typography } from "../../../../ui";
import type { PlayerResponse } from "../../../../models/Room";

interface EndGameOverlayProps {
    winnerId: string | null | undefined;
    myGuid: string | undefined;
    players: Record<string, PlayerResponse> | undefined;
    onLeave: () => void;
}

export function EndGameOverlay({ winnerId, myGuid, players, onLeave }: EndGameOverlayProps) {
    const isDraw = winnerId === null;
    const iWon = !isDraw && winnerId === myGuid;
    const winnerName = !isDraw && winnerId && players ? (players[winnerId]?.username ?? "Opponent") : null;

    return (
        <Box style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            padding: "3rem 2rem",
            minHeight: "300px",
        }}>
            <Stack gap="1.5rem" align="center">
                <Typography variant="h2" style={{ color: isDraw ? "var(--color-text)" : iWon ? "#2ecc71" : "#e74c3c" }}>
                    {isDraw ? "Draw!" : iWon ? "You Won!" : "You Lost"}
                </Typography>

                {!isDraw && !iWon && winnerName && (
                    <Typography variant="body" style={{ opacity: 0.7 }}>
                        {winnerName} wins this round
                    </Typography>
                )}

                <Button onClick={onLeave}>
                    Back to Rooms
                </Button>
            </Stack>
        </Box>
    );
}
