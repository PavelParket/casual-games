import { useCallback } from "react";
import { useToast } from "../ui/hooks/useToast";
import { type ToastVariant } from "../ui/models/ToastTypes";

export function useSystemToast() {
    const { toasts, showToast, dismiss } = useToast({ maxToasts: 3 });

    const showSystemToast = useCallback((message: string, variant: ToastVariant) => {
        showToast(message, variant);
    }, [showToast]);

    return { toasts, showSystemToast, dismiss };
}
