import { useEffect } from "react";
import { createPortal } from "react-dom";
import { Box, Img} from "../../../ui";

interface ImageViewerModalProps {
    isOpen: boolean;
    src: string;
    alt?: string;
    onClose: () => void;
}

export function ImageViewerModal({ isOpen, src, alt, onClose }: ImageViewerModalProps) {
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if (e.key === "Escape") onClose();
        };
        if (isOpen) document.addEventListener("keydown", handleKeyDown);
        return () => document.removeEventListener("keydown", handleKeyDown);
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return createPortal(
        <div 
            className="modal-overlay" 
            onClick={onClose} 
            style={{ zIndex: 9999, cursor: "zoom-out" }}
        >
            <Box 
                onClick={(e) => e.stopPropagation()} 
                style={{ cursor: "default", position: "relative" }}
            >
                <Img 
                    src={src} 
                    alt={alt} 
                    style={{ 
                        maxWidth: "90vw", 
                        maxHeight: "90vh", 
                        objectFit: "contain",
                        borderRadius: "var(--radius-md)",
                        boxShadow: "0 10px 40px rgba(0,0,0,0.5)"
                    }} 
                />
            </Box>
        </div>,
        document.body
    );
}
