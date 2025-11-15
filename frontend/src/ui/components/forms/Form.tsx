import type { FormHTMLAttributes, ReactNode } from "react";
import { Stack } from "../layout/Stack";

type FormProps = FormHTMLAttributes<HTMLFormElement> & {
   children: ReactNode;
   gap?: string;
};

export function Form({ children, gap = "12px", style, ...rest }: FormProps) {
   return (
      <form {...rest} style={{ margin: 0 }}>
         <Stack gap={gap} style={style}>
            {children}
         </Stack>
      </form>
   );
}
