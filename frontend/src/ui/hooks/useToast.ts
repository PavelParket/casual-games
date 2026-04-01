import { useCallback, useEffect, useRef, useState } from "react";
import { TOAST_DURATIONS, TOAST_EXIT_DURATION_MS, type ToastItem, type ToastVariant } from "../models/ToastTypes";

interface UseToastProps {
    maxToasts: number;
}

export function useToast({ maxToasts }: UseToastProps) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);
    const timersRef = useRef<Map<string, ReturnType<typeof setTimeout>>>(new Map());
    const toastsRef = useRef<ToastItem[]>([]);

    useEffect(() => { toastsRef.current = toasts; });

    const dismiss = useCallback((id: string) => {
        const durationTimer = timersRef.current.get(id);
        if (durationTimer !== undefined) {
            clearTimeout(durationTimer);
            timersRef.current.delete(id);
        }

        setToasts(prev => prev.map(t => t.id === id ? { ...t, isClosing: true } : t));

        const exitTimer = setTimeout(() => {
            setToasts(prev => prev.filter(t => t.id !== id));
            timersRef.current.delete(`exit_${id}`);
        }, TOAST_EXIT_DURATION_MS);

        timersRef.current.set(`exit_${id}`, exitTimer);
    }, []);

    const showToast = useCallback((message: string, variant: ToastVariant) => {
        if (toastsRef.current.length >= maxToasts) {
            dismiss(toastsRef.current[0].id);
        }

        const id = crypto.randomUUID();
        const duration = TOAST_DURATIONS[variant];
        const newToast: ToastItem = { id, message, variant, duration, isClosing: false };

        setToasts(prev => [...prev, newToast]);

        const timer = setTimeout(() => dismiss(id), duration);
        timersRef.current.set(id, timer);
    }, [dismiss, maxToasts]);

    useEffect(() => {
        const timers = timersRef.current;
        return () => {
            timers.forEach(timer => clearTimeout(timer));
            timers.clear();
        };
    }, []);

    return { toasts, showToast, dismiss };
}
