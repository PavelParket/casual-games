import { Box, Button, Modal, Stack, Typography } from "../../../../ui";

interface EndGameOverlayProps {
    isOpen: boolean;
    isWin: boolean;
    winnerName?: string;
    jackpot: number;
    onClose: () => void;
    onLeave: () => void;
}

export function EndGameOverlay({ isOpen, isWin, winnerName, jackpot, onClose, onLeave }: EndGameOverlayProps) {
    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            disableOutsideClick
            hideCloseButton
        >
            <Stack gap="1.5rem" align="center">

                <Typography variant="h2" style={{
                    color: isWin ? "#2ecc71" : "var(--color-text)",
                }}>
                    {isWin ? "You Cracked the Code!" : `${winnerName || "Someone"} won!`}
                </Typography>

                {!isWin && (
                    <Typography variant="body" style={{ opacity: 0.7 }}>
                        Better luck next time. The code has been deciphered.
                    </Typography>
                )}

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
