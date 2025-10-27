import type { CSSProperties, InputHTMLAttributes, ReactNode } from "react";
import { Input } from "./Input";
import { Icon } from "../common/Icon";

type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
   label?: string;
   helperText?: string;
   rounded?: boolean;
   inputStyle?: CSSProperties;
   endAdornment?: ReactNode;
   endAdornmentSrc?: string;
   endAdornmentAlt?: string;
   endAdornmentSize?: number;
};

export function FormField({ label, helperText, rounded, inputStyle, endAdornment, endAdornmentSrc, endAdornmentAlt, endAdornmentSize = 20, ...rest }: FormFieldProps) {
   return (
      <div style={{ display: "grid", gap: "6px", position: "relative" }}>
         {label ? <label style={{ color: "var(--color-text)" }}>{label}</label> : null}
         <Input
            {...rest}
            style={{
               borderRadius: rounded ? 40 : undefined,
               paddingRight: endAdornment ? 48 : undefined,
               ...(inputStyle || {}),
               ...(rest.style as CSSProperties),
            }}
         />
         {(endAdornmentSrc || endAdornment) ? (
            <div
               style={{
                  position: "absolute",
                  right: 12,
                  top: "50%",
                  transform: "translateY(-50%)",
                  pointerEvents: "none",
                  color: "var(--color-text)",
                  opacity: 0.8,
                  display: "flex",
                  alignItems: "center",
               }}
            >
               {endAdornmentSrc ? (
                  <Icon src={endAdornmentSrc} alt={endAdornmentAlt} size={endAdornmentSize} />
               ) : (
                  endAdornment
               )}
            </div>
         ) : null}
         {helperText ? (
            <span style={{ fontSize: "0.8rem", opacity: 0.8, color: "var(--color-text)" }}>{helperText}</span>
         ) : null}
      </div>
   );
}
