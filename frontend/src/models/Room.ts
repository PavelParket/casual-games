export interface Room {
    id: string;
    name: string;
    type: string;
    participantGuids: string[];
    participantCount: number;
}

export interface LastRoom {
    id: string | null;
    name: string | null;
    type: RoomType | null;
}

export const ROOM_TYPE_HANDLERS: Record<string, string> = {
    "TIC_TAC_TOE": "t-t-t",
} as const;

export type RoomType = keyof typeof ROOM_TYPE_HANDLERS;
