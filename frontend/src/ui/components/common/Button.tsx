import type { ButtonHTMLAttributes } from "react";
import "../styles/button.css";
import { classNames } from "../../utils/classNames";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
   variant?: "solid" | "outline" | "ghost";
};

export function Button({ variant = "solid", className, children, ...props }: ButtonProps) {
   return (
      <button {...props} className={classNames("btn", `btn-${variant}`, className)}>
         <span className="btn-content">
            {children}
         </span>
      </button>
   );
}
