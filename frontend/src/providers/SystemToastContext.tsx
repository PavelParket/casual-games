import { type ReactNode } from "react";
import { SystemToastContext } from "../hooks/useSystemToastContext";
import { useSystemToast } from "../hooks/useSystemToast";
import { ToastContainer } from "../ui/components/common/ToastContainer";

export function SystemToastProvider({ children }: { children: ReactNode }) {
    const { toasts, showSystemToast, dismiss } = useSystemToast();

    return (
        <SystemToastContext.Provider value={{ showSystemToast }}>
            {children}
            <ToastContainer layer="system" toasts={toasts} dismiss={dismiss} />
        </SystemToastContext.Provider>
    );
};
