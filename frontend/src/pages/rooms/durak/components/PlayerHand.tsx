import { AnimatePresence, motion } from "framer-motion";
import { Box, Typography } from "../../../../ui";
import { PlayingCard } from "./PlayingCard";
import { cardId, sortCards } from "../utils/CardUtils";
import type { CardSuit, DurakAction, DurakCard } from "../../../../models/Durak";

interface PlayerHandProps {
    cards: DurakCard[];
    trumpSuit: CardSuit | null;
    isMyTurn: boolean;
    availableActions: DurakAction[];
    playerName: string;
    disabled: boolean;
    tableRef: React.RefObject<HTMLDivElement | null>;
    onPlayCard: (card: DurakCard) => void;
}

export function PlayerHand({
    cards,
    trumpSuit,
    isMyTurn,
    availableActions,
    playerName,
    disabled,
    tableRef,
    onPlayCard,
}: PlayerHandProps) {
    const canPlay = isMyTurn && availableActions.includes("PLAY_CARD") && !disabled;

    const sortedCards = trumpSuit ? sortCards(cards, trumpSuit) : cards;

    const handleDragEnd = (
        card: DurakCard,
        _event: MouseEvent | TouchEvent | PointerEvent,
        info: { point: { x: number; y: number } }
    ) => {
        if (!canPlay) return;

        const tableEl = tableRef.current;
        if (!tableEl) return;

        const rect = tableEl.getBoundingClientRect();
        const { x, y } = info.point;

        const droppedOnTable =
            x >= rect.left && x <= rect.right &&
            y >= rect.top && y <= rect.bottom;

        if (droppedOnTable) {
            onPlayCard(card);
        }
    };

    return (
        <Box style={{
            display: "flex",
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "center",
            gap: "0.75rem",
            padding: "0.5rem 0",
        }}>
            {/* Name + badge */}
            <Box style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "flex-start",
                gap: "0.25rem",
                minWidth: "80px",
                flexShrink: 0,
            }}>
                <Typography variant="caption" style={{
                    fontWeight: 600,
                    fontSize: "0.8rem",
                    whiteSpace: "nowrap",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    maxWidth: "80px",
                }}>
                    {playerName}
                </Typography>
                <Typography variant="caption" style={{
                    background: "var(--color-bg-glass)",
                    border: "1px solid var(--color-border)",
                    borderRadius: "var(--radius-sm)",
                    padding: "0.1rem 0.4rem",
                    fontSize: "0.7rem",
                    fontWeight: 600,
                }}>
                    {cards.length} cards
                </Typography>
            </Box>

            {/* Card row */}
            <motion.div
                /* animate={canPlay ? {
                    boxShadow: [
                        "0 0 0px rgba(255,215,0,0)",
                        "0 0 16px rgba(255,215,0,0.6)",
                        "0 0 0px rgba(255,215,0,0)",
                    ],
                } : { boxShadow: "none" }}
                transition={canPlay ? { duration: 1.5, repeat: Infinity, ease: "easeInOut" } : {}} */
                style={{
                    display: "flex",
                    flexDirection: "row",
                    alignItems: "center",
                    borderRadius: "var(--radius-sm)",
                    padding: "4px",
                }}
            >
                <AnimatePresence mode="popLayout">
                    {sortedCards.map((card, i) => (
                        <motion.div
                            key={cardId(card)}
                            initial={{ scale: 0.5, opacity: 0, y: -20 }}
                            animate={{ scale: 1, opacity: 1, y: 0 }}
                            exit={{ scale: 0.5, opacity: 0, y: 20 }}
                            transition={{ duration: 0.2 }}
                            style={{ marginLeft: i === 0 ? 0 : -20 }}
                        >
                            <PlayingCard
                                card={card}
                                faceDown={false}
                                size="md"
                                layoutId={cardId(card)}
                                draggable={canPlay}
                                disabled={!canPlay}
                                onClick={canPlay ? () => onPlayCard(card) : undefined}
                                onDragEnd={(event, info) => handleDragEnd(card, event, info)}
                            />
                        </motion.div>
                    ))}
                </AnimatePresence>

                {cards.length === 0 && (
                    <Typography variant="caption" style={{
                        color: "var(--color-text)",
                        opacity: 0.4,
                        fontSize: "0.75rem",
                        padding: "0 0.5rem",
                    }}>
                        No cards
                    </Typography>
                )}
            </motion.div>
        </Box>
    );
}
