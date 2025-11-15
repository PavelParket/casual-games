import type { HTMLAttributes, ReactNode } from "react";
import { Box } from "../layout/Box";

type NavbarProps = HTMLAttributes<HTMLElement> & {
   brand?: ReactNode;
   right?: ReactNode;
};

export function Navbar({ brand, right, style, ...rest }: NavbarProps) {
   return (
      <header
         style={{
            position: "sticky",
            top: 0,
            zIndex: 100,
            background: "var(--glass-surface)",
            backdropFilter: `blur(var(--glass-blur))`,
            boxShadow: "var(--shadow-sm)",
            ...style,
         }}
         {...rest}
      >
         <Box style={{
            padding: "0 9rem",
         }}>
            <div style={{
               display: "flex",
               alignItems: "center",
               justifyContent: "space-between",
               height: "60px",
            }}>
               <div>{brand}</div>
               <nav style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                  {right}
               </nav>
            </div>
         </Box>
      </header>
   );
}
