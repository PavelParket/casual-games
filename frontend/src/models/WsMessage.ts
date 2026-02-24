import type { HorseRaceGameTick } from "./HorseRace";

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
   ticks?: HorseRaceGameTick[];
   horseIndex?: number;
   bet?: number;
}
