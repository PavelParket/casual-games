import type { CSSProperties } from "react";
import "../styles/skeleton.css";
import { classNames } from "../../utils/classNames";

type SkeletonProps = {
    variant?: "text" | "circular" | "rectangular" | "card";
    width?: CSSProperties["width"];
    height?: CSSProperties["height"];
    style?: CSSProperties;
    className?: string;
    count?: number;
};

export function Skeleton({
    variant = "text",
    width,
    height,
    style,
    className,
    count = 1,
}: SkeletonProps) {
    const skeletonStyle: CSSProperties = {
        width: width ?? "100%",
        height: height ?? "100%",
        ...style,
    };

    if (count > 1) {
        return (
            <>
                {Array.from({ length: count }).map((_, index) => (
                    <div
                        key={index}
                        className={classNames("skeleton", `skeleton-${variant}`, className)}
                        style={skeletonStyle}
                    />
                ))}
            </>
        );
    }

    return (
        <div
            className={classNames("skeleton", `skeleton-${variant}`, className)}
            style={skeletonStyle}
        />
    );
}
