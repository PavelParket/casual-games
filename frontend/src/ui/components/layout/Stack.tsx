import type { CSSProperties, HTMLAttributes, ReactNode } from "react";
import { classNames } from "../../utils/classNames";

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
   direction = "column",
   gap = "1rem",
   align = "stretch",
   justify = "flex-start",
   wrap,
   style,
   className,
   ...rest
}: StackProps) {
   return (
      <div
         className={classNames(className)}
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
