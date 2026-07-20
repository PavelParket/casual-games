import { Box, Button, Modal, Stack, Typography } from "../../../../ui";

interface EndGameOverlayProps {
    isOpen: boolean;
    won: boolean;
    betAmount: number;
    winAmount: number;
    onClose: () => void;
    onLeave: () => void;
}

export function EndGameOverlay({ isOpen, won, betAmount, winAmount, onClose, onLeave }: EndGameOverlayProps) {
    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            disableOutsideClick
            hideCloseButton
        >
            <Stack gap="1.5rem" align="center" style={{ padding: "1rem 0" }}>

                <Typography variant="h2" style={{ color: won ? "#2ecc71" : "#e74c3c" }}>
                    {won ? "You Won!" : "You Lost"}
                </Typography>

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
