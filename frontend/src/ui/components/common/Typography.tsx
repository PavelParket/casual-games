import type { ComponentPropsWithoutRef, JSX } from "react";
import "../styles/typography.css";

export function Typography(props: { variant: "h1"; inverse?: boolean } & ComponentPropsWithoutRef<"h1">): JSX.Element;
export function Typography(props: { variant: "h2"; inverse?: boolean } & ComponentPropsWithoutRef<"h2">): JSX.Element;
export function Typography(props: { variant: "h3"; inverse?: boolean } & ComponentPropsWithoutRef<"h3">): JSX.Element;
export function Typography(props: { variant: "caption"; inverse?: boolean } & ComponentPropsWithoutRef<"span">): JSX.Element;
export function Typography(props: { variant?: "body"; inverse?: boolean } & ComponentPropsWithoutRef<"p">): JSX.Element;

export function Typography(
   props:
      | ({ variant: "h1"; inverse?: boolean } & ComponentPropsWithoutRef<"h1">)
      | ({ variant: "h2"; inverse?: boolean } & ComponentPropsWithoutRef<"h2">)
      | ({ variant: "h3"; inverse?: boolean } & ComponentPropsWithoutRef<"h3">)
      | ({ variant: "caption"; inverse?: boolean } & ComponentPropsWithoutRef<"span">)
      | ({ variant?: "body"; inverse?: boolean } & ComponentPropsWithoutRef<"p">)
) {
   const { variant = "body", inverse = false, className, children, ...rest } = props;

   const Tag =
      variant === "body"
         ? "p"
         : variant === "caption"
            ? "span"
            : variant;

   const inverseClass = inverse ? "text-color-inverse" : "";

   return (
      <Tag
         className={`typography ${variant}${inverseClass ? ` ${inverseClass}` : ""}${className ? ` ${className}` : ""}`}
         {...rest}
      >
         {children}
      </Tag>
   );
}
