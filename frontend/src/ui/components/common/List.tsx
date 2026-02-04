import type { CSSProperties, HTMLAttributes } from "react";
import { classNames } from "../../utils/classNames";

type ListProps = HTMLAttributes<HTMLUListElement | HTMLOListElement> & {
   items: React.ReactNode[];
   ordered?: boolean;
   gap?: CSSProperties["gap"];
};

export function List({ items, ordered, gap, style, className, ...rest }: ListProps) {
   const Tag = ordered ? "ol" : "ul";
   return (
      <Tag
         className={classNames(className)}
         style={{
            paddingLeft: "1.2rem",
            color: "var(--color-text)",
            display: gap ? "grid" : undefined,
            gap,
            ...style,
         }}
         {...rest}
      >
         {items.map((item, i) => (
            <li key={i}>{item}</li>
         ))}
      </Tag>
   );
}
