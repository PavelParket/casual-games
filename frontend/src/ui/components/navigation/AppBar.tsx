import type { HTMLAttributes, ReactNode } from "react";

type AppBarProps = {
   left?: ReactNode;
   center?: ReactNode;
   right?: ReactNode;
   position?: "static" | "sticky" | "fixed";
   height?: string | number;
   paddingX?: string | number;
   className?: string;
   style?: React.CSSProperties;
} & HTMLAttributes<HTMLElement>;

export function AppBar({
   left,
   center,
   right,
   position = "sticky",
   height = 60,
   paddingX = "2rem",
   className,
   style,
   ...rest
}: AppBarProps) {
   return (
      <header
         className={className}
         style={{
            position,
            top: position === "sticky" || position === "fixed" ? 0 : undefined,
            zIndex: 100,
            width: "100%",
            background: "var(--glass-surface)",
            backdropFilter: `blur(var(--glass-blur))`,
            boxShadow: "var(--shadow-sm)",
            ...style,
         }}
         {...rest}
      >
         <div
            style={{
               display: "flex",
               alignItems: "center",
               justifyContent: "space-between",
               height,
               padding: `0 ${paddingX}`,
            }}
         >
            <div style={{ flexShrink: 0 }}>
               {left}
            </div>

            {center && (
               <div
                  style={{
                     flex: 1,
                     display: "flex",
                     justifyContent: "center",
                     alignItems: "center"
                  }}
               >
                  {center}
               </div>
            )}

            <div style={{ flexShrink: 0, display: "flex", alignItems: "center", gap: "0.5rem" }}>
               {right}
            </div>
         </div>
      </header>
   );
}
