import { motion } from "framer-motion";
import type { DurakCard } from "../../../../models/Durak";
import { Box, Img } from "../../../../ui";
import { CARD_BACK, getCardImage } from "../utils/CardUtils";

interface PlayingCardProps {
    card?: DurakCard;
    faceDown?: boolean;
    size?: "sm" | "md";
    draggable?: boolean;
    disabled?: boolean;
    trumpRotated?: boolean;
    layoutId?: string;
    style?: React.CSSProperties;
    onClick?: () => void;
    onDragEnd?: (event: MouseEvent | TouchEvent | PointerEvent, info: { point: { x: number; y: number }; offset: { x: number; y: number } }) => void;
}

const SIZES = {
    sm: { width: 50, height: 70 },
    md: { width: 72, height: 100 },
} as const;

const fullCoverStyle: React.CSSProperties = {
    width: "100%",
    height: "100%",
    objectFit: "contain",
    display: "block",
};

export function PlayingCard({
    card,
    faceDown = false,
    size = "md",
    draggable = false,
    disabled = false,
    trumpRotated = false,
    layoutId,
    style,
    onClick,
    onDragEnd,
}: PlayingCardProps) {
    const { width, height } = SIZES[size];
    const isClickable = !!onClick && !disabled;
    const imgSrc = card ? getCardImage(card.rank, card.suit) : CARD_BACK;

    return (
        <motion.div
            layoutId={layoutId}
            drag={draggable && !disabled}
            dragSnapToOrigin
            whileDrag={{ scale: 1.08, zIndex: 50 }}
            onClick={isClickable ? onClick : undefined}
            onDragEnd={draggable && !disabled ? onDragEnd : undefined}
            transition={{ layout: { duration: 0.25, ease: "easeOut" } }}
            style={{
                position: "relative",
                display: "inline-block",
                width,
                height,
                cursor: isClickable ? "pointer" : "default",
                opacity: disabled ? 0.5 : 1,
                transform: trumpRotated ? "rotate(90deg)" : undefined,
                transformOrigin: "center center",
                userSelect: "none",
                flexShrink: 0,
                ...style,
            }}
        >
            {/* motion.div — rotateY flip animation */}
            <motion.div
                animate={{ rotateY: faceDown ? 180 : 0 }}
                transition={{ duration: 0.35, ease: "easeInOut" }}
                style={{
                    position: "relative",
                    width: "100%",
                    height: "100%",
                    transformStyle: "preserve-3d",
                }}
            >
                {/* Face */}
                <Box style={{
                    position: "absolute",
                    inset: 0,
                    backfaceVisibility: "hidden",
                    WebkitBackfaceVisibility: "hidden",
                    borderRadius: "var(--radius-sm)",
                    overflow: "hidden",
                    boxShadow: "var(--shadow-md)",
                    background: "#fff",
                }}>
                    <Img
                        src={imgSrc}
                        alt={card ? `${card.rank} of ${card.suit}` : "card"}
                        draggable={false}
                        style={fullCoverStyle}
                    />
                </Box>

                {/* Back */}
                <Box style={{
                    position: "absolute",
                    inset: 0,
                    backfaceVisibility: "hidden",
                    WebkitBackfaceVisibility: "hidden",
                    transform: "rotateY(180deg)",
                    borderRadius: "var(--radius-sm)",
                    overflow: "hidden",
                    boxShadow: "var(--shadow-md)",
                    background: "#1e3a8a",
                }}>
                    <Img
                        src={CARD_BACK}
                        alt="card back"
                        draggable={false}
                        style={fullCoverStyle}
                    />
                </Box>
            </motion.div>
        </motion.div>
    );
}
