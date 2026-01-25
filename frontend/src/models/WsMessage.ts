export interface WSMessage {
   type: string;
   event: string;
   fromUserId?: string;
   toUserId?: string;
   roomName: string;
   message?: string;
}

export interface GameMessage extends WSMessage {
   board?: string[];
   cell?: number;
   currentPlayerSymbol?: string;
   nextPlayerSymbol?: string;
   playersSymbols?: Record<string, string>;
   players?: Record<string, string>;
   winner?: string;
   bet?: number;
}
