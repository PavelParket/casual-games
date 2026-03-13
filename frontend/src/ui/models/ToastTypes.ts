export type ToastVariant = "game-info" | "game-error" | "system-info" | "system-error";

export type ToastLayer = "game" | "system";

export const TOAST_DURATIONS: Record<ToastVariant, number> = {
    "game-info": 3000,
    "game-error": 4500,
    "system-info": 3000,
    "system-error": 7000,
};

export const TOAST_EXIT_DURATION_MS = 300;

export interface ToastItem {
    id: string;
    message: string;
    variant: ToastVariant;
    duration: number;
    isClosing: boolean;
};
