import { useRef } from "react";
import type { CardSuit, DurakAction, DurakCard, DurakPhase, DurakTablePair } from "../../../../models/Durak";
import { Box } from "../../../../ui";
import { DeckArea } from "./DeckArea";
import { OpponentHand } from "./OpponentHand";
import { PlayerHand } from "./PlayerHand";
import { TableArea } from "./TableArea";
import { DiscardPile } from "./DiscardPile";
import { TurnTimer } from "./TurnTimer";
import { ActionButton } from "./ActionButton";

interface DurakBoardProps {
    myCards: DurakCard[];
    opponentCardCount: number;
    deckCardsLeft: number;
    trumpCard: DurakCard | null;
    trumpSuit: CardSuit | null;
    table: DurakTablePair[];
    phase: DurakPhase | null;
    isMyTurn: boolean;
    availableActions: DurakAction[];
    remainingSeconds: number | null;
    discardCount: number;
    playerName: string;
    opponentName: string;
    onPlayCard: (card: DurakCard) => void;
    onPass: () => void;
    onTakeCards: () => void;
    disabled: boolean;
}

export function DurakBoard({
    myCards,
    opponentCardCount,
    deckCardsLeft,
    trumpCard,
    trumpSuit,
    table,
    phase,
    isMyTurn,
    availableActions,
    remainingSeconds,
    discardCount,
    playerName,
    opponentName,
    onPlayCard,
    onPass,
    onTakeCards,
    disabled,
}: DurakBoardProps) {
    const tableRef = useRef<HTMLDivElement>(null);

    return (
        <Box style={{
            display: "grid",
            gridTemplateAreas: `
                "opponent opponent opponent sidebar"
                "deck     table    table    sidebar"
                ".        action   action   sidebar"
                "player   player   player   sidebar"
            `,
            gridTemplateColumns: "120px 1fr 1fr 160px",
            gridTemplateRows: "auto 1fr auto auto",
            gap: "0.75rem",
            minHeight: "520px",
            padding: "0.75rem",
        }}>

            {/* ── Opponent hand ── */}
            <Box style={{ gridArea: "opponent" }}>
                <OpponentHand
                    cardCount={opponentCardCount}
                    opponentName={opponentName}
                />
            </Box>

            {/* ── Deck + trump ── */}
            <Box style={{ gridArea: "deck", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <DeckArea
                    deckCardsLeft={deckCardsLeft}
                    trumpCard={trumpCard}
                    trumpSuit={trumpSuit}
                />
            </Box>

            {/* ── Table ── */}
            <Box style={{ gridArea: "table", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <TableArea
                    table={table}
                    tableRef={tableRef}
                />
            </Box>

            {/* ── Action buttons ── */}
            <Box style={{ gridArea: "action", display: "flex", alignItems: "center", justifyContent: "center", padding: "0.25rem 0" }}>
                <ActionButton
                    availableActions={availableActions}
                    phase={phase}
                    disabled={disabled}
                    onPass={onPass}
                    onTakeCards={onTakeCards}
                />
            </Box>

            {/* ── Player hand ── */}
            <Box style={{ gridArea: "player" }}>
                <PlayerHand
                    cards={myCards}
                    trumpSuit={trumpSuit}
                    isMyTurn={isMyTurn}
                    availableActions={availableActions}
                    playerName={playerName}
                    disabled={disabled}
                    tableRef={tableRef}
                    onPlayCard={onPlayCard}
                />
            </Box>

            {/* ── Sidebar: timer + discard ── */}
            <Box style={{
                gridArea: "sidebar",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: "1rem",
                paddingTop: "0.5rem",
            }}>
                <TurnTimer remainingSeconds={remainingSeconds} isMyTurn={isMyTurn} />
                <DiscardPile discardCount={discardCount} />
            </Box>

        </Box>
    );
}
