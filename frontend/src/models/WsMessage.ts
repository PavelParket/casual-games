import type { CardSuit, DurakAction, DurakCard, DurakPhase, DurakTablePair } from "./Durak";
import type { DeCoderGameHistory } from "./DeCoderGameHistory";
import type { HorseRaceHorseKeyframes } from "./HorseRace";

export interface WSMessage {
    type: string;
    event: string;
    fromUserId?: string;
    toUserId?: string;
    roomId: string;
    message?: string;
}

export interface ErrorWSMessage extends WSMessage {
    errorCode?: string;
}

export interface TicTacToeGameMessage extends WSMessage {
    board?: string[];
    cell?: number;
    currentPlayerSymbol?: string;
    nextPlayerSymbol?: string;
    playersSymbols?: Record<string, string>;
    players?: Record<string, string>;
    winner?: string;
    bet?: number;
}

export interface HorseRaceGameMessage extends WSMessage {
    participants?: Record<string, string>;
    horseCount?: number;
    odds?: number[];
    seedHash?: string;
    serverSeed?: string;
    winnerHorseIndex?: number;
    segmentsCount?: number;
    horseKeyframes?: HorseRaceHorseKeyframes[];
    horseIndex?: number;
    bet?: number;
    remainingSeconds?: number;
}

export interface DeCoderMessage extends WSMessage {
    player?: string;
    code?: string;
    winner?: string;
    gameState?: DeCoderGameHistory[];
    isGameStarted?: boolean;
    jackpot?: number;
    spent?: number;
}

export interface DurakGameMessage extends WSMessage {
    action?: DurakAction;
    card?: DurakCard;
    bet?: number;
    gameId?: number;
    playerGuid?: string;
    phase?: DurakPhase;
    myCards?: DurakCard[];
    opponentCardCount?: number;
    deckCardsLeft?: number;
    trumpCard?: DurakCard;
    trumpSuit?: CardSuit;
    table?: DurakTablePair[];
    isMyTurn?: boolean;
    availableActions?: DurakAction[];
    attackerId?: string;
    defenderId?: string;
    winnerId?: string;
    remainingSeconds?: number;
}
