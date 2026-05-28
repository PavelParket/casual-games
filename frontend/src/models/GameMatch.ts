import type { RoomType } from "./Room";

export type GameResult = "WIN" | "LOSS" | "DRAW";

export interface GameMatchRequestFilter {
    gameType: RoomType;
    isWinner?: boolean;
}

export interface GameMatchResponse {
    id: number;
    gameType: RoomType;
    roomId: string;
    gameResult: GameResult;
    winnerId: string | null;
    players: string[];
    createdAt: string;
}
