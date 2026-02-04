import type { ImgHTMLAttributes } from "react";
import { classNames } from "../../utils/classNames";

type IconProps = ImgHTMLAttributes<HTMLImageElement> & {
   size?: number;
   rounded?: boolean;
};

export function Icon({ src, alt, size = 20, rounded, style, className, ...rest }: IconProps) {
   return (
      <img
         src={src}
         alt={alt}
         className={classNames(className)}
         style={{
            width: size,
            height: size,
            borderRadius: rounded ? "50%" : undefined,
            objectFit: "contain",
            ...style,
         }}
         {...rest}
      />
   );
}
