import type { DurakAction, DurakPhase } from "../../../../models/Durak";
import { Box, Button, Stack } from "../../../../ui";

interface ActionButtonProps {
    availableActions: DurakAction[];
    phase: DurakPhase | null;
    disabled: boolean;
    onPass: () => void;
    onTakeCards: () => void;
}

export function ActionButton({ availableActions, phase, disabled, onPass, onTakeCards }: ActionButtonProps) {
    const canPass = availableActions.includes("PASS");
    const canTake = availableActions.includes("TAKE_CARDS");

    if (!canPass && !canTake) {
        return <Box style={{ height: "40px" }} />;
    }

    const passLabel = phase === "PICKING_UP" ? "Enough" : "Done";

    return (
        <Stack direction="row" gap="0.75rem" align="center" justify="center">
            {canTake && (
                <Button
                    variant="outline"
                    disabled={disabled}
                    onClick={onTakeCards}
                    style={{ minWidth: "110px" }}
                >
                    Take cards
                </Button>
            )}
            {canPass && (
                <Button
                    variant="solid"
                    disabled={disabled}
                    onClick={onPass}
                    style={{ minWidth: "110px" }}
                >
                    {passLabel}
                </Button>
            )}
        </Stack>
    );
}
