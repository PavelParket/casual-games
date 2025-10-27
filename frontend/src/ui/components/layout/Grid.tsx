import type { CSSProperties, HTMLAttributes, ReactNode } from "react";

type GridProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
   columns?: CSSProperties["gridTemplateColumns"];
   rows?: CSSProperties["gridTemplateRows"];
   gap?: CSSProperties["gap"];
   align?: CSSProperties["alignItems"];
   justify?: CSSProperties["justifyItems"];
};

export function Grid({
   children,
   style,
   columns,
   rows,
   gap,
   align,
   justify,
   ...rest
}: GridProps) {
   return (
      <div
         style={{
            display: "grid",
            gridTemplateColumns: columns,
            gridTemplateRows: rows,
            gap,
            alignItems: align,
            justifyItems: justify,
            ...style,
         }}
         {...rest}
      >
         {children}
      </div>
   );
}
