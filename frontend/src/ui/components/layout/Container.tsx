import type { ReactNode } from "react";

type ContainerProps = {
   children: ReactNode;
   maxWidth?: string;
};

export function Container({ children, maxWidth = "1200px" }: ContainerProps) {
   return (
      <div
         style={{
            maxWidth,
            margin: "0 0",
            padding: "0 1rem",
         }}
      >
         {children}
      </div>
   );
}
