import type { ReactNode } from "react";
import "../styles/modal.css";
import { Icon } from "../common/Icon";
import { useThemedIcon } from "../../hooks/useThemedIcon";

type ModalProps = {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    children: ReactNode;
    disableOutsideClick?: boolean;
}

export function Modal({ isOpen, onClose, title, children, disableOutsideClick }: ModalProps) {
    const { getIcon } = useThemedIcon();

    if (!isOpen) {
        return null;
    }

    return (
        <div className="modal-overlay" onClick={disableOutsideClick ? undefined : onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                {title && <h2 className="modal-title">{title}</h2>}
                <div className="modal-body">{children}</div>
                <button className="modal-close" onClick={onClose}>
                    <Icon src={getIcon("close")} size={24} alt="close" />
                </button>
            </div>
        </div>
    );
}
