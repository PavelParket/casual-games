import type { HTMLAttributes, ReactNode } from "react";
import "../styles/durakCard.css";
import { classNames } from "../../utils/classNames";

type CardProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
};

export function Card({ children, className, ...props }: CardProps) {
   return <div className={classNames("durakCard", className)} {...props}>{children}</div>;
}
