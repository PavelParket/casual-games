import { createContext, useContext, type ReactNode } from "react";
import type { ToastVariant } from "../ui/models/ToastTypes";
import { useSystemToast } from "../hooks/useSystemToast";
import { ToastContainer } from "../ui/components/common/ToastContainer";

interface SystemToastContextValue {
    showSystemToast: (message: string, variant: ToastVariant) => void;
};

const SystemToastContext = createContext<SystemToastContextValue | null>(null);

export function SystemToastProvider({ children }: { children: ReactNode }) {
    const { toasts, showSystemToast, dismiss } = useSystemToast();

    return (
        <SystemToastContext.Provider value={{ showSystemToast }}>
            {children}
            <ToastContainer layer="system" toasts={toasts} dismiss={dismiss} />
        </SystemToastContext.Provider>
    );
};

export function useSystemToastContext(): SystemToastContextValue {
    const ctx = useContext(SystemToastContext);

    if (!ctx) {
        throw new Error("useSystemToastContext must be used within SystemToastProvider");
    }

    return ctx;
}
