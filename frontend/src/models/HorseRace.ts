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

export interface PlacedBetInfo {
    horseIndex: number;
    amount: number;
}

export const HORSE_FILTERS: Record<string, string> = {
    "#e74c3c": "sepia(1) saturate(5) hue-rotate(327deg) brightness(1.1)",
    "#e67e22": "sepia(1) saturate(5) hue-rotate(349deg) brightness(1.1)",
    "#2ecc71": "sepia(1) saturate(5) hue-rotate(98deg)  brightness(1.0)",
    "#3498db": "sepia(1) saturate(5) hue-rotate(169deg) brightness(1.0)",
    "#9b59b6": "sepia(1) saturate(5) hue-rotate(245deg) brightness(1.0)",
    "#f1c40f": "sepia(1) saturate(5) hue-rotate(11deg)  brightness(1.2)",
    "#e91e63": "sepia(1) saturate(5) hue-rotate(299deg) brightness(1.1)",
    "#1abc9c": "sepia(1) saturate(5) hue-rotate(130deg) brightness(1.0)",
};
