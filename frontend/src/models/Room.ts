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
    "DE_CODER": "de-coder",
    "HORSE_RACE": "horse-race",
    "DURAK": "durak",
} as const;

export const ROOM_TYPE_LABELS: Record<string, string> = {
    "TIC_TAC_TOE": "Tic Tac Toe",
    "HORSE_RACE": "Horse Race",
    "DE_CODER": "De-Coder",
    "DURAK": "Durak (card game)",
} as const;

export type RoomType = keyof typeof ROOM_TYPE_HANDLERS;

export interface RoomStatus {
    status: string;
}

/* ============================ */
/* ===== TIC TAC TOE ROOM ===== */
/* ============================ */
export interface PlayerBet {
    guid: string;
    bet: number;
};
