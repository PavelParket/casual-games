import type { CSSProperties } from "react";
import "./styles/horsesprite.css"

const FRAME_W = 64;
const FRAME_H = 64;
const FRAME_COUNT = 4;
const GALLOP_EAST_Y = 192;

export const HORSE_FILTERS: Record<string, string> = {
    "#e74c3c": "sepia(1) saturate(5) hue-rotate(327deg) brightness(1.1)",
    "#e67e22": "sepia(1) saturate(5) hue-rotate(349deg) brightness(1.1)",
    "#2ecc71": "sepia(1) saturate(5) hue-rotate(98deg)  brightness(1.0)",
    "#3498db": "sepia(1) saturate(5) hue-rotate(169deg) brightness(1.0)",
    "#9b59b6": "sepia(1) saturate(5) hue-rotate(245deg) brightness(1.0)",
    "#f1c40f": "sepia(1) saturate(5) hue-rotate(11deg)  brightness(1.2)",
    "#e91e63": "sepia(1) saturate(5) hue-rotate(299deg) brightness(1.1)",
    "#1abc9c": "sepia(1) saturate(5) hue-rotate(130deg) brightness(1.0)",
};

type HorseSpriteProps = {
    color: string;
    size?: number;
    isRunning?: boolean;
    isWinner?: boolean;
    label?: string;
    style?: CSSProperties;
};

export default function HorseSprite({
    color,
    size = 48,
    isRunning = false,
    isWinner = false,
    label,
    style,
}: HorseSpriteProps) {
    const scale = size / FRAME_W;
    const bgY = isRunning ? GALLOP_EAST_Y : GALLOP_EAST_Y;
    const bgSize = `${FRAME_W * FRAME_COUNT * scale}px auto`;
    const bgPosition = `0px -${bgY * scale}px`;

    const cssFilter = HORSE_FILTERS[color] ?? "sepia(1) saturate(4) hue-rotate(0deg)";

    return (
        <div
            style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: "2px",
                ...style,
            }}
        >
            <div
                className={[
                    "horse-sprite",
                    isRunning && "horse-sprite--running",
                    isWinner && "horse-sprite--winner",
                ]
                    .filter(Boolean)
                    .join(" ")}
                style={{
                    width: size,
                    height: size,
                    backgroundImage: `url(/src/assets/images/horse-white.png)`,
                    backgroundSize: bgSize,
                    backgroundPosition: bgPosition,
                    backgroundRepeat: "no-repeat",
                    imageRendering: "pixelated",
                    filter: cssFilter,
                    ["--sprite-w" as string]: `${FRAME_W * FRAME_COUNT * scale}px`,
                    ["--sprite-frame-w" as string]: `${FRAME_W * scale}px`,
                }}
            />
            {label && (
                <span
                    style={{
                        fontSize: "9px",
                        fontWeight: 700,
                        color,
                        lineHeight: 1,
                        textShadow: "0 1px 2px rgba(0,0,0,0.5)",
                    }}
                >
                    {label}
                </span>
            )}
        </div>
    );
}
