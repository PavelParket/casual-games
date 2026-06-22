import { useEffect, useRef, useState } from "react";
import type { CardSuit, DurakAction, DurakCard, DurakPhase, DurakTablePair } from "../../../../models/Durak";
import { Box } from "../../../../ui";
import { DeckArea } from "./DeckArea";
import { OpponentHand } from "./OpponentHand";
import { PlayerHand } from "./PlayerHand";
import { TableArea } from "./TableArea";
import { DiscardPile } from "./DiscardPile";
import { TurnTimer } from "./TurnTimer";
import { ActionButton } from "./ActionButton";
import { LayoutGroup } from "framer-motion";
import type { TableExitMode } from "../../DurakRoom";
import "../styles/DurakRoom.css";

const DEAL_STAGE_COUNTS = [0, 1, 3, 6] as const;

const DEAL_STAGE_DELAYS = [50, 500, 950] as const;

const DEAL_DONE_DELAY = 1500;

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
    isOpponentAttacker: boolean;
    tableExitMode: TableExitMode;
    isDealAnimation: boolean;
    onDealComplete: () => void;
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
    isOpponentAttacker,
    tableExitMode,
    isDealAnimation,
    onDealComplete,
    onPlayCard,
    onPass,
    onTakeCards,
    disabled,
}: DurakBoardProps) {
    const tableRef = useRef<HTMLDivElement>(null);
    const discardPileRef = useRef<HTMLDivElement>(null);

    const [dealStage, setDealStage] = useState(0);

    useEffect(() => {
        if (!isDealAnimation) {
            setDealStage(0);
            return;
        }

        const timers: ReturnType<typeof setTimeout>[] = [];

        DEAL_STAGE_DELAYS.forEach((delay, i) => {
            timers.push(setTimeout(() => setDealStage(i + 1), delay));
        });

        timers.push(setTimeout(() => onDealComplete(), DEAL_DONE_DELAY));

        return () => timers.forEach(clearTimeout);
    }, [isDealAnimation, onDealComplete]);

    const visibleMyCards = isDealAnimation
        ? myCards.slice(0, DEAL_STAGE_COUNTS[dealStage])
        : myCards;

    const visibleOpponentCount = isDealAnimation
        ? DEAL_STAGE_COUNTS[dealStage]
        : opponentCardCount;

    return (
        <LayoutGroup id="durak-board">
            <Box className="durak-board-grid">
                <Box className="durak-area-opponent">
                    <OpponentHand
                        cardCount={visibleOpponentCount}
                        opponentName={opponentName}
                    />
                </Box>

                <Box className="durak-area-timer">
                    <TurnTimer remainingSeconds={remainingSeconds} isMyTurn={isMyTurn} />
                </Box>

                <Box className="durak-area-deck">
                    <DeckArea deckCardsLeft={deckCardsLeft} trumpCard={trumpCard} trumpSuit={trumpSuit} />
                </Box>

                <Box className="durak-area-table">
                    <TableArea
                        table={table}
                        tableRef={tableRef}
                        isOpponentAttacker={isOpponentAttacker}
                        tableExitMode={tableExitMode}
                        discardPileRef={discardPileRef}
                    />
                </Box>

                <Box className="durak-area-discard">
                    <DiscardPile ref={discardPileRef} discardCount={discardCount} />
                </Box>

                <Box className="durak-area-action">
                    <ActionButton
                        availableActions={availableActions}
                        phase={phase}
                        disabled={disabled}
                        onPass={onPass}
                        onTakeCards={onTakeCards}
                    />
                </Box>

                <Box className="durak-area-player">
                    <PlayerHand
                        cards={visibleMyCards}
                        trumpSuit={trumpSuit}
                        isMyTurn={isMyTurn}
                        availableActions={availableActions}
                        playerName={playerName}
                        disabled={disabled}
                        tableRef={tableRef}
                        onPlayCard={onPlayCard}
                    />
                </Box>

            </Box>
        </LayoutGroup>
    );
}
