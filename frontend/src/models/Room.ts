/* ============================= */
/* ========== GENERAL ========== */
/* ============================= */
export interface Room {
    id: string;
    name: string;
    type: RoomType;
    participantGuids: string[];
    participantCount: number;
}

export interface RoomRequest {
    roomName: string;
    roomType: RoomType;
};

export const ROOM_TYPE_HANDLERS: Record<string, string> = {
    "TIC_TAC_TOE": "t-t-t",
    "DE_CODER":"de-coder",
    "HORSE_RACE": "horse-race",
} as const;

export const ROOM_TYPE_LABELS: Record<string, string> = {
    "TIC_TAC_TOE": "Tic Tac Toe",
    "HORSE_RACE": "Horse Race",
    "DE_CODER":"De-Coder",
    "ROOM_TEST": "Room Test",
} as const;

export type RoomType = keyof typeof ROOM_TYPE_HANDLERS;

/* ============================ */
/* ===== TIC TAC TOE ROOM ===== */
/* ============================ */
export interface PlayerBet {
    guid: string;
    bet: number;
};
