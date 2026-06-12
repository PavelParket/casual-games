import type { RoomType } from "./Room";

export type GameResult = "WIN" | "LOSS" | "DRAW";
export type ResultFilter = "ALL" | "WINS" | "LOSSES";

export const RESULT_FILTER_LABELS: Record<ResultFilter, string> = {
    ALL: "All",
    WINS: "Win",
    LOSSES: "Loss",
} as const;

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

export interface GamePageMetadata {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface GamePageResponse<T> {
    content: T[];
    page: GamePageMetadata;
}
