import { forwardRef, type InputHTMLAttributes } from "react";
import "../styles/input.css";

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
    (props, ref) => {
        return <input ref={ref} className="input" {...props} />;
    }
);
