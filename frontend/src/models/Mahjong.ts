export type TileSuit = "BAMBOO" | "CHARACTERS" | "CIRCLES" | "WIND" | "DRAGON" | "FLOWER" | "SEASON";

export interface MahjongTile {
    slotId: string;
    suit: TileSuit;
    value: number;
}

export interface MahjongBoard {
    playerGuid: string;
    tiles: MahjongTile[];
}
