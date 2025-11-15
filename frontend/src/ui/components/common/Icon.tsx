import type { ImgHTMLAttributes } from "react";

type IconProps = ImgHTMLAttributes<HTMLImageElement> & {
   size?: number;
   rounded?: boolean;
};

export function Icon({ size = 20, rounded, style, ...rest }: IconProps) {
   return (
      <img
         {...rest}
         style={{
            width: size,
            height: size,
            borderRadius: rounded ? "50%" : undefined,
            objectFit: "contain",
            ...style,
         }}
      />
   );
}
