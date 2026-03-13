import { useToast } from "../ui/hooks/useToast";
import { type ToastVariant } from "../ui/models/ToastTypes";

export function useSystemToast() {
    const { toasts, showToast, dismiss } = useToast({ maxToasts: 3 });

    const showSystemToast = (message: string, variant: ToastVariant) => showToast(message, variant);

    return { toasts, showSystemToast, dismiss };
}
