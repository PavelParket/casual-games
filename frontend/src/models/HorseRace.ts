export interface HorseRaceGameTick {
    tickIndex: number;
    positions: number[];
}

export interface HorseRaceGamePreset {
    roomId: string;
    horseCount: number;
    odds: number[];
}
