import { Box, Button, Modal, Stack, Typography } from "../../../../ui";

interface EndGameOverlayProps {
    isOpen: boolean;
    isDraw: boolean;
    iWon: boolean;
    winnerName: string | null;
    onClose: () => void;
    onLeave: () => void;
}

export function EndGameOverlay({ isOpen, isDraw, iWon, winnerName, onClose, onLeave }: EndGameOverlayProps) {
    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            disableOutsideClick
            hideCloseButton
        >
            <Stack gap="1.5rem" align="center">

                <Typography variant="h2" style={{
                    color: isDraw
                        ? "var(--color-text)"
                        : iWon
                            ? "#2ecc71"
                            : "#e74c3c",
                }}>
                    {isDraw ? "Draw!" : iWon ? "You Won!" : "You Lost"}
                </Typography>

                {!isDraw && !iWon && winnerName && (
                    <Typography variant="body" style={{ opacity: 0.7 }}>
                        {winnerName} wins this round
                    </Typography>
                )}

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

                <Stack direction="row" gap="1rem" style={{ width: "100%", justifyContent: "center", marginTop: "0.5rem" }}>
                    <Button variant="outline" onClick={onClose} style={{ flex: 1 }}>
                        View Board
                    </Button>
                    <Button variant="solid" onClick={onLeave} style={{ flex: 1 }}>
                        Leave Room
                    </Button>
                </Stack>
            </Stack>
        </Modal>
    );
}
