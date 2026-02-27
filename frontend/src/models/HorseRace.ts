export interface HorseRaceKeyframe {
    offset: number;
    position: number;
}

export interface HorseRaceHorseKeyframes {
    horseIndex: number;
    keyframes: HorseRaceKeyframe[];
}

export interface HorseRaceGamePreset {
    roomId: string;
    horseCount: number;
    odds: number[];
}
