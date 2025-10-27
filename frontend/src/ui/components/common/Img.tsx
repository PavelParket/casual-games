import type { ImgHTMLAttributes } from "react";

type ImgProps = ImgHTMLAttributes<HTMLImageElement> & {
   rounded?: boolean;
};

export function Img({ rounded, ...props }: ImgProps) {
   return (
      <img
         {...props}
         style={{
            borderRadius: rounded ? "50%" : "var(--radius-md)",
            maxWidth: "100%",
            height: "auto",
         }}
      />
   );
}
