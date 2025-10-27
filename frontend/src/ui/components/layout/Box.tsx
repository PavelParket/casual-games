import type { ComponentPropsWithoutRef, ElementType } from "react";

type BoxProps<T extends ElementType = "div"> = {
   component?: T;
} & ComponentPropsWithoutRef<T>;

export function Box<T extends ElementType = "div">({
   component,
   ...props
}: BoxProps<T>) {
   const Component = component || "div";
   return <Component {...props} />;
}
