import type { InputHTMLAttributes } from "react";
import "../styles/input.css";

export function Input(props: InputHTMLAttributes<HTMLInputElement>) {
   return <input className="input" {...props} />;
}
