import { Box, Button, FormField, Typography } from "../../../../ui";
import HorseSprite from "../../../../assets/sprites/HorseSprite";
import { HORSE_COLORS } from "../../../../models/HorseRace";

interface BettingPanelProps {
    odds: number[];
    phase: string;
    winnerIndex: number | undefined;
    selectedHorse: number | null;
    hoveredHorse: number | null;
    betPlaced: boolean;
    placedBetInfo: { horseIndex: number; amount: number } | null;
    balance: number | undefined;
    betInput: string;
    potentialWin: number | null;
    ready: boolean;
    inDrawer?: boolean;
    onSelectHorse: (index: number) => void;
    onHoverHorse: (index: number | null) => void;
    onBetChange: (val: string) => void;
    onPlaceBet: () => void;
    onReady: () => void;
}

export function BettingPanel({
    odds,
    phase,
    winnerIndex,
    selectedHorse,
    hoveredHorse,
    betPlaced,
    placedBetInfo,
    balance,
    betInput,
    potentialWin,
    ready,
    inDrawer = false,
    onSelectHorse,
    onHoverHorse,
    onBetChange,
    onPlaceBet,
    onReady
}: BettingPanelProps) {
    const isBetButtonDisabled = betPlaced || selectedHorse === null || !betInput || parseFloat(betInput) <= 0;

    return (
        <Box style={{
            width: inDrawer ? "100%" : "220px",
            flexShrink: 0,
            padding: inDrawer ? "1rem 1.5rem" : "1rem",
            display: "flex",
            flexDirection: "column",
            gap: "0.5rem",
            background: inDrawer ? "transparent" : "var(--color-bg-secondary)",
            borderRadius: inDrawer ? "0" : "var(--radius-md)",
            border: inDrawer ? "none" : "1px solid var(--color-border)",
            minHeight: inDrawer ? "100%" : "auto"
        }}>
            <Typography variant="h3" style={{ fontSize: "1rem", fontWeight: 700 }}>
                Place a Bet
            </Typography>

            {odds.length === 0 ? (
                <Typography variant="caption" style={{ color: "var(--color-text-secondary)" }}>—</Typography>
            ) : (
                odds.map((odd, i) => {
                    const color = HORSE_COLORS[i % HORSE_COLORS.length];
                    const isWinner = phase === "FINISHED" && winnerIndex === i;
                    const isSelected = selectedHorse === i;
                    const isMyBet = betPlaced && placedBetInfo?.horseIndex === i;
                    const isActive = isSelected || isMyBet;
                    const isClickable = !betPlaced && phase === "LOBBY";
                    const isHovered = hoveredHorse === i && isClickable && !isActive;

                    return (
                        <Box
                            key={i}
                            onClick={() => { if (isClickable) onSelectHorse(i); }}
                            onMouseEnter={() => { if (isClickable) onHoverHorse(i); }}
                            onMouseLeave={() => onHoverHorse(null)}
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                gap: "0.75rem",
                                padding: "0.4rem 0.5rem",
                                borderRadius: "var(--radius-sm)",
                                border: isActive ? "1.5px solid var(--color-text)" : "1.5px solid transparent",
                                background: (isHovered || isActive) ? "rgba(128, 128, 128, 0.12)" : "transparent",
                                cursor: isClickable ? "pointer" : "default",
                                opacity: isWinner ? 1 : phase === "FINISHED" ? 0.45 : betPlaced && !isMyBet ? 0.45 : 1,
                                transition: "background 0.12s, border-color 0.12s, opacity 0.2s",
                            }}
                        >
                            <Box style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                                <Typography variant="body" style={{ fontWeight: 700, fontSize: "16px", lineHeight: 1, color }}>
                                    #{i + 1}
                                </Typography>
                                <HorseSprite color={color} size={48} isRunning={false} isWinner={isWinner} />
                            </Box>
                            <Typography variant="body" style={{ color: "var(--color-text-secondary)", fontSize: "0.875rem", fontWeight: 500, fontVariantNumeric: "tabular-nums" }}>
                                {odd.toFixed(1)}x
                            </Typography>
                        </Box>
                    );
                })
            )}

            {!ready && (
                <Box style={{ borderTop: "1px solid var(--color-border)", marginTop: "0.375rem", paddingTop: "0.75rem", display: "flex", flexDirection: "column", gap: "0.5rem" }}>

                    {balance !== undefined && (
                        <Typography variant="caption" style={{ color: "var(--color-text-secondary)", fontSize: "0.75rem" }}>
                            Balance: {balance.toFixed(2)} CG Coins
                        </Typography>)}

                    <Typography variant="caption" style={{ color: "var(--color-text-secondary)", fontWeight: 600 }}>
                        {selectedHorse !== null ? `Horse #${selectedHorse + 1} · ${odds[selectedHorse]?.toFixed(1)}x` : "Select a horse above"}
                    </Typography>

                    <FormField
                        type="text"
                        inputMode="decimal"
                        value={betInput}
                        onChange={(e) => onBetChange(e.target.value)}
                        placeholder="Amount"
                        style={{ width: "100%", background: "var(--color-bg)", color: "var(--color-text)", fontSize: "0.875rem" }}
                    />

                    {potentialWin !== null && (
                        <Typography variant="caption" style={{ fontSize: "0.75rem", color: "var(--color-text-secondary)" }}>
                            Win: <span style={{ color: "var(--color-success, #2ecc71)", fontWeight: 600 }}>{potentialWin.toFixed(2)} CG</span>
                        </Typography>
                    )}

                    <Button onClick={onPlaceBet} disabled={isBetButtonDisabled} style={{ width: "100%", opacity: isBetButtonDisabled ? 0.5 : 1 }}>
                        Place Bet
                    </Button>

                    <Button
                        onClick={onReady}
                        disabled={!betPlaced || ready || phase !== "LOBBY"}
                        style={{ opacity: (!betPlaced || ready) ? 0.5 : 1, marginTop: "0.5rem" }}
                    >
                        {ready ? "Waiting..." : "Get Ready"}
                    </Button>
                </Box>
            )}
        </Box>
    );
}
