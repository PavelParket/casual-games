import type { HTMLAttributes, ReactNode } from "react";
import "../styles/card.css";
import { classNames } from "../../utils/classNames";

type CardProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
};

export function Card({ children, className, ...props }: CardProps) {
   return <div className={classNames("card", className)} {...props}>{children}</div>;
}
