import { useCallback } from "react";
import { useToast } from "../ui/hooks/useToast";
import { type ToastVariant } from "../ui/models/ToastTypes";

export function useGameToast() {
    const { toasts, showToast, dismiss } = useToast({ maxToasts: 3 });

    const showGameToast = useCallback((message: string, variant: ToastVariant) => {
        showToast(message, variant);
    }, [showToast]);

    return { toasts, showGameToast, dismiss };
}
