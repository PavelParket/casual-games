import { useEffect, useState, type CSSProperties, type ReactNode } from "react";
import { useTheme } from "../../theme/useTheme";
import "../styles/themeswitcher.css";

type ThemeSwitcherProps = {
   size?: "sm" | "md" | "lg";
   icon?: ReactNode;
   style?: CSSProperties;
};

export function ThemeSwitcher({ size = "sm", icon, style }: ThemeSwitcherProps) {
   const { theme, toggleTheme } = useTheme();
   const [checked, setChecked] = useState(theme === "dark");

   useEffect(() => {
      setChecked(theme === "dark");
   }, [theme]);

   const handleChange = () => {
      toggleTheme();
   };

   return (
      <label className={`theme-switch theme-switch-${size}`} style={style}>
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
