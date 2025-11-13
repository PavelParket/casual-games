export interface WSMessage {
   type: string;
   event?: string;
   fromUserId?: string;
   toUserId?: string;
   roomName?: string;
   message?: string;
}

export interface GameMessage extends WSMessage {
   board?: (string | null)[];
   cell?: number;
   player?: string;
   nextPlayer?: string;
   playersSymbols?: Record<string, string>;
   players?: string[];
   winner?: string;
}