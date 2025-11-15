import { Input } from "./Input";
import "../styles/textfield.css";

type TextFieldProps = {
   value: string;
   onChange: (value: string) => void;
   placeholder?: string;
   rounded?: boolean;
   disabled?: boolean;
}

export function Textfield({ value, onChange, placeholder, rounded, disabled }: TextFieldProps) {
   return (
      <Input
         type="text"
         className="textfield"
         value={value}
         onChange={(e) => onChange(e.target.value)}
         placeholder={placeholder}
         disabled={disabled}
         style={{
            borderRadius: rounded ? 20 : undefined,
         }}
      />
   );
}
