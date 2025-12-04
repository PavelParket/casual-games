import type { ImgHTMLAttributes } from "react";
import { classNames } from "../../utils/classNames";

type ImgProps = ImgHTMLAttributes<HTMLImageElement> & {
   rounded?: boolean;
};

export function Img({ rounded, className, ...props }: ImgProps) {
   return (
      <img
         {...props}
         className={classNames(className)}
         style={{
            borderRadius: rounded ? "50%" : "var(--radius-md)",
            maxWidth: "100%",
            height: "auto",
         }}
      />
   );
}
