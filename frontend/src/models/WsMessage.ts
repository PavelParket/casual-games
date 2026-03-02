import type { HorseRaceHorseKeyframes } from "./HorseRace";

export interface WSMessage {
   type: string;
   event: string;
   fromUserId?: string;
   toUserId?: string;
   roomId: string;
   message?: string;
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
   code?: number;
   winner?: string;
   gameState?: string;
   isGameStarted?: boolean;
}
