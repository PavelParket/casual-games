import type { CSSProperties, HTMLAttributes, ReactNode } from "react";

type StackProps = HTMLAttributes<HTMLDivElement> & {
   children: ReactNode;
   direction?: "row" | "column";
   gap?: CSSProperties["gap"];
   align?: CSSProperties["alignItems"];
   justify?: CSSProperties["justifyContent"];
   wrap?: CSSProperties["flexWrap"];
};

export function Stack({
   children,
   style,
   direction = "column",
   gap,
   align,
   justify,
   wrap,
   ...rest
}: StackProps) {
   return (
      <div
         style={{
            display: "flex",
            flexDirection: direction,
            gap,
            alignItems: align,
            justifyContent: justify,
            flexWrap: wrap,
            ...style,
         }}
         {...rest}
      >
         {children}
      </div>
   );
}
