import type { HTMLAttributes, JSX, ReactNode } from "react";
import "../styles/divider.css";
import { classNames } from "../../utils/classNames";

type DividerProps = HTMLAttributes<HTMLElement> & {
    orientation?: "horizontal" | "vertical";
    variant?: "fullwidth" | "middle";
    flexItem?: boolean;
    listItem?: boolean;
    children?: ReactNode;
    childAlign?: "center" | "left" | "right";
};

export function Divider({
    orientation = "horizontal",
    variant = "fullwidth",
    flexItem = false,
    listItem = false,
    children,
    childAlign = "center",
    style,
    className,
    ...rest
}: DividerProps) {
    const classes = classNames(
        "divider",
        !children ? `divider-${orientation}` : undefined,
        children ? "divider-with-content" : undefined,
        children ? `divider-content-${childAlign}` : undefined,
        variant && `divider-${variant}`,
        flexItem ? "divider-flex" : undefined,
        className,
    );

    let Component: keyof JSX.IntrinsicElements = "hr";

    if (listItem) {
        Component = "li";
    } else if (children) {
        Component = "div";
    }

    return (
        <Component
            className={classes}
            role={Component === "div" ? "separator" : undefined}
            aria-orientation={orientation === 'vertical' ? 'vertical' : undefined}
            style={style}
            {...rest}
        >
            {children}
        </Component>
    );
}
