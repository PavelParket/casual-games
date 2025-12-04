import type { CSSProperties, HTMLAttributes, ReactNode } from "react";
import { classNames } from "../../utils/classNames";

type GridProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
   columns?: CSSProperties["gridTemplateColumns"];
   rows?: CSSProperties["gridTemplateRows"];
   gap?: CSSProperties["gap"];
   alignItems?: CSSProperties["alignItems"];
   justifyItems?: CSSProperties["justifyItems"];
   alignContent?: CSSProperties["alignContent"];
   justifyContent?: CSSProperties["justifyContent"];
};

export function Grid({
   children,
   columns,
   rows,
   gap = "1rem",
   alignItems = "stretch",
   justifyItems = "stretch",
   alignContent,
   justifyContent,
   style,
   className,
   ...rest
}: GridProps) {
   return (
      <div
         className={classNames(className)}
         style={{
            display: "grid",
            gridTemplateColumns: columns,
            gridTemplateRows: rows,
            gap,
            alignItems,
            justifyItems,
            alignContent,
            justifyContent,
            ...style,
         }}
         {...rest}
      >
         {children}
      </div>
   );
}
