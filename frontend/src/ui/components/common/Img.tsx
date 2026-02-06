import type { ImgHTMLAttributes, CSSProperties } from "react";
import { classNames } from "../../utils/classNames";

type ImgProps = ImgHTMLAttributes<HTMLImageElement> & {
   rounded?: boolean;
};

export function Img({ rounded, className, style, ...props }: ImgProps) {
   const baseStyles: CSSProperties = {
      borderRadius: rounded ? "50%" : "var(--radius-md)",
      maxWidth: "100%",
      height: "auto",
   };

   return (
      <img
         {...props}
         className={classNames(className)}
         style={{ ...baseStyles, ...style }}
      />
   );
}