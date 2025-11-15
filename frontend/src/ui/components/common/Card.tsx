import type { HTMLAttributes, ReactNode } from "react";
import "../styles/card.css";

type CardProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
};

export function Card({ children, ...props }: CardProps) {
   return <div className="card" {...props}>{children}</div>;
}
