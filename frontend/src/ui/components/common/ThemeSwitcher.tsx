import { useEffect, useState, type CSSProperties, type ReactNode } from "react";
import "../styles/themeswitcher.css";
import { useTheme } from "../../theme/useTheme";
import { classNames } from "../../utils/classNames";

type ThemeSwitcherProps = {
   size?: "sm" | "md" | "lg";
   icon?: ReactNode;
   style?: CSSProperties;
   className?: string;
};

export function ThemeSwitcher({ size = "sm", icon, style, className }: ThemeSwitcherProps) {
   const { theme, toggleTheme } = useTheme();
   const [checked, setChecked] = useState(theme === "dark");

   useEffect(() => {
      setChecked(theme === "dark");
   }, [theme]);

   const handleChange = () => {
      toggleTheme();
   };

   return (
      <label className={classNames("theme-switch", `theme-switch-${size}`, className)} style={style}>
         <input
            type="checkbox"
            checked={checked}
            onChange={handleChange}
         />
         <span className="theme-switch-slider" />
         {icon && <span className="theme-switch-icon">{icon}</span>}
      </label>
   );
}
