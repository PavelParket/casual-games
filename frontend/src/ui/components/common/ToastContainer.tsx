import { createPortal } from "react-dom";
import "../styles/toastcontainer.css";
import type { ToastItem, ToastLayer } from "../../models/ToastTypes";
import { classNames } from "../../utils/classNames";
import { Icon } from "./Icon";
import { useThemedIcon } from "../../hooks/useThemedIcon";

interface ToastItemProps {
    toast: ToastItem;
    onDismiss: (id: string) => void;
}

function ToastItemEl({ toast, onDismiss }: ToastItemProps) {
    const isInfo = toast.variant.endsWith("-info");
    const { getIcon } = useThemedIcon();

    return (
        <div
            className={classNames(
                "toast-item",
                `toast-item--${toast.variant}`,
                toast.isClosing ? "toast-item--closing" : "toast-item--entering",
            )}
            style={{ "--toast-duration": `${toast.duration}ms` } as React.CSSProperties}
            role="alert"
            aria-live="polite"
        >
            <span className="toast-item__icon" aria-hidden="true">
                {isInfo ? "✓" : "✕"}
            </span>

            <span className="toast-item__message">{toast.message}</span>

            <button
                className="toast-item__close"
                onClick={() => onDismiss(toast.id)}
                aria-label="Close notification"
            >
                <Icon src={getIcon("close")} size={16} alt="close" />
            </button>

            {!toast.isClosing && (
                <div className="toast-item__progress" />
            )}
        </div>
    );
}

// -------------- ToastContainer --------------

interface ToastContainerProps {
    layer: ToastLayer;
    toasts: ToastItem[];
    dismiss: (id: string) => void;
}

export function ToastContainer({ layer, toasts, dismiss }: ToastContainerProps) {
    if (toasts.length === 0) return null;

    return createPortal(
        <div className={classNames("toast-container", `toast-container--${layer}`)}>
            {toasts.map(toast => (
                <ToastItemEl
                    key={toast.id}
                    toast={toast}
                    onDismiss={dismiss}
                />
            ))}
        </div>,
        document.body
    );
}
