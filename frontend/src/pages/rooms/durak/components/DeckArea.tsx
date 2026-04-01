import type { CardSuit, DurakCard } from "../../../../models/Durak";
import { Box, Typography } from "../../../../ui";
import { suitSymbol } from "../utils/CardUtils";
import { PlayingCard } from "./PlayingCard";

interface DeckAreaProps {
    deckCardsLeft: number;
    trumpCard: DurakCard | null;
    trumpSuit: CardSuit | null;
}

const STACK_OFFSETS = [0, 2, 4];

export function DeckArea({ deckCardsLeft, trumpCard, trumpSuit }: DeckAreaProps) {
    const isDeckEmpty = deckCardsLeft === 0;

    return (
        <Box style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: "0.4rem",
        }}>
            {/* ── Trump card + deck pile composite ── */}
            {!isDeckEmpty && (
                <Box style={{ position: "relative", overflow: "visible" }}>

                    {/* Trump card — rotated 90°, negative marginBottom makes deck pile slide on top */}
                    {trumpCard && (
                        <Box style={{
                            display: "flex",
                            justifyContent: "center",
                            marginBottom: "-62px",
                            position: "relative",
                            zIndex: 0,
                        }}>
                            <PlayingCard
                                card={trumpCard}
                                faceDown={false}
                                trumpRotated
                                size="md"
                            />
                        </Box>
                    )}

                    {/* Deck pile — front card is relative (takes up space), rest are absolute offsets */}
                    <Box style={{ position: "relative", zIndex: 1 }}>
                        {STACK_OFFSETS.map((offset, i) => (
                            <Box
                                key={i}
                                style={{
                                    position: i === 0 ? "relative" : "absolute",
                                    top: i === 0 ? undefined : -offset,
                                    left: i === 0 ? undefined : offset,
                                    zIndex: STACK_OFFSETS.length - i,
                                }}
                            >
                                <PlayingCard faceDown size="md" />
                            </Box>
                        ))}
                    </Box>
                </Box>
            )}

            {/* ── Empty deck — trump suit symbol placeholder ── */}
            {isDeckEmpty && trumpSuit && (
                <Box style={{
                    width: 62,
                    height: 88,
                    borderRadius: "var(--radius-sm)",
                    border: "1px dashed var(--color-border)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                }}>
                    <Typography variant="h2" style={{
                        color: trumpSuit === "HEARTS" || trumpSuit === "DIAMONDS"
                            ? "#e74c3c"
                            : "var(--color-text)",
                    }}>
                        {suitSymbol(trumpSuit)}
                    </Typography>
                </Box>
            )}

            {/* ── Card count badge ── */}
            {!isDeckEmpty && (
                <Typography variant="caption" style={{
                    background: "var(--color-bg-glass)",
                    border: "1px solid var(--color-border)",
                    borderRadius: "var(--radius-sm)",
                    padding: "0.1rem 0.5rem",
                    fontWeight: 600,
                    fontSize: "0.75rem",
                }}>
                    {deckCardsLeft}
                </Typography>
            )}
        </Box>
    );
}
