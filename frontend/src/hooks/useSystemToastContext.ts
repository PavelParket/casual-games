import { createContext, useContext } from "react";
import type { ToastVariant } from "../ui";

export interface SystemToastContextValue {
    showSystemToast: (message: string, variant: ToastVariant) => void;
}

export const SystemToastContext = createContext<SystemToastContextValue | null>(null);

export function useSystemToastContext(): SystemToastContextValue {
    const ctx = useContext(SystemToastContext);

    if (!ctx) {
        throw new Error("useSystemToastContext must be used within SystemToastProvider");
    }

    return ctx;
}
