import type { HTMLAttributes } from "react";
import { Box } from "../layout/Box";
import { Img } from "./Img";
import { Skeleton } from "./Skeleton";
import { Typography } from "./Typography";
import { classNames } from "../../utils/classNames";

export interface AvatarProps extends HTMLAttributes<HTMLDivElement> {
    src?: string | null;
    fallback: string;
    size?: number | string;
    isLoading?: boolean;
}

export function Avatar({
    src,
    fallback,
    size = 40,
    isLoading = false,
    className,
    style,
    ...rest
}: AvatarProps) {
    const firstLetter = fallback ? fallback.charAt(0).toUpperCase() : "?";

    const fontSize = typeof size === "number" ? `${size * 0.4}px` : "inherit";

    return (
        <Box
            className={classNames("avatar", className)}
            style={{
                width: size,
                height: size,
                borderRadius: "50%",
                background: "var(--color-bg)",
                boxShadow: "var(--shadow-sm)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                overflow: "hidden",
                flexShrink: 0,
                position: "relative",
                ...style,
            }}
            {...rest}
        >
            {isLoading ? (
                <Skeleton variant="circular" width="100%" height="100%" />
            ) : src ? (
                <Img
                    src={src}
                    alt={fallback}
                    style={{ width: "100%", height: "100%", objectFit: "cover" }}
                />
            ) : (
                <Typography
                    variant="h3"
                    style={{
                        fontWeight: 600,
                        margin: 0,
                        lineHeight: 1,
                        fontSize: fontSize,
                    }}
                >
                    {firstLetter}
                </Typography>
            )}
        </Box>
    );
}
