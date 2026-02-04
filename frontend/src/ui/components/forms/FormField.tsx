import type { InputHTMLAttributes, ReactNode } from "react";
import "../styles/formfield.css";
import { Input } from "./Input";
import { Icon } from "../common/Icon";
import { classNames } from "../../utils/classNames";

type FormFieldProps = InputHTMLAttributes<HTMLInputElement> & {
   label?: string;
   helperText?: string;
   rounded?: boolean;
   endAdornment?: ReactNode;
   endAdornmentSrc?: string;
   endAdornmentAlt?: string;
   endAdornmentSize?: number;
};

export function FormField({
   label,
   placeholder,
   helperText,
   rounded,
   style,
   className,
   endAdornment,
   endAdornmentSrc,
   endAdornmentAlt,
   endAdornmentSize = 20,
   ...rest
}: FormFieldProps) {
   const hasEndIcon = Boolean(endAdornmentSrc || endAdornment);

   return (
      <div className="form-field-root" style={style}>
         {label &&
            <label className="label">
               {label}
            </label>
         }

         <div style={{ position: "relative" }}>
            <Input
               className={classNames(
                  "form-input",
                  hasEndIcon && "has-end-icon",
                  className
               )}
               placeholder=" "
               style={{
                  borderRadius: rounded ? 12 : 8,
               }}
               {...rest}
            />

            {placeholder && (
               <label className="floating-label">
                  {placeholder}
               </label>
            )}

            {hasEndIcon && (
               <div className="field-icon field-icon-end">
                  {endAdornmentSrc ? (
                     <Icon src={endAdornmentSrc} alt={endAdornmentAlt} size={endAdornmentSize} />
                  ) : (
                     endAdornment
                  )}
               </div>
            )}
         </div>

         {helperText && (
            <span className="helper-text">
               {helperText}
            </span>
         )}
      </div>
   );
}
