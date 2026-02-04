import type { HTMLAttributes, ReactNode } from "react";
import { classNames } from "../../utils/classNames";

type ContainerProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
   maxWidth?: string;
};

export function Container({ children, maxWidth = "1200px", className, ...rest }: ContainerProps) {
   return (
      <div
         className={classNames(className)}
         style={{
            maxWidth,
            margin: "0 auto",
            padding: "0 1rem 1.5rem 1rem",
         }}
         {...rest}
      >
         {children}
      </div>
   );
}
