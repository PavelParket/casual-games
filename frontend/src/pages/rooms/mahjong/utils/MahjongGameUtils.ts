import { MAHJONG_LAYOUT } from "./MahjongLayoutData";
import type { MahjongSlot, MahjongTileData, TileFace } from "./MahjongTypes";
import { MahjongTileImages } from '../../../../assets/icons/mahjong-tiles-svg/index';

const NUM_MAP = ["", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE"] as const;
const WIND_MAP = ["", "EAST", "SOUTH", "WEST", "NORTH"] as const;
const DRAGON_MAP = ["", "RED", "GREEN", "WHITE"] as const;
const FLOWER_MAP = ["", "PLUM", "CHRYSANTHEMUM", "ORCHID", "BAMBOO"] as const;
const SEASON_MAP = ["", "SPRING", "SUMMER", "AUTUMN", "WINTER"] as const;

export function getTileAsset(face: TileFace): string {
    try {
        switch (face.suit) {
            case "BAMBOO": return MahjongTileImages.BAMBOO[NUM_MAP[face.value] as keyof typeof MahjongTileImages.BAMBOO];
            case "CHARACTERS": return MahjongTileImages.CHARACTERS[NUM_MAP[face.value] as keyof typeof MahjongTileImages.CHARACTERS];
            case "CIRCLES": return MahjongTileImages.CIRCLES[NUM_MAP[face.value] as keyof typeof MahjongTileImages.CIRCLES];
            case "WIND": return MahjongTileImages.WIND[WIND_MAP[face.value] as keyof typeof MahjongTileImages.WIND];
            case "DRAGON": return MahjongTileImages.DRAGON[DRAGON_MAP[face.value] as keyof typeof MahjongTileImages.DRAGON];
            case "FLOWER": return MahjongTileImages.FLOWER[FLOWER_MAP[face.value] as keyof typeof MahjongTileImages.FLOWER] || MahjongTileImages.FLOWER.PLUM;
            case "SEASON": return MahjongTileImages.SEASON[SEASON_MAP[face.value] as keyof typeof MahjongTileImages.SEASON] || MahjongTileImages.SEASON.SPRING;
        }
    } catch (e) {
        console.warn("Missing asset for face", face, e);
    }
    return "";
}

export function generateTestBoard(count: 72 | 144 = 72): MahjongTileData[] {
    const faces: TileFace[] = [];

    for (let value = 1; value <= 9; value++) faces.push({ suit: "BAMBOO", value });
    for (let value = 1; value <= 9; value++) faces.push({ suit: "CHARACTERS", value });
    for (let value = 1; value <= 9; value++) faces.push({ suit: "CIRCLES", value });
    for (let value = 1; value <= 4; value++) faces.push({ suit: "WIND", value });
    for (let value = 1; value <= 3; value++) faces.push({ suit: "DRAGON", value });
    faces.push({ suit: "FLOWER", value: 1 });
    faces.push({ suit: "SEASON", value: 1 });

    let allFaces: TileFace[] = [];
    if (count === 72) {
        allFaces = [...faces, ...faces];
    } else {
        allFaces = [...faces, ...faces, ...faces, ...faces];
    }

    for (let i = allFaces.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [allFaces[i], allFaces[j]] = [allFaces[j], allFaces[i]];
    }

    let layout = MAHJONG_LAYOUT;
    if (count === 144) {
        const shiftedLayout = MAHJONG_LAYOUT.map(s => ({
            ...s,
            id: "B_" + s.id,
            row: s.row + 8,
            covers: s.covers.map(c => "B_" + c),
            leftId: s.leftId ? "B_" + s.leftId : null,
            rightId: s.rightId ? "B_" + s.rightId : null
        }));
        layout = [...MAHJONG_LAYOUT, ...shiftedLayout];
    }

    return layout.map((slot, i) => ({
        slot,
        face: allFaces[i]
    }));
}

export function isTileFree(slot: MahjongSlot, removedIds: Set<string>): boolean {
    const uncovered = slot.covers.every(coverId => removedIds.has(coverId));
    const leftOpen = slot.leftId === null || removedIds.has(slot.leftId);
    const rightOpen = slot.rightId === null || removedIds.has(slot.rightId);
    return uncovered && (leftOpen || rightOpen);
}

export function facesMatch(face1: TileFace, face2: TileFace): boolean {
    if (face1.suit === "FLOWER" && face2.suit === "FLOWER") return true;
    if (face1.suit === "SEASON" && face2.suit === "SEASON") return true;
    return face1.suit === face2.suit && face1.value === face2.value;
}

export function getAvailableMoves(tiles: MahjongTileData[], removedIds: Set<string>): number {
    const freeTiles = tiles.filter(t => !removedIds.has(t.slot.id) && isTileFree(t.slot, removedIds));
    let moves = 0;
    for (let i = 0; i < freeTiles.length; i++) {
        for (let j = i + 1; j < freeTiles.length; j++) {
            if (facesMatch(freeTiles[i].face, freeTiles[j].face)) {
                moves++;
            }
        }
    }
    return moves;
}

export function generateAllUniqueFaces(): TileFace[] {
    const allUniqueFaces: TileFace[] = [];
    for (let i = 1; i <= 9; i++) {
        allUniqueFaces.push({ suit: "CHARACTERS", value: i });
        allUniqueFaces.push({ suit: "CIRCLES", value: i });
        allUniqueFaces.push({ suit: "BAMBOO", value: i });
    }
    for (let i = 1; i <= 4; i++) allUniqueFaces.push({ suit: "WIND", value: i });
    for (let i = 1; i <= 3; i++) allUniqueFaces.push({ suit: "DRAGON", value: i });
    for (let i = 1; i <= 4; i++) allUniqueFaces.push({ suit: "FLOWER", value: i });
    for (let i = 1; i <= 4; i++) allUniqueFaces.push({ suit: "SEASON", value: i });
    return allUniqueFaces;
}
