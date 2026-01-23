export interface Room {
    id: string;
    name: string;
    type: string;
    participantGuids: string[];
    participantCount: number;
}

export interface RoomType {
    name: string;
    label: string;
    handlerUrl: string;
}

export interface LastRoom {
    id: string | null;
    name: string | null;
    type: RoomType | null;
}
