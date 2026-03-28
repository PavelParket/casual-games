import { Box, Button, Input, Stack, Typography } from "../../../../ui";

interface BettingPanelProps {
    balance: number | undefined;
    betInput: string;
    betPlaced: boolean;
    ready: boolean;
    playerBetMap: Record<string, number> | undefined;
    players: Record<string, string> | undefined;
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

    return (
        <Stack gap="0.75rem" style={{
            padding: "1rem",
            background: "var(--color-bg-secondary)",
            borderRadius: "var(--radius-md)",
            border: "1px solid var(--color-border)",
        }}>
            {/* Current bets */}
            {hasBets && (
                <>
                    <Typography variant="h3">Current Bets</Typography>
                    <Stack gap="0.25rem">
                        {Object.entries(playerBetMap!).map(([guid, bet]) => (
                            <Box key={guid} style={{ display: "flex", justifyContent: "space-between" }}>
                                <Typography variant="body">{players?.[guid] ?? guid}</Typography>
                                <Typography variant="body" style={{ color: "var(--color-income-text)", fontWeight: 600 }}>
                                    ${bet}
                                </Typography>
                            </Box>
                        ))}
                    </Stack>
                    <Box style={{ height: "1px", background: "var(--color-border)" }} />
                </>
            )}

            {/* Bet input */}
            <Typography variant="h3">Place Your Bet</Typography>

            {balance !== undefined && (
                <Typography variant="caption" style={{ opacity: 0.7 }}>
                    Balance: ${balance.toFixed(2)}
                </Typography>
            )}

            <Input
                type="number"
                value={betInput}
                onChange={e => onBetInputChange(e.target.value)}
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
        </Stack>
    );
}
