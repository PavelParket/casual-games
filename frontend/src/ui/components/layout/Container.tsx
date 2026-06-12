import type { HTMLAttributes, ReactNode } from "react";
import { classNames } from "../../utils/classNames";

type ContainerProps = HTMLAttributes<HTMLDivElement> & {
    children: ReactNode;
    maxWidth?: string;
};

export function Container({ children, maxWidth = "1200px", className, style, ...rest }: ContainerProps) {
    return (
        <div
            className={classNames("container", className)}
            style={{
                maxWidth,
                ...style,
            }}
            {...rest}
        >
            {children}
        </div>
    );
}
