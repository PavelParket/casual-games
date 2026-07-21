import { Button, Modal, Stack, Typography } from "../../../../ui";

interface DeadlockOverlayProps {
    isOpen: boolean;
    secondsLeft: number;
    onLeave: () => void;
}

export function DeadlockOverlay({ isOpen, secondsLeft, onLeave }: DeadlockOverlayProps) {
    return (
        <Modal
            isOpen={isOpen}
            onClose={() => { }}
            disableOutsideClick
            hideCloseButton
        >
            <Stack gap="1.5rem" align="center" style={{ padding: "1rem" }}>
                <Typography variant="h2" style={{ color: "var(--color-expense-text)", textAlign: "center" }}>
                    No Moves Left
                </Typography>

                <Typography variant="body" style={{ opacity: 0.8, textAlign: "center" }}>
                    Waiting for opponent to finish or reach a deadlock.
                </Typography>

                <Typography variant="h1" style={{ fontWeight: "bold", color: "var(--color-primary)" }}>
                    {secondsLeft}s
                </Typography>

                <Button variant="outline" onClick={onLeave} style={{ marginTop: "1rem", width: "100%" }}>
                    Leave Early
                </Button>
            </Stack>
        </Modal>
    );
}
