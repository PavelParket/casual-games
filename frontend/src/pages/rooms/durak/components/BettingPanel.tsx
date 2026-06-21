import { Box, Button, FormField, Stack, Typography } from "../../../../ui";
import type { PlayerResponse } from "../../../../models/Room";
import { validateAmountInput } from "../../../../utils/SecurityUtils";
import "../styles/DurakRoom.css";

interface BettingPanelProps {
    balance: number | undefined;
    betInput: string;
    betPlaced: boolean;
    ready: boolean;
    playerBetMap: Record<string, number> | undefined;
    players: Record<string, PlayerResponse> | undefined;
    isConnected: boolean;
    onBetInputChange: (value: string) => void;
    onPlaceBet: () => void;
    onReady: () => void;
}

export function BettingPanel({
    balance,
    betInput,
    betPlaced,
    ready,
    playerBetMap,
    players,
    isConnected,
    onBetInputChange,
    onPlaceBet,
    onReady,
}: BettingPanelProps) {
    const hasBets = playerBetMap && Object.keys(playerBetMap).length > 0;

    const handleBetChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const validated = validateAmountInput(e.target.value);
        if (validated !== null) {
            onBetInputChange(validated);
        }
    };

    return (
        <Stack gap="0.75rem" style={{
            padding: "1rem",
            background: "var(--color-bg-secondary)",
            borderRadius: "var(--radius-md)",
            border: "1px solid var(--color-border)",
            flex: 1,
            display: "flex",
            flexDirection: "column"
        }}>
            {/* Current bets */}
            {hasBets && (
                <Box style={{
                    display: "flex",
                    flexDirection: "column",
                    flex: !ready ? "none" : 1,
                    minHeight: 0
                }}>
                    <Typography variant="h3" style={{ marginBottom: "0.75rem" }}>Current Bets</Typography>
                    <Stack gap="1rem" justify="center">
                        {Object.entries(playerBetMap!).map(([guid, bet]) => (
                            <Box key={guid} style={{
                                display: "flex",
                                justifyContent: "space-between",
                                padding: "0.75rem",
                                background: "var(--color-bg-secondary)",
                                borderRadius: "var(--radius-sm)",
                                border: "1px solid var(--color-border)",
                            }}>
                                <Typography variant="body">{players?.[guid]?.username ?? guid}</Typography>
                                <Typography variant="body" style={{ color: "var(--color-income-text)", fontWeight: 600 }}>
                                    {bet} CG
                                </Typography>
                            </Box>
                        ))}
                    </Stack>
                    {!ready && <Box style={{ height: "1px", background: "var(--color-border)", marginTop: "0.75rem" }} />}
                </Box>
            )}

            {!ready && (
                <Box style={{ display: "flex", flexDirection: "column", gap: "0.75rem", flex: 1 }}>
                    <Typography variant="h3">Place Your Bet</Typography>

                    {balance !== undefined && (
                        <Typography variant="caption" style={{ opacity: 0.7 }}>
                            Balance: {balance.toFixed(2)} CG Coins
                        </Typography>
                    )}

                    <FormField
                        type="text"
                        inputMode="decimal"
                        value={betInput}
                        onChange={handleBetChange}
                        placeholder="Enter bet amount"
                        disabled={betPlaced || !isConnected}
                    />

                    <Button
                        onClick={onPlaceBet}
                        disabled={betPlaced || !isConnected}
                        style={{ opacity: betPlaced ? 0.5 : 1 }}
                    >
                        Place Bet
                    </Button>

                    <Typography variant="caption" style={{ opacity: 0.6, fontSize: "0.8rem" }}>
                        {betPlaced
                            ? "Bet placed — you can now get ready!"
                            : "Place a bet before becoming ready"
                        }
                    </Typography>

                    {/* Ready button */}
                    <Button
                        onClick={onReady}
                        disabled={ready || !betPlaced || !isConnected}
                        style={{ opacity: (!betPlaced || ready) ? 0.5 : 1 }}
                    >
                        {ready ? "Waiting..." : "Get Ready"}
                    </Button>

                </Box>
            )}

            {ready && (
                <Typography variant="caption" style={{ textAlign: "center", color: "var(--color-text-secondary)", marginTop: "auto" }}>
                    Waiting for opponent...
                </Typography>
            )}
        </Stack>
    );
}
