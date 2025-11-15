import { createContext } from "react";

export type Theme = "light" | "dark";

export type ThemeContextProps = {
   theme: Theme;
   toggleTheme: () => void;
};

export const ThemeContext = createContext<ThemeContextProps>({
   theme: "light",
   toggleTheme: () => { },
});
