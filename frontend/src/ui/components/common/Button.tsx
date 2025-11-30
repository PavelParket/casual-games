import type { ButtonHTMLAttributes } from "react";
import "../styles/button.css";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
   variant?: "solid" | "outline" | "ghost";
};

export function Button({ variant = "solid", className, children, ...props }: ButtonProps) {
   return (
      <button {...props} className={`btn btn-${variant} ${className}`}>
         {children}
      </button>
   );
}
