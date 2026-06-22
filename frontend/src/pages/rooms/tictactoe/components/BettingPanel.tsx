import { Box, Button, FormField, Stack, Typography } from "../../../../ui";
import "../styles/TicTacToeRoom.css"

interface BettingPanelProps {
    balance: number | undefined;
    betInput: string;
    betPlaced: boolean;
    ready: boolean;
    isGame: boolean;
    playerBetMap: Record<string, number> | undefined;
    isConnected: boolean;
    gameAborted: boolean;
    onBetInputChange: (value: string) => void;
    onPlaceBet: () => void;
    onReady: () => void;
}

export function BettingPanel({
    balance,
    betInput,
    betPlaced,
    ready,
    isGame,
    playerBetMap,
    isConnected,
    gameAborted,
    onBetInputChange,
    onPlaceBet,
    onReady,
}: BettingPanelProps) {
    const hasBets = playerBetMap && Object.keys(playerBetMap).length > 0;

    const showForm = !ready && !isGame;

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
            {hasBets && (
                <Box style={{
                    display: "flex",
                    flexDirection: "column",
                    flex: showForm ? "none" : 1,
                    minHeight: 0
                }}>
                    <Typography variant="h3" style={{ marginBottom: "0.75rem" }}>Current Bets</Typography>

                    <Stack gap="1rem" justify="center">
                        {Object.entries(playerBetMap).map(([username, bet]) => (
                            <Box
                                key={username}
                                style={{
                                    display: "flex",
                                    justifyContent: "space-between",
                                    padding: "0.75rem",
                                    background: "var(--color-bg-secondary)",
                                    borderRadius: "var(--radius-sm)",
                                    border: "1px solid var(--color-border)",
                                }}>
                                <Typography variant="body">{username}</Typography>
                                <Typography variant="body" style={{ color: "var(--color-income-text)", fontWeight: 600 }}>
                                    {bet} CG
                                </Typography>
                            </Box>
                        ))}
                    </Stack>

                    {showForm && <Box style={{ height: "1px", background: "var(--color-border)", marginTop: "0.75rem" }} />}
                </Box>
            )}

            {isGame && !hasBets && (
                <Typography variant="caption" style={{ opacity: 0.6, textAlign: "center", margin: "auto 0" }}>
                    No bets placed this round
                </Typography>
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
                        onChange={(e) => onBetInputChange(e.target.value)}
                        placeholder="Enter bet amount"
                        disabled={betPlaced || !isConnected || isGame || gameAborted}
                    />

                    <Button
                        onClick={onPlaceBet}
                        disabled={betPlaced || !isConnected || isGame || gameAborted}
                        style={{ opacity: (betPlaced || isGame || gameAborted) ? 0.5 : 1 }}
                    >
                        Place Bet
                    </Button>

                    <Typography variant="caption" style={{ opacity: 0.6, fontSize: "0.8rem" }}>
                        {betPlaced
                            ? "Bet placed — you can now get ready!"
                            : "Place a bet before becoming ready"
                        }
                    </Typography>

                    <Button
                        onClick={onReady}
                        disabled={ready || !betPlaced || !isConnected || isGame || gameAborted}
                        style={{ opacity: (!betPlaced || ready || isGame || gameAborted) ? 0.5 : 1, marginTop: "auto" }}
                    >
                        {ready ? "Waiting..." : "Get Ready"}
                    </Button>
                </Box>
            )}

            {ready && !isGame && (
                <Typography variant="caption" style={{ textAlign: "center", color: "var(--color-text-secondary)", marginTop: "auto" }}>
                    Waiting for opponent...
                </Typography>
            )}
        </Stack>
    );
}
