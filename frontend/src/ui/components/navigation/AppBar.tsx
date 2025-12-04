import type { HTMLAttributes, ReactNode } from "react";
import "../styles/appbar.css";
import { classNames } from "../../utils/classNames";

type AppBarProps = HTMLAttributes<HTMLElement> & {
   left?: ReactNode;
   center?: ReactNode;
   right?: ReactNode;
   position?: "static" | "sticky" | "fixed";
   height?: string | number;
   paddingX?: string | number;
};

export function AppBar({
   left,
   center,
   right,
   position = "sticky",
   height,
   paddingX,
   style,
   className,
   ...rest
}: AppBarProps) {
   return (
      <header
         className={classNames("app-bar", className)}
         style={{
            position,
            top: position === "sticky" || position === "fixed" ? 0 : undefined,
            ...style,
         }}
         {...rest}
      >
         <div
            className="app-bar-content"
            style={{
               height,
               paddingLeft: `${paddingX}`,
               paddingRight: `${paddingX}`,
            }}
         >
            <div className="app-bar-left">
               {left}
            </div>

            {center && (
               <div className="app-bar-center">
                  {center}
               </div>
            )}

            <div className="app-bar-right">
               {right}
            </div>
         </div>
      </header>
   );
}
