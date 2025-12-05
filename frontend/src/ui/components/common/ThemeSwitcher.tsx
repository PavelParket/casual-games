import { useEffect, useState, type CSSProperties } from "react";
import "../styles/themeswitcher.css";
import { useTheme } from "../../theme/useTheme";
import { Switch } from "./Switch";
import { Box } from "../layout/Box";
import { Icon } from "./Icon";
import { useThemedIcon } from "../../hooks/useThemedIcon";
import { classNames } from "../../utils/classNames";

type ThemeSwitcherProps = {
   size?: "sm" | "md" | "lg";
   style?: CSSProperties;
   className?: string;
};

export function ThemeSwitcher({ size = "sm", style, className }: ThemeSwitcherProps) {
   const { theme, toggleTheme } = useTheme();
   const [checked, setChecked] = useState(theme === "dark");

   const { getIcon } = useThemedIcon();

   useEffect(() => {
      setChecked(theme === "dark");
   }, [theme]);

   const handleChange = () => {
      toggleTheme();
   };

   return (
      <Box className={classNames("theme-switch", className)} style={style}>
         <Switch size={size} checked={checked} onChange={handleChange} />
         <Icon className="theme-switch-icon" src={theme === "light" ? getIcon("sun") : getIcon("moonStars")} alt="sunOrMoom" size={30} />
      </Box>
   );
}
