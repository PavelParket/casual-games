import type { ComponentPropsWithoutRef, ElementType } from "react";
import { classNames } from "../../utils/classNames";

type BoxProps<T extends ElementType = "div"> = {
   component?: T;
} & ComponentPropsWithoutRef<T>;

export function Box<T extends ElementType = "div">({
   component,
   className,
   ...props
}: BoxProps<T>) {
   const Component = component || "div";

   return <Component className={classNames(className)} {...props} />;
}
