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
}

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
    "DURAK": "Durak",
} as const;

export type RoomType = keyof typeof ROOM_TYPE_HANDLERS;

export type RoomSortField = "NAME" | "CREATED_AT";
export type SortDirection = "ASC" | "DESC";

export interface RoomFilterRequest {
    name?: string;
    types?: RoomType[];
    sortField: RoomSortField;
    sortDirection: SortDirection;
}

export interface RoomResponseMap {
    rooms: Record<RoomType, Room[]>;
}

export interface RoomStatus {
    status: string;
}

export interface PlayerResponse {
    guid: string;
    username: string;
    status: string;
    linkProfilePicture?: string | null;
    linkProfilePictureMini?: string | null;
}

/* ============================ */
/* ===== TIC TAC TOE ROOM ===== */
/* ============================ */
export interface PlayerBet {
    guid: string;
    bet: number;
};

/* ============================= */
/* ====== TICKET FOR ROOM ====== */
/* ============================= */
export interface WsTicketRequest {
    roomId: string;
};

export interface WsTicket {
    ticketId: string;
};
