import type { CSSProperties } from "react";

type ListProps = {
   items: React.ReactNode[];
   ordered?: boolean;
   gap?: CSSProperties["gap"];
   style?: CSSProperties;
};

export function List({ items, ordered, gap, style }: ListProps) {
   const Tag = ordered ? "ol" : "ul";
   return (
      <Tag
         style={{
            paddingLeft: "1.2rem",
            color: "var(--color-text)",
            display: gap ? "grid" : undefined,
            gap,
            ...style,
         }}
      >
         {items.map((item, i) => (
            <li key={i}>{item}</li>
         ))}
      </Tag>
   );
}
