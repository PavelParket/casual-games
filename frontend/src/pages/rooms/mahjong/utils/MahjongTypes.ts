export type TileSuit = "BAMBOO" | "CHARACTERS" | "CIRCLES" | "WIND" | "DRAGON" | "FLOWER" | "SEASON";

export interface TileFace {
    suit: TileSuit;
    value: number;
}

export interface MahjongSlot {
    id: string;
    col: number;
    row: number;
    layer: number;
    covers: string[];
    leftId: string | null;
    rightId: string | null;
}

export interface MahjongTileData {
    slot: MahjongSlot;
    face: TileFace;
}
